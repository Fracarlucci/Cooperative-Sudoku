package pcd.ass03.part2B.model.message;

import java.io.Serializable;

public record UnselectCellMessage(String sudokuId, String playerId, int row, int col) implements Serializable {}
