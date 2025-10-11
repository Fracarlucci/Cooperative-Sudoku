package pcd.ass03.part2A.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SudokuFactory {
    private static final int SIZE = 9;
    
    private String generateRandomId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random rand = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    public SudokuGrid generate(int emptyCells, String creator) {
        String id = generateRandomId();
        
        SudokuGrid newSudoku = new SudokuGrid();
        fillGrid(newSudoku);
        
        Integer[][] puzzle = deepCopy(newSudoku.getGrid());
        
        // remove numbers to create the puzzle
        Random rand = new Random();
        int removed = 0;
        while (removed < emptyCells) {
            int r = rand.nextInt(SIZE);
            int c = rand.nextInt(SIZE);
            if (puzzle[r][c] != null) {
                puzzle[r][c] = null;
                removed++;
            }
        }
        
        return new SudokuGrid(puzzle, id, creator);
    }
    
    /**
    * recursive method to fill the grid using backtracking
    */
    private boolean fillGrid(SudokuGrid sudoku) {
        Integer[][] grid = sudoku.getGrid();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col] == null) {
                    List<Integer> numbers = new ArrayList<>();
                    for (int i = 1; i <= SIZE; i++) numbers.add(i);
                    Collections.shuffle(numbers);
                    
                    for (int num : numbers) {
                        if (sudoku.isValidMove(row, col, num)) {
                            grid[row][col] = num;
                            if (fillGrid(sudoku)) {
                                return true;
                            } else {
                                grid[row][col] = null;
                            }
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }
    
    private static Integer[][] deepCopy(Integer[][] grid) {
        Integer[][] copy = new Integer[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, SIZE);
        }
        return copy;
    }
}
