package pcd.ass03.part2A.model.message;

public record SelectCellMessage(int sudokuId, String playerId, int row, int col) {}
