package pcd.ass03.part2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SudokuFactory {
   /** Genera un Sudoku completo e poi rimuove celle per creare un puzzle */
  private static final int SIZE = 9;
  private static final int SUBGRID = 3;
 
  public SudokuGrid generate(int emptyCells) {
        
      SudokuGrid newSudoku = new SudokuGrid();
      fillGrid(newSudoku);

      // copia la soluzione
      Integer[][] puzzle = deepCopy(newSudoku.getGrid());

      // rimuove celle casualmente
      Random rand = new Random();
      int removed = 0;
      while (removed < emptyCells) {
          int r = rand.nextInt(SIZE);
          int c = rand.nextInt(SIZE);
          if (puzzle[r][c] != null) {
              puzzle[r][c] = null;
              removed++;
          }
      }

      return new SudokuGrid(puzzle);
  }

  /** Riempi la griglia con un Sudoku valido (ricorsione/backtracking) */
  private boolean fillGrid(SudokuGrid sudoku) {
      Integer[][] grid = sudoku.getGrid();
      for (int row = 0; row < SIZE; row++) {
          for (int col = 0; col < SIZE; col++) {
              if (grid[row][col] == null) {
                  List<Integer> numbers = new ArrayList<>();
                  for (int i = 1; i <= SIZE; i++) numbers.add(i);
                  Collections.shuffle(numbers);

                  for (int num : numbers) {
                      if (sudoku.isValidMove(row, col, num)) {
                          grid[row][col] = num;
                          if (fillGrid(sudoku)) {
                              return true;
                          } else {
                              grid[row][col] = null;
                          }
                      }
                  }
                  return false;
              }
          }
      }
      return true; // griglia piena
  }

  /** Crea una copia profonda della griglia */
  private static Integer[][] deepCopy(Integer[][] grid) {
      Integer[][] copy = new Integer[SIZE][SIZE];
      for (int r = 0; r < SIZE; r++) {
          System.arraycopy(grid[r], 0, copy[r], 0, SIZE);
      }
      return copy;
  }
}
