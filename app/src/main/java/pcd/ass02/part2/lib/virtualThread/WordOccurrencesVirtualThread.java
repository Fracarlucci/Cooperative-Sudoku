package pcd.ass02.part2.lib.virtualThread;

import pcd.ass02.part2.lib.WordOccurrences;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class WordOccurrencesVirtualThread implements WordOccurrences {

  private final Map<String, Integer> map = new HashMap<>();
  private final Set<String> pageLinks = new HashSet<>();

  @Override
  public Map<String, Integer> getWordOccurences(final String webAddress, final String wordToFind, final int depth) {
    pageLinks.add(webAddress);
    final List<Future<Map.Entry<String, Integer>>> wordFutureList = new ArrayList<>();
    final List<Future<Set<String>>> linkFutureList = new ArrayList<>();
    final Set<String> failedPages = new HashSet<>();
    Set<String> availablePages;
    Set<String> foundLinks = new HashSet<>();


    for (int i = 0; i <= depth; i++) {
      final ExecutorService executor = Executors.newFixedThreadPool(pageLinks.size());

      pageLinks.forEach(page -> {
        try {
          linkFutureList.add(executor.submit(new LinkFinderFuture(page)));
        } catch (Exception e) {
          failedPages.add(page);
        }
      });

      linkFutureList.forEach(future -> {
        try {
          final Set<String> links = future.get();
          System.out.println(future + ": link trovati = " + links.size());
          foundLinks.addAll(links);
        } catch (Exception e) {
          System.out.println("Failed to run future");
        }
      });

      pageLinks.clear();
      pageLinks.addAll(foundLinks);
      executor.shutdown();
    }

    // I need to add again the first address because pageLink.clear() delete it
    pageLinks.add(webAddress);
    availablePages = pageLinks.stream().filter(l -> !failedPages.contains(l)).collect(Collectors.toSet());
    final ExecutorService executor = Executors.newFixedThreadPool(availablePages.size());

    availablePages.forEach(page -> {
      wordFutureList.add(executor.submit(new WordFinderFuture(wordToFind, page)));
    });

    wordFutureList.forEach(future -> {
      try {
        final Map.Entry<String, Integer> entry = future.get();
        if (entry.getValue() != 0) {
          System.out.println("Future n. " + future + ": " + entry.getValue());
          map.put(entry.getKey(), entry.getValue());
        }
      } catch (Exception e) {
        System.out.println(e);
      }
    });

    // It shutdown all virtual threads when map is ready
    executor.shutdown();
    return getMap();
  }

  public Map<String, Integer> getMap() {
    final Map<String, Integer> map = this.map;
    return map;
  }
}
