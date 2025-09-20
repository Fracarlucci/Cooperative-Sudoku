package pcd.ass03.part2A.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SudokuManager {
  public final Map<Integer, List<Player>> playersMap;
  public final List<SudokuGrid> sudokuList;

  public SudokuManager() {
    this.playersMap = new HashMap<>();
    sudokuList = new ArrayList<>();
  }

  public List<Player> getPlayersInGrid(int sudokuId) {
    return playersMap.get(sudokuId);
  }

  public void addPlayerToSudoku(int sudokuId, Player player) {
    playersMap.computeIfAbsent(sudokuId, k -> new ArrayList<>()).add(player);
  }

  public void removePlayerFromSudoku(int sudokuId, Player player) {
    List<Player> players = playersMap.get(sudokuId);
    if (players != null) {
      players.remove(player);
      if (players.isEmpty()) {
        playersMap.remove(sudokuId);
      }
    }
  }

  public void addSudoku(SudokuGrid sudoku) {
    sudokuList.add(sudoku);
    playersMap.put(sudoku.getId(), new ArrayList<>());
  }

}
