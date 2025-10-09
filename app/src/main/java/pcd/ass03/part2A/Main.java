package pcd.ass03.part2A;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import pcd.ass03.part2A.controller.SudokuControllerImpl;
import pcd.ass03.part2A.model.Player;
import pcd.ass03.part2A.model.SudokuFactory;
import pcd.ass03.part2A.model.SudokuGrid;
import pcd.ass03.part2A.view.SudokuGUI;

public class Main {
  public static void main(String[] args) {
    try {
        SudokuFactory factory = new SudokuFactory();
        SudokuGrid sudoku1 = factory.generate(10);
        SudokuGrid sudoku2 = factory.generate(15);
        
        Player p1 = new Player("p1", "player-1");
        
        Thread.sleep(200);
        
        // p1.createSudoku(sudoku1);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            SudokuGUI gui = new SudokuGUI(new SudokuControllerImpl(p1));
            gui.setVisible(true);
        });
        
        Thread.sleep(2000);
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}