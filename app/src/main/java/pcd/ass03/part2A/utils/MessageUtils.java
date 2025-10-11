package pcd.ass03.part2A.utils;

import pcd.ass03.part2A.model.SudokuGrid;
import pcd.ass03.part2A.model.message.SelectCellMessage;
import pcd.ass03.part2A.model.message.SetValueMessage;
import pcd.ass03.part2A.model.message.UnselectCellMessage;

public class MessageUtils {
    
    public static SudokuGrid deserializeSudokuGrid(String message) {
        String[] elements = message.split(" ");
        String sudokuId = elements[0];
        String creator = elements[1];
        Integer[][] grid = new Integer[9][9];
        int index = 2;
        try
        {
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    if (elements[index].equals("null")) {
                        grid[row][col] = null;
                    } else {
                        grid[row][col] = Integer.parseInt(elements[index]);
                    }
                    index++;
                }
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return new SudokuGrid(grid, sudokuId, creator);
    }
    
    public static SelectCellMessage deserializeSelectCellMessage(String message) {
        String[] elements = message.split(" ");
        String sudokuId = elements[0];
        String playerId = elements[1];
        int row = Integer.parseInt(elements[2]);
        int col = Integer.parseInt(elements[3]);
        String color = elements[4];
        
        return new SelectCellMessage(sudokuId, playerId, row, col, color);
    }
    
    public static UnselectCellMessage deserializeUnselectCellMessage(String message) {
        String[] elements = message.split(" ");
        String sudokuId = elements[0];
        String playerId = elements[1];
        int row = Integer.parseInt(elements[2]);
        int col = Integer.parseInt(elements[3]);
        
        return new UnselectCellMessage(sudokuId, playerId, row, col);
    }
    
    
    public static SetValueMessage deserializeSetValueMessage(String message) {
        String[] elements = message.split(" ");
        String sudokuId = elements[0];
        String playerId = elements[1];
        int row = Integer.parseInt(elements[2]);
        int col = Integer.parseInt(elements[3]);
        String cellValue = elements[4]; 
        
        return new SetValueMessage(sudokuId, playerId, row, col, cellValue);
    }
    
    public static String serializeSudokuGrid(String sudokuId, String creator, Integer[][] grid) {
        StringBuilder sb = new StringBuilder();
        sb.append(sudokuId).append(" ").append(creator).append(" ");
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                sb.append(grid[row][col]).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
