package blog.article1.e1_plain;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

class E1_Plain {

    public static void main(String[] args) throws IOException {
        var searchQuery = "iphone 13";
        var searchUrl = "https://newyork.craigslist.org/search/moa?query=%s".formatted(URLEncoder.encode(searchQuery, StandardCharsets.UTF_8));

        System.out.println("searchUrl = " + searchUrl);

        try (var client = new WebClient()) {
            client.getOptions().setCssEnabled(false);
            client.getOptions().setJavaScriptEnabled(false);
            client.getOptions().setThrowExceptionOnFailingStatusCode(false);
            client.getOptions().setThrowExceptionOnScriptError(false);

            HtmlPage page = client.getPage(searchUrl);
            for (var htmlItem : page.<HtmlElement>getByXPath("//li[contains(@class,'cl-static-search-result')]")) {
                HtmlAnchor itemAnchor = htmlItem.getFirstByXPath(".//a");
                HtmlElement itemTitle = htmlItem.getFirstByXPath(".//div[@class='title']");
                HtmlElement itemPrice = htmlItem.getFirstByXPath(".//div[@class='price']");
                HtmlElement itemLocation = htmlItem.getFirstByXPath(".//div[@class='location']");

                if (itemAnchor != null && itemTitle != null) {
                    System.out.printf("Name: %s, Price: %s, Location: %s, URL: %s%n", itemTitle.asNormalizedText(), itemPrice.asNormalizedText(), (itemLocation == null) ? "N/A" : itemLocation.asNormalizedText(), itemAnchor.getHrefAttribute());
                }
            }
        }
    }
}
