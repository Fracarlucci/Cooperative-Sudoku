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
    private Map<String, Cell> selectedCells;
    private SudokuGUI view;
    
    public SudokuControllerImpl(Player player) {
        this.player = player;
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
    public void updateView(SudokuGrid sudoku) {
        this.selectedCells = sudoku.getSelectedCells(); 
        if (view != null) {
            view.updateView(this.selectedCells, sudoku);
        }
    }

    @Override
    public void updateSudokuList(String sudokuId, String creator) {
        view.addGame(sudokuId, creator);
    }
    
    // Select cell, if row or col is -1
    // player will unselect the cell
    @Override
    public boolean selectCell(int row, int col) {
        String gridId = player.getCurrentGridId();
        try {
            if (isAlreadySelectedCell(new Cell(row, col))) {
                return false;
            }
            player.selectCell(row, col);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean setCellValue(int row, int col, int value) {
        if (player.tryToSetValue(row, col, value)) {
            return true;
        }
        return false;
    }
    
    @Override
    public void joinGame(String sudokuId) {
        player.joinGrid(sudokuId);
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