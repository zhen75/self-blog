package com.example.selfblog.post.utils;

import java.util.List;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.stereotype.Service;

@Service
public class MarkdownService {
    private static final List<Extension> EXTENSIONS = List.of(
            TablesExtension.create(),
            StrikethroughExtension.create());

    private final Parser parser = Parser.builder().extensions(EXTENSIONS).build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder()
            .extensions(EXTENSIONS)
            .escapeHtml(true)
            .sanitizeUrls(true)
            .build();
    private final TextContentRenderer textRenderer = TextContentRenderer.builder().build();

    public String toHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        return htmlRenderer.render(parser.parse(markdown));
    }

    public String toPlainText(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        Node document = parser.parse(markdown);
        return textRenderer.render(document).strip();
    }
}
