package pcd.ass02.part2.lib;

import java.util.Map;

/**
 * Interface that, given a web address, a word, and a value, provides a report
 * containing the list of pages that contain that word and the occurrences of
 * the word in the pages, starting from the specified address and considering
 * the linked pages, recursively, to a depth level equal to the value specified.
 */
public interface WordOccurrences {

    /**
     *
     * @param webAddress web address from which the research starts
     * @param wordToFind word to be found
     * @param depth level of the recursion
     * @return a map whose keys are the names of the pages where the word was
     * found and whose values are the number of occurrences of the word on that page
     */
    Map<String, Integer> getWordOccurences(String webAddress, String wordToFind, int depth);
}