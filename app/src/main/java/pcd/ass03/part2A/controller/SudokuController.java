package pcd.ass03.part2A.controller;

import pcd.ass03.part2A.model.Player;

public interface SudokuController {
  public boolean selectCell(int row, int col);
  public boolean setCellValue(int value);
  public void clearCellValue();
  public void joinGame(String sudokuId);
  public void leaveGame();
  public void newGame();
  public void updateView();
}
