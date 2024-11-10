package blog.article1.e4_multiple_outputs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class E4_MultipleOutputTypes {

    public static void main(String[] args) {
        timed(() -> {
            var outputType = args.length == 1 ? args[0].toLowerCase() : "";
            var searchQuery = "iphone 13";
            var cities = List.of("newyork", "boston", "washingtondc", "losangeles", "chicago", "sanfrancisco", "seattle", "miami", "dallas", "denver");

            var results = fetchCities(cities, searchQuery);

            switch (outputType) {
                case "json" -> asJson(results);
                case "csv" -> asCsv(results);
                default -> System.out.println("unknown output type");
            }
        });
    }

    private static void timed(Runnable action) {
        var start = System.currentTimeMillis();
        action.run();
        var end = System.currentTimeMillis();
        System.out.printf("time = %dms%n", end - start);
    }

    private static void asJson(Map<String, List<Item>> results) {
        var objectMapper = new ObjectMapper();
        try {
            System.out.println(objectMapper.writeValueAsString(results));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void asCsv(Map<String, List<Item>> results) {
        System.out.println("city,title,price,location,url");
        for (Map.Entry<String, List<Item>> entry : results.entrySet()) {
            for (Item item : entry.getValue()) {
                System.out.printf("%s,%s,%s,%s,%s%n", entry.getKey(), item.title, item.price, item.location, item.url);
            }
        }
    }

    private static Map<String, List<Item>> fetchCities(List<String> cities, String searchQuery) {
        try (var client = new WebClient()) {
            client.getOptions().setCssEnabled(false);
            client.getOptions().setJavaScriptEnabled(false);
            client.getOptions().setThrowExceptionOnFailingStatusCode(false);
            client.getOptions().setThrowExceptionOnScriptError(false);

            Map<String, List<Item>> items = new HashMap<>();

            for (String city : cities) {
                var searchUrl = "https://%s.craigslist.org/search/moa?query=%s".formatted(city, URLEncoder.encode(searchQuery, StandardCharsets.UTF_8));

                System.out.println("searchUrl = " + searchUrl);

                try {
                    HtmlPage page = client.getPage(searchUrl);
                    for (var htmlItem : page.<HtmlElement>getByXPath("//li[contains(@class,'cl-static-search-result')]")) {
                        HtmlAnchor itemAnchor = htmlItem.getFirstByXPath(".//a");
                        HtmlElement itemTitle = htmlItem.getFirstByXPath(".//div[@class='title']");
                        HtmlElement itemPrice = htmlItem.getFirstByXPath(".//div[@class='price']");
                        HtmlElement itemLocation = htmlItem.getFirstByXPath(".//div[@class='location']");

                        if (itemAnchor != null && itemTitle != null) {
                            var itemName = itemTitle.asNormalizedText();
                            var itemUrl = itemAnchor.getHrefAttribute();
                            var itemPriceText = itemPrice.asNormalizedText();
                            var itemLocationText = (itemLocation == null) ? "N/A" : itemLocation.asNormalizedText();

                            items.computeIfAbsent(city, _ -> new ArrayList<>())
                              .add(new Item(itemName, new BigDecimal(itemPriceText.replace("$", "")
                                .replace(",", ".")), itemLocationText, itemUrl));
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return items;
        }
    }

    record Item(String title, BigDecimal price, String location, String url) {
    }


}
