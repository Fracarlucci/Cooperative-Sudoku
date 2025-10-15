package pcd.ass03.part2B.model;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SudokuGrid implements Sudoku, Serializable {
    private static final int GRID_SIZE = 9;
    
    private final String id;
    private final String creator;
    private final Integer[][] cells = new Integer[GRID_SIZE][GRID_SIZE];
    private final Map<String, Cell> selectedCells;

    
    /** Crea una nuova griglia vuota */
    public SudokuGrid() {
        for (int r = 0; r < GRID_SIZE; r++) {
            Arrays.fill(cells[r], null);
        }
        this.id = "";
        this.creator = "";
        this.selectedCells = new HashMap<>();
    }
    
    public SudokuGrid(Integer[][] initial, String id, String creator) {
        for (int r = 0; r < GRID_SIZE; r++) {
            System.arraycopy(initial[r], 0, cells[r], 0, GRID_SIZE);
        }
        this.id = id;
        this.creator = creator;
        this.selectedCells = new HashMap<>();
    }
    
    /** Imposta un valore in una cella, se valido */
    public boolean setValue(int row, int col, int value) throws RemoteException {
        if (value == -1) {
            cancelValue(row, col);
            return true;
        }
        if (value < 1 || value > GRID_SIZE) {
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
    
    /** Controlla se inserire value in (row,col) rispetta le regole del Sudoku */
    public boolean isValidMove(int row, int col, int value) throws RemoteException {
        for (int c = 0; c < GRID_SIZE; c++) {
            if (Objects.equals(cells[row][c], value)) return false;
        }
        for (int r = 0; r < GRID_SIZE; r++) {
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
    public synchronized boolean isComplete() throws RemoteException {
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (cells[r][c] == null) return false;
            }
        }
        return true;
    }
    
    /** Restituisce lo stato della griglia come stringa */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                sb.append(cells[r][c] == null ? "." : cells[r][c]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    public Integer[][] getGrid() {
        return cells;
    }
    
    public String getId() {
        return id;
    }
    
    public String getCreator() {
        return creator;
    }

    public Map<String, Cell> getSelectedCells() {
        return selectedCells;
    }
}
