package pcd.ass03.part2B.view;

import java.util.List;
import java.util.Map;

import pcd.ass03.part2B.model.Cell;
import pcd.ass03.part2B.model.SudokuGrid;

public interface SudokuGUI {

    void updateView(String currentGridId, Map<String, Cell> selectedCells, SudokuGrid currentGrid);

    void updateSudokuList(List<String> sudokuIds);

    void addGame(String id, String creator);

    void checkWin();
}
