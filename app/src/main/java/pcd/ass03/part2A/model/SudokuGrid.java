package pcd.ass03.part2A.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SudokuGrid {
    private final String id;
    private final Integer[][] cells = new Integer[9][9];
    private final Map<String, Set<String>> selections = new HashMap<>(); 

    /** Crea una nuova griglia vuota */
    public SudokuGrid() {
        for (int r = 0; r < 9; r++) {
            Arrays.fill(cells[r], null);
        }
        this.id = "";
    }

    public SudokuGrid(Integer[][] initial, String id) {
        for (int r = 0; r < 9; r++) {
            System.arraycopy(initial[r], 0, cells[r], 0, 9);
        }
        this.id = id;
    }

    /** Imposta un valore in una cella, se valido */
    public boolean setValue(int row, int col, int value) {
        if (value < 1 || value > 9) {
            return false; // valore non valido
        }
        if (!isValidMove(row, col, value)) {
            return false; // mossa non valida
        }
        cells[row][col] = value;
        return true;
    }

    public void cancelValue(int row, int col) {
        cells[row][col] = null;
    }

    /** Rimuove un valore da una cella */
    public void clearValue(int row, int col) {
        cells[row][col] = null;
    }

    /** Controlla se inserire value in (row,col) rispetta le regole del Sudoku */
    public boolean isValidMove(int row, int col, int value) {
        for (int c = 0; c < 9; c++) {
            if (Objects.equals(cells[row][c], value)) return false;
        }
        for (int r = 0; r < 9; r++) {
            if (Objects.equals(cells[r][col], value)) return false;
        }
        // stesso quadrato 3x3
        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;
        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                if (Objects.equals(cells[r][c], value)) return false;
            }
        }
        return true;
    }

    /** Controlla se la griglia è completata (nessuna cella vuota) */
    public boolean isComplete() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (cells[r][c] == null) return false;
            }
        }
        return true;
    }

    /** Seleziona una cella per un giocatore */
    public void selectCell(String playerId, int row, int col) {
        selections.computeIfAbsent(playerId, k -> new HashSet<>())
                  .add(row + "," + col);
    }

    /** Deseleziona una cella per un giocatore */
    public void unselectCell(String playerId, int row, int col) {
        Set<String> set = selections.get(playerId);
        if (set != null) {
            set.remove(row + "," + col);
            if (set.isEmpty()) selections.remove(playerId);
        }
    }

    /** Restituisce lo stato della griglia come stringa */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                sb.append(cells[r][c] == null ? "." : cells[r][c]).append(" ");
            }
            sb.append("\n");
        }
        sb.append("Selections: ").append(selections).append("\n");
        return sb.toString();
    }

    public Integer[][] getGrid() {
        return cells;
    }

    public String getId() {
        return id;
    }
}
