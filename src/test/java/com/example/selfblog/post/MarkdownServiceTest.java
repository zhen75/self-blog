package com.example.selfblog.post;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.example.selfblog.post.utils.MarkdownService;

class MarkdownServiceTest {
    private final MarkdownService markdownService = new MarkdownService();

    @Test
    void rendersCommonMarkdownAndGfmExtensions() {
        String markdown = "# Title\n\n**bold** and ~~deleted~~\n\n| A | B |\n|---|---|\n| 1 | 2 |";

        String html = markdownService.toHtml(markdown);

        assertTrue(html.contains("<h1>Title</h1>"));
        assertTrue(html.contains("<strong>bold</strong>"));
        assertTrue(html.contains("<del>deleted</del>"));
        assertTrue(html.contains("<table>"));
    }

    @Test
    void doesNotAllowRawHtmlOrUnsafeUrls() {
        String html = markdownService.toHtml("<script>alert('xss')</script>\n\n[bad](javascript:alert(1))");

        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("&lt;script&gt;"));
        assertFalse(html.contains("href=\"javascript:"));
    }

    @Test
    void createsPlainTextForArticleSummary() {
        String text = markdownService.toPlainText("## Heading\n\nA **Markdown** [link](https://example.com). ");

        assertTrue(text.contains("Heading"));
        assertTrue(text.contains("A Markdown"));
        assertTrue(text.contains("link"));
        assertFalse(text.contains("**"));
    }
}
