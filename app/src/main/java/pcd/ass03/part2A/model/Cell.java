package pcd.ass03.part2A.model;

public record Cell(int row, int col, int sudokuId) {
  @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell cell)) return false;
        return row == cell.row && col == cell.col && sudokuId == cell.sudokuId;
    }

    @Override
    public String toString() {
        return "Cell(" + row + "," + col + ") in Sudoku " + sudokuId;
    }
}
