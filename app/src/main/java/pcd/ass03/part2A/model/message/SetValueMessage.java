package pcd.ass03.part2A.model.message;

public record SetValueMessage(int sudokuId, String playerId, int row, int col, String value) {}
