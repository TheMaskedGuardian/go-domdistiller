// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.distiller;

import org.chromium.distiller.webdocument.WebTable;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;

import java.util.Map;
import java.util.List;

public class DomUtilTest extends DomDistillerJsTestCase {
    public void testGetAttributes() {
        Element e = Document.get().createDivElement();
        e.setInnerHTML("<div style=\"width:50px; height:100px\" id=\"f\" class=\"sdf\"></div>");
        e = Element.as(e.getChildNodes().getItem(0));
        JsArray<Node> jsAttrs = DomUtil.getAttributes(e);
        assertEquals(3, jsAttrs.length());
        assertEquals("style", jsAttrs.get(0).getNodeName());
        assertEquals("width:50px; height:100px", jsAttrs.get(0).getNodeValue());
        assertEquals("id", jsAttrs.get(1).getNodeName());
        assertEquals("f", jsAttrs.get(1).getNodeValue());
        assertEquals("class", jsAttrs.get(2).getNodeName());
        assertEquals("sdf", jsAttrs.get(2).getNodeValue());
    }

    public void testGetFirstElementWithClassName() {
        Element rootDiv = TestUtil.createDiv(0);

        Element div1 = TestUtil.createDiv(1);
        div1.addClassName("abcd");
        rootDiv.appendChild(div1);

        Element div2 = TestUtil.createDiv(2);
        div2.addClassName("test");
        div2.addClassName("xyz");
        rootDiv.appendChild(div2);

        Element div3 = TestUtil.createDiv(2);
        div3.addClassName("foobar foo");
        rootDiv.appendChild(div3);

        assertEquals(div1, DomUtil.getFirstElementWithClassName(rootDiv, "abcd"));
        assertEquals(div2, DomUtil.getFirstElementWithClassName(rootDiv, "test"));
        assertEquals(div2, DomUtil.getFirstElementWithClassName(rootDiv, "xyz"));
        assertEquals(null, DomUtil.getFirstElementWithClassName(rootDiv, "bc"));
        assertEquals(null, DomUtil.getFirstElementWithClassName(rootDiv, "t xy"));
        assertEquals(null, DomUtil.getFirstElementWithClassName(rootDiv, "tes"));
        assertEquals(div3, DomUtil.getFirstElementWithClassName(rootDiv, "foo"));
    }

    public void testHasRootDomain() {
        // Positive tests.
        assertTrue(DomUtil.hasRootDomain("http://www.foo.bar/foo/bar.html", "foo.bar"));
        assertTrue(DomUtil.hasRootDomain("https://www.m.foo.bar/foo/bar.html", "foo.bar"));
        assertTrue(DomUtil.hasRootDomain("https://www.m.foo.bar/foo/bar.html", "www.m.foo.bar"));
        assertTrue(DomUtil.hasRootDomain("http://localhost/foo/bar.html", "localhost"));
        assertTrue(DomUtil.hasRootDomain("https://www.m.foo.bar.baz", "foo.bar.baz"));
        // Negative tests.
        assertFalse(DomUtil.hasRootDomain("https://www.m.foo.bar.baz", "x.foo.bar.baz"));
        assertFalse(DomUtil.hasRootDomain("https://www.foo.bar.baz", "foo.bar"));
        assertFalse(DomUtil.hasRootDomain("http://foo", "m.foo"));
        assertFalse(DomUtil.hasRootDomain("https://www.badfoobar.baz", "foobar.baz"));
        assertFalse(DomUtil.hasRootDomain("", "foo"));
        assertFalse(DomUtil.hasRootDomain("http://foo.bar", ""));
        assertFalse(DomUtil.hasRootDomain(null, "foo"));
        assertFalse(DomUtil.hasRootDomain("http://foo.bar", null));
    }

    public void testSplitUrlParams() {
        Map<String, String> result = DomUtil.splitUrlParams("param1=apple&param2=banana");
        assertEquals(2, result.size());
        assertEquals("apple", result.get("param1"));
        assertEquals("banana", result.get("param2"));

        result = DomUtil.splitUrlParams("123=abc");
        assertEquals(1, result.size());
        assertEquals("abc", result.get("123"));

        result = DomUtil.splitUrlParams("");
        assertEquals(0, result.size());

        result = DomUtil.splitUrlParams(null);
        assertEquals(0, result.size());
    }

    public void testNodeDepth() {
        Element div = TestUtil.createDiv(1);

        Element div2 = TestUtil.createDiv(2);
        div.appendChild(div2);

        Element div3 = TestUtil.createDiv(3);
        div2.appendChild(div3);

        assertEquals(2, DomUtil.getNodeDepth(div3));
    }

    public void testZeroOrNoNodeDepth() {
        Element div = TestUtil.createDiv(0);
        assertEquals(0, DomUtil.getNodeDepth(div));
        assertEquals(-1, DomUtil.getNodeDepth(null));
    }

    public void testGetOutputNodes() {
        Element div = Document.get().createDivElement();
        String html = "<p>" +
                          "<span>Some content</span>" +
                          "<img src=\"./image.png\">" +
                      "</p>";
        div.setInnerHTML(html);
        mBody.appendChild(div);

        List<Node> contentNodes = DomUtil.getOutputNodes(div);

        // Expected nodes: <div><p><span>#text<img>.
        assertEquals(5, contentNodes.size());

        Node n = contentNodes.get(0);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("DIV", Element.as(n).getNodeName());

        n = contentNodes.get(1);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("P", Element.as(n).getNodeName());

        n = contentNodes.get(2);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("SPAN", Element.as(n).getNodeName());

        n = contentNodes.get(3);
        assertEquals(Node.TEXT_NODE, n.getNodeType());

        n = contentNodes.get(4);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("IMG", Element.as(n).getNodeName());
    }

    public void testGetOutputNodesWithHiddenChildren() {
        Element table = Document.get().createTableElement();
        String html = "<tbody>" +
                          "<tr>" +
                              "<td>row1col1</td>" +
                              // Since the <img> is hidden, it should not be included in the final
                              // output.
                              "<td><img src=\"./table.png\" style=\"display:none\"></td>" +
                          "</tr>" +
                      "</tbody>";
        table.setInnerHTML(html);
        mBody.appendChild(table);
        WebTable webTable = new WebTable(table);

        List<Node> contentNodes = DomUtil.getOutputNodes(webTable.getTableElement());

        // Expected nodes: <table><tbody><tr><td>#text<td>.
        assertEquals(6, contentNodes.size());

        Node n = contentNodes.get(0);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TABLE", Element.as(n).getNodeName());

        n = contentNodes.get(1);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TBODY", Element.as(n).getNodeName());

        n = contentNodes.get(2);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TR", Element.as(n).getNodeName());

        n = contentNodes.get(3);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TD", Element.as(n).getNodeName());
        n = contentNodes.get(4);
        assertEquals("#text", n.getNodeName());
        assertEquals("row1col1", n.getNodeValue());
        n = contentNodes.get(5);
        assertEquals(Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("TD", Element.as(n).getNodeName());
    }

    public void testGetOutputNodesNestedTable() {
        Element table = Document.get().createTableElement();
        String html = "<tbody><tr>" +
            "<td><table><tbody><tr><td>nested</td></tr></tbody></table></td>" +
            "<td>outer</td>" +
            "</tr></tbody>";
        table.setInnerHTML(html);
        mBody.appendChild(table);
        WebTable webTable = new WebTable(table);

        List<Node> contentNodes = DomUtil.getOutputNodes(webTable.getTableElement());

        assertEquals(11, contentNodes.size());
    }

    public void testGetSrcSetUrls() {
        String html =
            "<img src=\"http://example.com/image\" " +
              "srcset=\"http://example.com/image200 200w, http://example.com/image400 400w\">";

        mBody.setInnerHTML(html);
        List<String> list = DomUtil.getSrcSetUrls((ImageElement) mBody.getChild(0));
        assertEquals(2, list.size());
        assertEquals("http://example.com/image200", list.get(0));
        assertEquals("http://example.com/image400", list.get(1));
    }

    public void testGetAllSrcSetUrls() {
        String html =
            "<picture>" +
                "<source srcset=\"image200 200w, //example.org/image400 400w\">" +
                "<source srcset=\"image100 100w, //example.org/image300 300w\">" +
                "<img>" +
            "</picture>";
        Element container = Document.get().createDivElement();
        container.setInnerHTML(html);
        List<String> urls = DomUtil.getAllSrcSetUrls(container);
        assertEquals(4, urls.size());
        assertEquals("image200", urls.get(0));
        assertEquals("//example.org/image400", urls.get(1));
        assertEquals("image100", urls.get(2));
        assertEquals("//example.org/image300", urls.get(3));
    }

    public void testStripImageElements() {
        String html =
            "<img id=\"a\" alt=\"alt\" dir=\"rtl\" title=\"t\" style=\"typo\" align=\"left\"" +
                    "src=\"image\" class=\"a\" srcset=\"image200 200w\" data-dummy=\"a\">" +
            "<img mulformed=\"nothing\" data-empty data-dup=\"1\" data-dup=\"2\"" +
                    "src=\"image\" src=\"second\">";

        final String expected =
            "<img alt=\"alt\" dir=\"rtl\" title=\"t\" src=\"image\" srcset=\"image200 200w\">" +
            "<img src=\"image\">";

        // Test if the root element is handled properly.
        mBody.setInnerHTML(html);
        for (int i = 0; i < mBody.getChildCount(); i++) {
            DomUtil.stripImageElements(mBody.getChild(i));
        }
        assertEquals(expected, mBody.getInnerHTML());

        mBody.setInnerHTML(html);
        DomUtil.stripImageElements(mBody);
        assertEquals(expected, mBody.getInnerHTML());
    }

    public void testIsVisibleByOffsetParentDisplayNone() {
        String html =
            "<div style=\"display: none;\">" +
                "<div></div>" +
            "</div>";
        mBody.setInnerHTML(html);
        Element child = mBody.getFirstChildElement().getFirstChildElement();
        assertFalse(DomUtil.isVisibleByOffset(child));
    }

    public void testIsVisibleByOffsetChildDisplayNone() {
        String html =
            "<div>" +
                "<div style=\"display: none;\"></div>" +
            "</div>";
        mBody.setInnerHTML(html);
        Element child = mBody.getFirstChildElement().getFirstChildElement();
        assertFalse(DomUtil.isVisibleByOffset(child));
    }

    public void testIsVisibleByOffsetDisplayBlock() {
        String html =
            "<div>" +
                "<div></div>" +
            "</div>";
        mBody.setInnerHTML(html);
        Element child = mBody.getFirstChildElement().getFirstChildElement();
        assertTrue(DomUtil.isVisibleByOffset(child));
    }

    public void testOnlyProcessArticleElement() {
        final String htmlArticle =
            "<h1></h1>" +
            "<article>a</article>";

        String expected = "<article>a</article>";

        Element result = getArticleElement(htmlArticle);
        assertEquals(expected, result.getString());
    }

    public void testOnlyProcessArticleElementWithHiddenArticleElement() {
        final String htmlArticle =
            "<h1></h1>" +
            "<article>a</article>" +
            "<article style=\"display:none\">b</article>";

        String expected = "<article>a</article>";

        Element result = getArticleElement(htmlArticle);
        assertEquals(expected, result.getString());
    }

    public void testOnlyProcessArticleElementWithZeroAreaElement() {
        final String htmlArticle =
                "<h1></h1>" +
                        "<article>a</article>" +
                        "<article style=\"width: 0px\">b</article>";

        String expected = "<article>a</article>";

        Element result = getArticleElement(htmlArticle);
        assertEquals(expected, result.getString());
    }

    public void testOnlyProcessArticleElementMultiple() {
        final String htmlArticle =
            "<h1></h1>" +
            "<article>a</article>" +
            "<article>b</article>";

        // The existence of multiple articles disables the fast path.
        assertNull(getArticleElement(htmlArticle));
    }

    public void testOnlyProcessSchemaOrgArticle() {
        final String htmlArticle =
            "<h1></h1>" +
            "<div itemscope itemtype=\"http://schema.org/Article\">a" +
            "</div>";

        final String expected =
            "<div itemscope=\"\" " +
                "itemtype=\"http://schema.org/Article\">a" +
            "</div>";

        Element result = getArticleElement(htmlArticle);
        assertEquals(expected, result.getString());
    }

    public void testOnlyProcessSchemaOrgArticleWithHiddenArticleElement() {
        final String htmlArticle =
            "<h1></h1>" +
            "<div itemscope itemtype=\"http://schema.org/Article\">a" +
            "</div>" +
            "<div itemscope itemtype=\"http://schema.org/Article\" " +
                "style=\"display:none\">b" +
            "</div>";

        String expected =
            "<div itemscope=\"\" itemtype=\"http://schema.org/Article\">a" +
            "</div>";

        Element result = getArticleElement(htmlArticle);
        assertEquals(expected, result.getString());
    }

    public void testOnlyProcessSchemaOrgArticleNews() {
        final String htmlArticle =
            "<h1></h1>" +
            "<div itemscope itemtype=\"http://schema.org/NewsArticle\">a" +
            "</div>";

        final String expected =
            "<div itemscope=\"\" " +
                "itemtype=\"http://schema.org/NewsArticle\">a" +
            "</div>";

        Element result = getArticleElement(htmlArticle);
        assertEquals(expected, result.getString());
    }

    public void testOnlyProcessSchemaOrgArticleBlog() {
        final String htmlArticle =
            "<h1></h1>" +
            "<div itemscope itemtype=\"http://schema.org/BlogPosting\">a" +
            "</div>";

        final String expected =
            "<div itemscope=\"\" " +
                "itemtype=\"http://schema.org/BlogPosting\">a" +
            "</div>";

        Element result = getArticleElement(htmlArticle);
        assertEquals(expected, result.getString());
    }

    public void testOnlyProcessSchemaOrgPostal() {
        final String htmlArticle =
            "<h1></h1>" +
            "<div itemscope itemtype=\"http://schema.org/PostalAddress\">a" +
            "</div>";

        Element result = getArticleElement(htmlArticle);
        assertNull(result);
    }

    public void testOnlyProcessSchemaOrgArticleNested() {
        final String htmlArticle =
            "<h1></h1>" +
            "<div itemscope itemtype=\"http://schema.org/Article\">a" +
                "<div itemscope itemtype=\"http://schema.org/Article\">b" +
                "</div>" +
            "</div>";

        final String expected =
            "<div itemscope=\"\" itemtype=\"http://schema.org/Article\">a" +
                "<div itemscope=\"\" itemtype=\"http://schema.org/Article\">b" +
               "</div>" +
            "</div>";

        Element result = getArticleElement(htmlArticle);
        assertEquals(expected, result.getString());
    }

    public void testOnlyProcessSchemaOrgArticleNestedWithNestedHiddenArticleElement() {
        final String htmlArticle =
            "<h1></h1>" +
            "<div itemscope itemtype=\"http://schema.org/Article\">a" +
                "<div itemscope itemtype=\"http://schema.org/Article\">b" +
                "</div>" +
                "<div itemscope itemtype=\"http://schema.org/Article\" " +
                    "style=\"display:none\">c" +
                "</div>" +
            "</div>";

        final String expected =
            "<div itemscope=\"\" itemtype=\"http://schema.org/Article\">a" +
                "<div itemscope=\"\" itemtype=\"http://schema.org/Article\">b" +
                "</div>" +
                "<div itemscope=\"\" itemtype=\"http://schema.org/Article\" " +
                    "style=\"display:none\">c" +
                "</div>" +
            "</div>";

        Element result = getArticleElement(htmlArticle);
        assertEquals(expected, result.getString());
    }

    public void testOnlyProcessSchemaOrgArticleNestedWithHiddenArticleElement() {
        final String paragraph = "<p></p>";

        final String htmlArticle =
            "<h1></h1>" +
            "<div itemscope itemtype=\"http://schema.org/Article\">a" +
                "<div itemscope itemtype=\"http://schema.org/Article\">b" +
                "</div>" +
            "</div>" +
            "<div itemscope itemtype=\"http://schema.org/Article\" " +
                "style=\"display:none\">c" +
            "</div>";

        final String expected =
            "<div itemscope=\"\" itemtype=\"http://schema.org/Article\">a" +
                "<div itemscope=\"\" itemtype=\"http://schema.org/Article\">b" +
                "</div>" +
            "</div>";

        Element result = getArticleElement(htmlArticle);
        assertEquals(expected, result.getString());
    }

    public void testOnlyProcessSchemaOrgNonArticleMovie() {
        final String htmlArticle =
            "<h1></h1>" +
            "<div itemscope itemtype=\"http://schema.org/Movie\">a" +
            "</div>";

        // Non-article schema.org types should not use the fast path.
        Element result = getArticleElement(htmlArticle);
        assertNull(result);
    }

    private Element getArticleElement(String html) {
        mBody.setInnerHTML(html);
        return DomUtil.getArticleElement(mRoot);
    }

    public void testGetArea() {
        String elements =
            "<div style=\"width: 200px; height: 100px\">w</div>" +
            "<div style=\"width: 300px;\">" +
                "<div style=\"width: 300px; height: 200px\"></div>" +
            "</div>" +
            "<div style=\"width: 400px; height: 100px\">" +
                "<div style=\"height: 100%\"></div>" +
            "</div>";
        mBody.setInnerHTML(elements);

        Element element = mBody.getFirstChildElement();
        assertEquals(200*100, DomUtil.getArea(element));

        element = element.getNextSiblingElement();
        assertEquals(300*200, DomUtil.getArea(element));

        element = element.getNextSiblingElement();
        assertEquals(400*100, DomUtil.getArea(element));

        element = element.getFirstChildElement();
        assertEquals(400*100, DomUtil.getArea(element));
    }
}
