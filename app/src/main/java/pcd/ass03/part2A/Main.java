package pcd.ass03.part2A;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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
        Player p2 = new Player("p2", "player-2");
        
        Thread.sleep(200);
        
        p1.createSudoku(sudoku1);
        p2.createSudoku(sudoku2);
        
        Thread.sleep(2000);
        
        System.out.println("Sudoku 1 ID: " + p1.getSudokusId());
        System.out.println("Sudoku 2 ID: " + p2.getSudokusId());

        p1.joinGrid(sudoku1.getId());
        p1.selectCell(1, 1);
        p2.joinGrid(sudoku2.getId());
        p2.selectCell(4, 7);
        p1.setValue(5);
        p1.selectCell(3, 3);
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}