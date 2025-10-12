package pcd.ass03.part2B.model.message;

public record SetValueMessage(String sudokuId, String playerId, int row, int col, String value) {}
