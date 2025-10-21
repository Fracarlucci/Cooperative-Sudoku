package pcd.ass03.part2A.model;

public enum ChannelsEnum {
  CHANNEL_CREATE_SUDOKU("channel_create_sudoku"),
  CHANNEL_SELECT_CELL("channel_select_cell"),
  CHANNEL_UNSELECT_CELL("channel_unselect_cell"),
  CHANNEL_SET_VALUE("channel_set_value");

   private final String name;

    ChannelsEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
