package pcd.ass03.part2.lib.eventLoop;

import io.vertx.core.*;

import java.util.*;

public class WordOccurrencesEventLoop extends AbstractVerticle {

    private final Map<String, Integer> result = new HashMap<>();

    public void getWordOccurences(final String webAddress, final String wordToFind, final int depth) {
        Vertx.vertx().deployVerticle(new VerticleFinder(webAddress, wordToFind, depth, false, res -> {
                    result.putAll(res);
                    System.out.println(result.values().stream().mapToInt(Integer::intValue).sum() + " occurrences found");
                }))
                .onComplete(res -> {
                    System.out.println("*************** REPORT ***************");
                    System.out.println("Occurrences of \"" + wordToFind + "\" : link");
                    result.forEach((k, v) -> System.out.println(v + " : " + k));
                    System.out.println("Links: " + result.keySet().size());
                    System.out.println("Words found: " + result.values().stream().mapToInt(Integer::intValue).sum());
                    System.exit(0);
                });
    }
}
