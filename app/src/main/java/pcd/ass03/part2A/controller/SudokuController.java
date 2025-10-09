package pcd.ass03.part2A.controller;

import pcd.ass03.part2A.model.SudokuGrid;
import pcd.ass03.part2A.model.message.SelectCellMessage;
import pcd.ass03.part2A.model.message.SetValueMessage;

public interface SudokuController {
  public boolean selectCell(int row, int col);
  public boolean setCellValue(int value);
  public void clearCellValue();
  public void joinGame(int sudokuId);
  public void leaveGame();
  public SudokuGrid newGame();
  public void updateView();
  public void notifyCellSelected(SelectCellMessage msg);
  public void notifyCellUnselected(String playerId);
  public void notifyCellValueChanged(SetValueMessage msg);
  public void notifySudokuCreated(SudokuGrid sudokuId);
  public String getPlayerName();
}
