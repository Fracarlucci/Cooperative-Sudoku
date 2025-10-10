package pcd.ass03.part2A.model.message;

public record UnselectCellMessage(String sudokuId, String playerId, int row, int col) {}
