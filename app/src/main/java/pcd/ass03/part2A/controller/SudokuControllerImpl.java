package pcd.ass03.part2A.controller;

import java.util.HashMap;
import java.util.Map;

import pcd.ass03.part2A.model.Cell;
import pcd.ass03.part2A.model.Player;
import pcd.ass03.part2A.model.SudokuFactory;
import pcd.ass03.part2A.model.SudokuGrid;

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
  public void newGame() {
    SudokuGrid sudoku = this.factory.generate(50);
    try {
      player.createSudoku(sudoku);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void updateView() {
  }

  @Override
  public boolean selectCell(int row, int col) {
    try {
      if (row == -1 || col == -1) {
        Cell previouslySelected = selectedCells.remove(player.getPlayerId());
        if (previouslySelected != null) {
          player.unselectCell(previouslySelected.row(), previouslySelected.col());
        }
        return true;
      }
      if (isAlreadySelectedCell(new Cell(row, col))) {
        return false;
      }
      selectedCells.put(player.getPlayerId(), new Cell(row, col));
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
  public void joinGame(String sudokuId) {
    player.joinGrid(sudokuId);
  }

  @Override
  public void leaveGame() {
    player.leaveGrid();
  }

  private boolean isAlreadySelectedCell(Cell cell) {
    for(Cell selectedCell : this.selectedCells.values()) {
      if (selectedCell.equals(cell)) { return true; }
    }
    return false;
  }
}