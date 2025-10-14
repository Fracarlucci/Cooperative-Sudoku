package pcd.ass03.part2B.model.message;

import java.io.Serializable;

public record SetValueMessage(String sudokuId, String playerId, int row, int col, String value) implements Serializable {}
