package pcd.ass03.part2A.controller;

import java.util.HashMap;
import java.util.Map;

import pcd.ass03.part2A.model.Cell;
import pcd.ass03.part2A.model.Player;
import pcd.ass03.part2A.model.SudokuFactory;
import pcd.ass03.part2A.model.SudokuGrid;
import pcd.ass03.part2A.model.message.SelectCellMessage;
import pcd.ass03.part2A.model.message.SetValueMessage;
import pcd.ass03.part2A.model.message.UnselectCellMessage;

public class SudokuControllerImpl implements SudokuController {

  private final Player player;
  private final SudokuFactory factory;
  private final Map<String, Cell> selectedCells;

  public SudokuControllerImpl(Player player) {
    this.player = player;
    this.factory = new SudokuFactory();
    this.selectedCells = new HashMap<>();
  }

  @Override
  public SudokuGrid newGame() {
    SudokuGrid sudoku = this.factory.generate(50);
    try {
      player.createSudoku(sudoku);
      return sudoku;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void updateView() {
    // TODO chiamerà la updateView della GUI qualcosa come
    // view.updateView(player.getCurrentGridId(), selectedCells, player.getSudokusId());
  }

  @Override
  public boolean selectCell(int row, int col) {
    int gridId = player.getCurrentGridId();
    try {
      if (row == -1 || col == -1) {
        Cell previouslySelected = selectedCells.remove(player.getPlayerId());
        if (previouslySelected != null) {
          player.unselectCell(previouslySelected.row(), previouslySelected.col());
        }
        return true;
      }
      if (isAlreadySelectedCell(new Cell(row, col, gridId))) {
        return false;
      }
      selectedCells.put(player.getPlayerId(), new Cell(row, col, gridId));
      player.selectCell(row, col);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public boolean setCellValue(int value) {
    return player.tryToSetValue(value, value, value);
  }

  @Override
  public void clearCellValue() {
  }

  @Override
  public void joinGame(int sudokuId) {
    player.joinGrid(sudokuId);
  }

  @Override
  public void leaveGame() {
    player.leaveGrid();
  }

  @Override
  public void notifyCellSelected(SelectCellMessage msg) {
    selectedCells.put(msg.playerId(), new Cell(msg.row(), msg.col(), msg.sudokuId()));
    this.updateView();
  }

  @Override
  public void notifyCellUnselected(String playerId) {
    selectedCells.remove(playerId);
    this.updateView();
  }
  
  @Override
  public void notifyCellValueChanged(SetValueMessage msg) {
    this.updateView();
  }
  
  @Override
  public void notifySudokuCreated(SudokuGrid sudokuId) {
    this.updateView();
  }

  @Override
  public String getPlayerName() {
    return this.player.getPlayerName();
  }

  private boolean isAlreadySelectedCell(Cell cell) {
    for(Cell selectedCell : this.selectedCells.values()) {
      if (selectedCell.equals(cell)) { return true; }
    }
    return false;
  }
}