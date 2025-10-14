package pcd.ass03.part2B.model;

import java.io.Serializable;

public record Cell(int row, int col) implements Serializable {
  @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell cell)) return false;
        return row == cell.row && col == cell.col;
    }

    @Override
    public String toString() {
        return "Cell(" + row + "," + col + ")";
    }
}
