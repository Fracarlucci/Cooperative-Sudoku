package pcd.ass03.part2A.controller;

import pcd.ass03.part2A.model.Player;

public interface SudokuController {
  public boolean selectCell(Player player, int row, int col);
  public boolean setCellValue(Player player, int value);
  public void clearCellValue(Player player);
  public void joinGame(Player player);
  public void leaveGame(Player player);
  public void newGame();
  public void updateView();
}
