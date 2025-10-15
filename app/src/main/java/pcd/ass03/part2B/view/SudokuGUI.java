package pcd.ass03.part2B.view;

import java.util.Map;

import pcd.ass03.part2B.model.Cell;
import pcd.ass03.part2B.model.SudokuGrid;

public interface SudokuGUI {

    void updateView(String currentGridId, Map<String, Cell> selectedCells, SudokuGrid currentGrid);

    void addGame(String id, String creator);

    void checkWin();
}
