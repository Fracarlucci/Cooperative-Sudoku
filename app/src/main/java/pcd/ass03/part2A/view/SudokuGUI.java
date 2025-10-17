package pcd.ass03.part2A.view;

import java.util.Map;

import pcd.ass03.part2A.model.Cell;
import pcd.ass03.part2A.model.SudokuGrid;

public interface SudokuGUI {

    void updateView(Map<String, Cell> selectedCells, SudokuGrid currentGrid);

    void addGame(String id, String creator);

    void checkWin();
}
