package pcd.ass03.part2A;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import pcd.ass03.part2A.view.SudokuGUI;

public class Main {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        new SudokuGUI().setVisible(true);
    });
  }
}