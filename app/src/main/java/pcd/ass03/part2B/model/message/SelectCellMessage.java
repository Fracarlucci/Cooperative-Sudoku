package pcd.ass03.part2B.model.message;

import java.io.Serializable;

public record SelectCellMessage(String sudokuId, String playerId, int row, int col, String color) implements Serializable{}
