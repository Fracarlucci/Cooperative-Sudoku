package pcd.ass03.part2A.utils;

import pcd.ass03.part2A.model.SudokuGrid;

public class MessageUtils {

  public static SudokuGrid deserializeSudokuGrid(String message) {
    String[] parts = message.split(" ");
    int sudokuId = Integer.parseInt(parts[0]);
    Integer[][] grid = new Integer[9][9];
    int index = 1;
    for (int row = 0; row < 9; row++) {
        for (int col = 0; col < 9; col++) {
            grid[row][col] = Integer.parseInt(parts[index]);
            index++;
        }
    }

    return new SudokuGrid(grid, sudokuId);
  }
}
