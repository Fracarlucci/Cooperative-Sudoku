package pcd.ass03.part2A.view;

import java.util.List;
import java.util.Map;

import pcd.ass03.part2A.model.Cell;
import pcd.ass03.part2A.model.SudokuGrid;

public interface SudokuView {
    /**
     * Aggiorna la visualizzazione della GUI con i dati correnti
     * 
     * @param currentGridId ID della griglia corrente
     * @param selectedCells Mappa delle celle selezionate dai vari giocatori
     * @param availableSudokusId Lista degli ID dei sudoku disponibili
     */
    void updateView(String currentGridId, Map<String, Cell> selectedCells, List<String> availableSudokusId, SudokuGrid currentGrid);

    void addGame(String id, String creator);

    void checkWin();
}
