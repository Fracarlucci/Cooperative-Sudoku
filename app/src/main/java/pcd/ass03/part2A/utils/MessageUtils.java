package pcd.ass03.part2A.utils;

import pcd.ass03.part2A.model.SudokuGrid;
import pcd.ass03.part2A.model.message.SelectCellMessage;
import pcd.ass03.part2A.model.message.SetValueMessage;

public class MessageUtils {

  public static SudokuGrid deserializeSudokuGrid(String message) {
    String[] elements = message.split(" ");
    int sudokuId = Integer.parseInt(elements[0]);
    Integer[][] grid = new Integer[9][9];
    int index = 1;
    for (int row = 0; row < 9; row++) {
        for (int col = 0; col < 9; col++) {
            grid[row][col] = Integer.parseInt(elements[index]);
            index++;
        }
    }

    return new SudokuGrid(grid, sudokuId);
  }

  public static SelectCellMessage deserializeSelectCellMessage(String message) {
    String[] elements = message.split(" ");
    int sudokuId = Integer.parseInt(elements[0]);
    String playerId = elements[1];
    int row = Integer.parseInt(elements[2]);
    int col = Integer.parseInt(elements[3]);

    return new SelectCellMessage(sudokuId, playerId, row, col);
  }

  public static SetValueMessage deserializeSetValueMessage(String message) {
    String[] elements = message.split(" ");
    int sudokuId = Integer.parseInt(elements[0]);
    String playerId = elements[1];
    int row = Integer.parseInt(elements[2]);
    int col = Integer.parseInt(elements[3]);
    String cellValue = elements[4]; 

    return new SetValueMessage(sudokuId, playerId, row, col, cellValue);
  }

  public static String serializeSudokuGrid(int sudokuId, Integer[][] grid) {
    StringBuilder sb = new StringBuilder();
    sb.append(sudokuId).append(" ");
    for (int row = 0; row < 9; row++) {
        for (int col = 0; col < 9; col++) {
            sb.append(grid[row][col]).append(" ");
        }
    }
    return sb.toString().trim();
  }
}
