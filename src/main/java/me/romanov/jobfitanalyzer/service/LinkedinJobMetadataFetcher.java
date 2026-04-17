package me.romanov.jobfitanalyzer.service;

import me.romanov.jobfitanalyzer.dto.JobMetadata;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Primary
@Component
public class LinkedinJobMetadataFetcher implements JobMetadataFetcher {

    private final HttpClient httpClient;

    public LinkedinJobMetadataFetcher() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    @Autowired
    public LinkedinJobMetadataFetcher(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Optional<JobMetadata> fetch(String sourceUrl) {
        try {
            String html = loadHtml(sourceUrl);
            Document doc = Jsoup.parse(html, sourceUrl);

            Optional<JobMetadata> fromJsonLd = parseFromJsonLd(doc);
            if (fromJsonLd.isPresent()) {
                return fromJsonLd;
            }

            JobMetadata fromHtml = parseFromHtml(doc);
            return isEmpty(fromHtml) ? Optional.empty() : Optional.of(fromHtml);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    protected String loadHtml(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept-Language", "en-US,en;q=0.9")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new IllegalStateException("HTTP error: " + response.statusCode());
        }

        return response.body();
    }

    private Optional<JobMetadata> parseFromJsonLd(Document doc) {
        for (Element script : doc.select("script[type=application/ld+json]")) {
            String json = script.data();
            if (json.isBlank() || !json.contains("\"JobPosting\"")) {
                continue;
            }

            String title = extractJsonValue(json, "title");
            String company = extractNestedValue(json, "hiringOrganization", "name");
            String location = extractNestedValue(json, "jobLocation", "addressLocality");
            if (location == null) {
                location = extractNestedValue(json, "address", "addressLocality");
            }
            String description = extractJsonValue(json, "description");

            if (!allBlank(title, company, location, description)) {
                return Optional.of(new JobMetadata(
                        blankToNull(normalizeInline(company)),
                        blankToNull(normalizeInline(title)),
                        blankToNull(normalizeInline(location)),
                        blankToNull(formatHtmlFragmentForAi(description))
                ));
            }
        }

        return Optional.empty();
    }

    private JobMetadata parseFromHtml(Document doc) {
        String title = text(doc, "h1.top-card-layout__title", "h1");
        String company = text(doc, "a.topcard__org-name-link", "span.topcard__flavor", "div.topcard__flavor-row a");
        String location = text(doc, "span.topcard__flavor--bullet", "div.topcard__flavor-row span.topcard__flavor--bullet");
        String description = formattedText(doc
        );

        return new JobMetadata(
                blankToNull(company),
                blankToNull(title),
                blankToNull(location),
                blankToNull(description)
        );
    }

    private String text(Document doc, String... selectors) {
        for (String selector : selectors) {
            Element el = doc.selectFirst(selector);
            if (el == null) {
                continue;
            }

            String normalized = normalizeInline(el.text());
            if (!normalized.isBlank()) {
                return normalized;
            }
        }
        return null;
    }

    private String formattedText(Document doc) {
        for (String selector : new String[]{"div.show-more-less-html__markup", "div.description__text", "section.show-more-less-html"}) {
            Element el = doc.selectFirst(selector);
            if (el == null) {
                continue;
            }

            String text = formatElementForAi(el);
            if (text != null && !text.isBlank()) {
                return text;
            }
        }
        return null;
    }

    /**
     * Formats an HTML element into plain text, suitable for AI:
     * - Preserves paragraphs
     * - Preserves lists
     * - Preserves line breaks
     * - Does not collapse everything into a single line
     */
    private String formatElementForAi(Element element) {
        if (element == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        appendNodeFormatted(element, sb, 0);
        return normalizeStructuredText(sb.toString());
    }

    /**
     * Same as above, but for an HTML fragment from JSON-LD description.
     */
    private String formatHtmlFragmentForAi(String htmlFragment) {
        if (htmlFragment == null || htmlFragment.isBlank()) {
            return null;
        }

        Document doc = Jsoup.parseBodyFragment(htmlFragment);
        return formatElementForAi(doc.body());
    }

    private void appendNodeFormatted(Node node, StringBuilder sb, int listDepth) {
        if (node instanceof TextNode textNode) {
            appendTextNode(textNode, sb);
            return;
        }

        if (!(node instanceof Element element)) {
            for (Node child : node.childNodes()) {
                appendNodeFormatted(child, sb, listDepth);
            }
            return;
        }

        switch (element.tagName().toLowerCase()) {
            case "br" -> sb.append("\n");
            case "p", "div", "section", "article" -> appendBlockElement(element, sb, listDepth);
            case "h1", "h2", "h3", "h4", "h5", "h6" -> appendHeadingElement(element, sb);
            case "ul" -> appendUnorderedList(element, sb, listDepth);
            case "ol" -> appendOrderedList(element, sb, listDepth);
            case "li" -> appendListItem(element, sb, listDepth);
            default -> appendChildren(element, sb, listDepth);
        }
    }

    private void appendTextNode(TextNode textNode, StringBuilder sb) {
        String text = textNode.text();
        if (!text.isBlank()) {
            appendNormalizedText(sb, text);
        }
    }

    private void appendBlockElement(Element element, StringBuilder sb, int listDepth) {
        ensureParagraphBreak(sb);
        appendChildren(element, sb, listDepth);
        ensureParagraphBreak(sb);
    }

    private void appendHeadingElement(Element element, StringBuilder sb) {
        ensureParagraphBreak(sb);
        appendNormalizedText(sb, element.text());
        ensureParagraphBreak(sb);
    }

    private void appendUnorderedList(Element element, StringBuilder sb, int listDepth) {
        ensureParagraphBreak(sb);
        appendChildren(element, sb, listDepth + 1);
        ensureParagraphBreak(sb);
    }

    private void appendOrderedList(Element element, StringBuilder sb, int listDepth) {
        ensureParagraphBreak(sb);
        int index = 1;

        for (Element li : element.children()) {
            if ("li".equalsIgnoreCase(li.tagName())) {
                indent(sb, listDepth);
                sb.append(index++).append(". ");
                appendChildrenInline(li, sb, listDepth + 1);
                sb.append("\n");
            }
        }

        ensureParagraphBreak(sb);
    }

    private void appendListItem(Element element, StringBuilder sb, int listDepth) {
        indent(sb, listDepth);
        sb.append("- ");
        appendChildrenInline(element, sb, listDepth + 1);
        sb.append("\n");
    }

    private void appendChildren(Element element, StringBuilder sb, int listDepth) {
        for (Node child : element.childNodes()) {
            appendNodeFormatted(child, sb, listDepth);
        }
    }

    /**
     * For li/ol content: try not to break lines unnecessarily,
     * but nested lists/blocks are still formatted separately.
     */
    private void appendChildrenInline(Element element, StringBuilder sb, int listDepth) {
        for (Node child : element.childNodes()) {
            if (child instanceof Element childEl && isBlockLike(childEl.tagName())) {
                sb.append("\n");
                appendNodeFormatted(childEl, sb, listDepth);
            } else {
                appendNodeFormatted(child, sb, listDepth);
            }
        }
    }

    private boolean isBlockLike(String tag) {
        String lower = tag.toLowerCase();
        return "ul".equals(lower)
                || "ol".equals(lower)
                || "p".equals(lower)
                || "div".equals(lower)
                || "section".equals(lower);
    }

    private void appendNormalizedText(StringBuilder sb, String text) {
        String normalized = text
                .replace('\u00A0', ' ')
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .trim();

        if (normalized.isEmpty()) {
            return;
        }

        boolean needsSpace =
                !sb.isEmpty()
                        && sb.charAt(sb.length() - 1) != '\n'
                        && sb.charAt(sb.length() - 1) != ' '
                        && !startsWithPunctuation(normalized)
                        && !endsWithOpeningBracketOrSlash(sb);

        if (needsSpace) {
            sb.append(' ');
        }

        sb.append(normalized);
    }

    private boolean startsWithPunctuation(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        char ch = value.charAt(0);
        return ch == ','
                || ch == '.'
                || ch == ':'
                || ch == ';'
                || ch == '!'
                || ch == '?'
                || ch == ')'
                || ch == ']'
                || ch == '}';
    }

    private boolean endsWithOpeningBracketOrSlash(StringBuilder sb) {
        if (sb == null || sb.isEmpty()) {
            return false;
        }

        char ch = sb.charAt(sb.length() - 1);
        return ch == '(' || ch == '[' || ch == '{' || ch == '/';
    }

    private void ensureParagraphBreak(StringBuilder sb) {
        if (sb.isEmpty()) {
            return;
        }

        if (sb.toString().endsWith("\n\n")) {
            return;
        }

        if (sb.charAt(sb.length() - 1) == '\n') {
            sb.append('\n');
        } else {
            sb.append("\n\n");
        }
    }

    private void indent(StringBuilder sb, int depth) {
        if (!sb.isEmpty() && sb.charAt(sb.length() - 1) != '\n') {
            sb.append('\n');
        }

        sb.repeat("  ", Math.max(0, depth - 1));
    }

    private String normalizeStructuredText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value
                .replace('\u00A0', ' ')
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll(" *\n *", "\n")
                .replaceAll("\n{3,}", "\n\n")
                .trim();

        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeInline(String value) {
        if (value == null) {
            return null;
        }

        return value.replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex < 0) {
            return null;
        }

        int colonIndex = json.indexOf(':', keyIndex);
        if (colonIndex < 0) {
            return null;
        }

        int valueStart = json.indexOf('"', colonIndex + 1);
        if (valueStart < 0) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        boolean escaped = false;

        for (int i = valueStart + 1; i < json.length(); i++) {
            char ch = json.charAt(i);

            if (escaped) {
                result.append(ch);
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == '"') {
                return unescapeJson(result.toString());
            } else {
                result.append(ch);
            }
        }

        return unescapeJson(result.toString());
    }

    private String extractNestedValue(String json, String objectKey, String innerKey) {
        String objectPattern = "\"" + objectKey + "\"";
        int objectIndex = json.indexOf(objectPattern);
        if (objectIndex < 0) {
            return null;
        }

        int objectStart = json.indexOf('{', objectIndex);
        if (objectStart < 0) {
            return null;
        }

        int objectEnd = findMatchingObjectEnd(json, objectStart);
        if (objectEnd < 0) {
            return null;
        }

        String objectJson = json.substring(objectStart, objectEnd + 1);
        return extractJsonValue(objectJson, innerKey);
    }

    private int findMatchingObjectEnd(String json, int objectStart) {
        int depth = 0;

        for (int i = objectStart; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '{') {
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    private String unescapeJson(String value) {
        return value
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\/", "/")
                .replace("\\\\", "\\");
    }

    private boolean allBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private boolean isEmpty(JobMetadata metadata) {
        return allBlank(
                metadata.companyName(),
                metadata.jobTitle(),
                metadata.location(),
                metadata.description()
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}