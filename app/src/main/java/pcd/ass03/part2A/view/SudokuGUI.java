package pcd.ass03.part2A.view;

import java.util.List;
import java.util.Map;

import pcd.ass03.part2A.model.Cell;
import pcd.ass03.part2A.model.SudokuGrid;

public interface SudokuGUI {

    void updateView(String currentGridId, Map<String, Cell> selectedCells, List<String> availableSudokusId, SudokuGrid currentGrid);

    void addGame(String id, String creator);

    void checkWin();
}
