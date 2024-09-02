package pcd.ass02.part2.lib.virtualThread;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class LinkFinderFuture implements Callable<Set<String>> {

  private final String webAddress;
  private Document doc;
  private Set<String> links;

  public LinkFinderFuture(String webAddress) {
    this.webAddress = webAddress;
  }

  /**
   * Implements link finder
   * @return a Set of links found
   */
  @Override
  public Set<String> call() {
    try {
      doc = Jsoup.connect(webAddress).get();
    } catch (Exception e) {
      System.out.println("Failed to connect to the website");
    }
    return findLinks();
  }

  private Set<String> findLinks() {
    return this.doc.getElementsByTag("a")
            .stream()
            .map(l -> l.attr("href"))
            .filter(l -> l.startsWith("http"))
            .collect(Collectors.toSet());
  }
}
