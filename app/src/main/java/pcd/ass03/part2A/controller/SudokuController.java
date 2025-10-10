package pcd.ass03.part2A.controller;

import pcd.ass03.part2A.model.PlayerInfo;
import pcd.ass03.part2A.model.SudokuGrid;
import pcd.ass03.part2A.model.message.SelectCellMessage;
import pcd.ass03.part2A.model.message.SetValueMessage;
import pcd.ass03.part2A.model.message.UnselectCellMessage;

public interface SudokuController {
  public boolean selectCell(int row, int col);
  public boolean setCellValue(int row, int col, int value);
  public void clearCellValue();
  public void joinGame(String sudokuId);
  public void leaveGame();
  public SudokuGrid newGame();
  public void updateView();
  public void notifyCellSelected(SelectCellMessage msg);
  public void notifyCellUnselected(UnselectCellMessage msg);
  public void notifyCellValueChanged(SetValueMessage msg);
  public void notifySudokuCreated(SudokuGrid sudokuId, String playerName);
  public PlayerInfo getCurrentPlayerInfo();
}
