package pcd.ass03.part2B.controller;

import java.util.HashMap;
import java.util.Map;

import pcd.ass03.part2B.model.Cell;
import pcd.ass03.part2B.model.Player;
import pcd.ass03.part2B.model.PlayerInfo;
import pcd.ass03.part2B.model.SudokuFactory;
import pcd.ass03.part2B.model.SudokuGrid;
import pcd.ass03.part2B.view.SudokuGUI;

public class SudokuControllerImpl implements SudokuController {
    
    private final Player player;
    private final SudokuFactory factory;
    private final Map<String, Cell> selectedCells;
    private SudokuGUI view;
    
    public SudokuControllerImpl(Player player) {
        this.player = player;
        this.factory = new SudokuFactory();
        this.selectedCells = new HashMap<>();
        this.view = null;
    }
    
    public void setView(SudokuGUI view) {
        this.view = view;
    }
    
    @Override
    public SudokuGrid newGame() {
        try {
            return player.createSudoku();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public void updateView(String gridId, SudokuGrid sudoku) {
        if (view != null) {
            view.updateView(gridId, sudoku.getSelectedCells(), sudoku);
        }
    }

    @Override
    public void updateSudokuList(String sudokuId, String creator) {
        view.addGame(sudokuId, creator);
        // view.updateSudokuList(sudokuId, creator);
    }
    
    // Select cell, if row or col is -1
    // player will unselect the cell
    @Override
    public boolean selectCell(int row, int col) {
        String gridId = player.getCurrentGridId();
        try {
            // if (row == -1 || col == -1) {
            //     Cell previouslySelected = selectedCells.remove(player.getPlayerId());
            //     if (previouslySelected != null) {
            //         player.unselectCell(previouslySelected.row(), previouslySelected.col());
            //     }
            //     return true;
            // }
            if (isAlreadySelectedCell(new Cell(row, col))) {
                return false;
            }
            // selectedCells.put(player.getPlayerId(), new Cell(row, col));
            player.selectCell(row, col);
            // this.updateView();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean setCellValue(int row, int col, int value) {
        if (player.tryToSetValue(row, col, value)) {
            // this.updateView();
            return true;
        }
        return false;
    }
    
    @Override
    public void joinGame(String sudokuId) {
        player.joinGrid(sudokuId);
        // this.updateView();
    }
    
    // Leave game and unselect player cell
    @Override
    public void leaveGame() {
        try {
            player.unselectCell(player.getSelectedRow(), player.getSelectedCol());
            selectedCells.remove(player.getPlayerId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.leaveGrid();
    }
    
    @Override
    public PlayerInfo getCurrentPlayerInfo() {
        return this.player.getPlayerInfo();
    }

    @Override
    public boolean isSudokuComplete() {
        return player.checkSudokuComplete();
    }
    
    private boolean isAlreadySelectedCell(Cell cell) {
        return selectedCells.values().stream()
        .anyMatch(selectedCell -> selectedCell.equals(cell));
    }
}