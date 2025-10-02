package pcd.ass03.part2A.model;

public record Cell(int row, int col) {
  @Override
    public boolean equals(Object o) {
        if (this == o) return true;                      // stesso riferimento
        if (o == null || getClass() != o.getClass())     // tipo diverso
            return false;
        Cell other = (Cell) o;
        return row == other.row && col == other.col;     // confronto campi
    }

    @Override
    public String toString() {
        return "Cell[" + row + "," + col + "]";
    }
}
