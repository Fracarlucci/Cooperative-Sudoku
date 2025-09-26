package pcd.ass03.part2A;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import pcd.ass03.part2A.model.Player;
import pcd.ass03.part2A.model.SudokuFactory;
import pcd.ass03.part2A.model.SudokuGrid;
import pcd.ass03.part2A.view.SudokuGUI;

public class Main {
  public static void main(String[] args) {
    // SwingUtilities.invokeLater(() -> {
    //     try {
    //         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
        
    //     new SudokuGUI().setVisible(true);
    // });
    try {
      SudokuFactory factory = new SudokuFactory();
      SudokuGrid sudoku1 = factory.generate(10);
      SudokuGrid sudoku2 = factory.generate(15);
      Player p1 = new Player("p1", "player-1");
      Player p2 = new Player("p2", "player-2");

      p1.createSudoku(sudoku1);
      System.out.println("Sudoku 1 ID: " + p1.getSudokusId());
      p2.createSudoku(sudoku2);
      System.out.println("Sudoku 2 ID: " + p1.getSudokusId());

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}