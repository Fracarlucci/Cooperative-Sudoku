package pcd.ass03.part2B.controller;

import pcd.ass03.part2B.model.PlayerInfo;
import pcd.ass03.part2B.model.SudokuGrid;
import pcd.ass03.part2B.model.message.SelectCellMessage;
import pcd.ass03.part2B.model.message.SetValueMessage;
import pcd.ass03.part2B.model.message.UnselectCellMessage;
import pcd.ass03.part2B.view.SudokuGUI;

public interface SudokuController {
    public boolean selectCell(int row, int col);
    public void setView(SudokuGUI view);
    public boolean setCellValue(int row, int col, int value);
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
