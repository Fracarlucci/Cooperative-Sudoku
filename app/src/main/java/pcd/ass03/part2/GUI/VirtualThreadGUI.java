package pcd.ass03.part2.GUI;

import io.vertx.core.Vertx;
import pcd.ass03.part2.lib.WordOccurrences;
import pcd.ass03.part2.lib.virtualThread.WordOccurrencesVirtualThread;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class VirtualThreadGUI extends JFrame {
    private boolean isStopped = true;
    private JTextField webAddress, wordToFind, depth;
    private JTextArea resultArea;
    private final JButton startButton;
    private JButton stopButton;
    Vertx vertx;

    public VirtualThreadGUI() {
        setTitle("Word Occurrences");
        setSize(600, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2));

        inputPanel.add(new JLabel("Web address:"));
        webAddress = new JTextField("https://virtuale.unibo.it");
        inputPanel.add(webAddress);

        inputPanel.add(new JLabel("Word to find:"));
        wordToFind = new JTextField("virtuale");
        inputPanel.add(wordToFind);

        inputPanel.add(new JLabel("Depth:"));
        depth = new JTextField("2");
        inputPanel.add(depth);

        startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            final Set<String> pageLinks = new HashSet<>();
            Map<String, Integer> result = new HashMap<>();
            final WordOccurrences woc = new WordOccurrencesVirtualThread();
            this.isStopped = false;
            String webAddressText = webAddress.getText().isEmpty() ? "https://fracarlucci.github.io/RancorRank/" : webAddress.getText();
            String wordToFindText = wordToFind.getText().isEmpty() ? "hello" : wordToFind.getText();
            int depthText;
            try {
                depthText = Integer.parseInt(depth.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            pageLinks.add(webAddressText);
            resultArea.setText("");
            startButton.setText("Finding words...");
            startButton.setEnabled(false);
            stopButton.setEnabled(true);

            resultArea.append("\nOccurrences of \"" + wordToFindText + "\" : link\n");
            result = woc.getWordOccurences(webAddressText, wordToFindText, depthText);
            result.forEach((k, v) -> resultArea.append(v + " : " + k + "\n"));
            final int viewedLinks = result.keySet().size();
            final int wordsFound = result.values().stream().mapToInt(Integer::intValue).sum();
            resultArea.append("Links: " + viewedLinks + "\n");
            resultArea.append("Words found: " + wordsFound + "\n");
            startButton.setText("Words found!");
            stopButton.setEnabled(false);
            startButton.setEnabled(true);
            startButton.setText("Start");
            stopButton.setEnabled(false);
            stopButton.setText("Stop");

        });
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> {
            startButton.setEnabled(true);
            startButton.setText("Start");
            stopButton.setEnabled(false);
            stopButton.setText("Stop");
            this.isStopped = true;
            vertx.close();
        });
        inputPanel.add(startButton);
        inputPanel.add(stopButton);

        add(inputPanel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        Font font = new Font("Arial", Font.PLAIN, 14);
        resultArea.setFont(font);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VirtualThreadGUI gui = new VirtualThreadGUI();
            gui.setVisible(true);
        });
    }
}
