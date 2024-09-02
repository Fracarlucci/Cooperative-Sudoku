package pcd.ass03.part2.lib.eventLoop;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VerticleFinder extends AbstractVerticle {

    private final Map<String, Integer> map = new HashMap<>();
    private final Set<String> pageLinks = new HashSet<>();
    private final Set<String> failedPages = new HashSet<>();
    private final int depth;
    private final String webAddress;
    private int pageToVisit = 1;
    private final String wordToFind;
    private final Consumer<Map<String, Integer>> result;
    private final boolean isStopped;

    public VerticleFinder(final String webAddress, final String wordToFind, final int depth, final boolean isStopped, Consumer<Map<String, Integer>> result) {
        this.wordToFind = wordToFind;
        this.depth = depth;
        this.webAddress = webAddress;
        this.result = result;
        this.isStopped = isStopped;
    }

    public void start(final Promise<Void> promise) throws IOException {
        int actualDepth = 0;
        pageLinks.add(webAddress);
        log("START");
        this.getVertx().eventBus().consumer("word-found", message -> {
            computeWordFound(message.body().toString());
        });
        this.findWord(webAddress, promise, actualDepth);
    }

    private void computeWordFound(final String webAddress) {
        this.map.put(webAddress, this.map.get(webAddress) == null ? 1 : this.map.get(webAddress) + 1);
        result.accept(map);
    }

    /*
     * @param webAddress where to find the word
     * @throws IOException if there are errors reading the web page
     */
    private void findWord(final String webAddress, final Promise<Void> promise, int actualDepth) throws IOException {
        Callable<Document> call = () -> {
            if (this.isStopped) {
                return null;
            }
            try {
                return Jsoup.connect(webAddress).get();
            } catch (Exception e) {
                this.failedPages.add(webAddress);
                log("Can't load the page! " + e);
                return null;
            }
        };

        getVertx().executeBlocking(call)
                .onComplete(res -> {
                    try {
                        pageToVisit--;
                        Document doc = res.result();
                        if (doc == null) {
                            return;
                        }
                        Elements elements = doc.body().select("*");
                        for (Element element : elements) {
                            if (Pattern.compile(Pattern.quote(wordToFind), Pattern.CASE_INSENSITIVE).matcher(element.ownText()).find()) {
                                String[] words = element.ownText().split("[\\s\\p{Punct}]+");
                                for (String word : words) {
                                    if (word.equalsIgnoreCase(wordToFind)) {
                                        this.getVertx().eventBus().publish("word-found", webAddress);
                                    }
                                }
                            }
                        }
                        if (actualDepth < depth) {
                            findLinks(doc, promise, actualDepth + 1);
                        }
                    } finally {
                        if (pageToVisit == 0 && actualDepth == depth) {
                            promise.complete();
                        }
                    }
                });
    }

    /**
     * @param doc         the html page
     * @param promise
     * @param actualDepth the current depth of the recursion
     */
    public void findLinks(final Document doc, final Promise<Void> promise, final int actualDepth) {
        var newLinks = doc.getElementsByTag("a")
                .stream()
                .map(l -> l.attr("href"))
                .filter(l -> l.startsWith("http"))
                .collect(Collectors.toSet());
        this.pageLinks.clear();
        this.pageLinks.addAll(newLinks);

        pageToVisit = pageLinks.size();
        this.pageLinks.stream()
                .filter(l -> !failedPages.contains(l) && !map.containsKey(l))
                .forEach(pl -> {
                    try {
                        findWord(pl, promise, actualDepth);
                    } catch (IOException e) {
                        log("Can't load the page! " + e);
                    }
                });
    }


    private void log(final String str) {
        System.out.println("[MAIN LOOP]: " + str);
    }
}
