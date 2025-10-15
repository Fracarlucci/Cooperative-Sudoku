package pcd.ass03.part2B.model;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import pcd.ass03.part2B.model.message.SetValueMessage;
import pcd.ass03.part2B.model.message.SelectCellMessage;
import pcd.ass03.part2B.model.message.UnselectCellMessage;

public class ServerImpl implements Server {

    private List<SudokuGrid> grids;
    private List<UserCallbackInterface> players;
    private Map<String, List<UserCallbackInterface>> playersInSudoku;
    private SudokuFactory factory;

    public ServerImpl() {
        this.grids = new ArrayList<>();
        this.playersInSudoku = new HashMap<>();
        this.players = new ArrayList<>();
        this.factory = new SudokuFactory();
    }

    @Override
    public synchronized void registerPlayer(UserCallbackInterface player) throws RemoteException {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    @Override
    public synchronized boolean joinGame(String playerId, String gridId) throws RemoteException {
        Optional<SudokuGrid> gridOpt = getSudoku(gridId);
        if (gridOpt.isEmpty()) {
            return false;
        }
        UserCallbackInterface player = players.stream()
                                        .filter(p -> {
                                            try {
                                                return p.getPlayerId().equals(playerId);
                                            } catch (RemoteException e) {
                                                e.printStackTrace();
                                                return false;
                                            }
                                        })
                                        .findFirst()
                                        .orElse(null);
        playersInSudoku.putIfAbsent(gridId, new ArrayList<>());
        List<UserCallbackInterface> playersList = playersInSudoku.get(gridId);
        playersList.add(player);
        return true;
    }

    @Override
    public synchronized void leaveGame(String playerId, String gridId) throws RemoteException {
        List<UserCallbackInterface> playersList = playersInSudoku.get(gridId);
        if (playersList != null) {
            playersList.removeIf(p -> {
                try {
                    return p.getPlayerId().equals(playerId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return false;
                }
            });
        }
    }

    @Override
    public synchronized SudokuGrid createSudoku(String creator) throws RemoteException {
        SudokuGrid newGrid = factory.generate(50, creator);
        grids.add(newGrid);
        System.out.println("New Sudoku created with ID: " + newGrid.getId());
        return newGrid;
    }

    @Override
    public synchronized List<SudokuGrid> getSudokus() throws RemoteException {
        return new ArrayList<>(grids);
    }
    
    @Override
    public synchronized Optional<SudokuGrid> getSudoku(String gridId) throws RemoteException {
        return grids.stream()
                    .filter(g -> g.getId()
                    .equals(gridId))
                    .findFirst();
    }

    @Override
    public synchronized boolean setCellValue(SetValueMessage msg) throws RemoteException {
        Optional<SudokuGrid> sudokuOpt = getSudoku(msg.sudokuId());
        if (sudokuOpt.isPresent()) {
            SudokuGrid sudoku = sudokuOpt.get();
            boolean success = sudoku.setValue(msg.row(), msg.col(), Integer.parseInt(msg.value()));
            if (success) {
                notifyClients(msg.sudokuId());
            }
            return success;
        }
        return false;
    }

    @Override
    public synchronized boolean selectCell(SelectCellMessage msg) throws RemoteException {
        Optional<SudokuGrid> sudokuOpt = getSudoku(msg.sudokuId());
        if (sudokuOpt.isPresent()) {
            SudokuGrid sudoku = sudokuOpt.get();
            sudoku.getSelectedCells().put(msg.playerId(), new Cell(msg.row(), msg.col()));
            notifyClients(msg.sudokuId());
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean unselectCell(UnselectCellMessage msg) throws RemoteException {
        Optional<SudokuGrid> sudokuOpt = getSudoku(msg.sudokuId());
        if (sudokuOpt.isPresent()) {
            SudokuGrid sudoku = sudokuOpt.get();
            sudoku.getSelectedCells().remove(msg.playerId());
            notifyClients(msg.sudokuId());
            return true;
        }
        return false;
    }

    // It notifies all the players in the same sudoku that a change has been made
    private synchronized void notifyClients(String gridId) throws RemoteException {
        List<UserCallbackInterface> playersList = playersInSudoku.get(gridId);
        SudokuGrid sudoku = getSudoku(gridId).orElseThrow();
        playersList.forEach(p -> {
            try {
                System.out.println("Notifying player " + p.getPlayerId() + " of update in Sudoku " + gridId);
                p.notifyUser(sudoku);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private synchronized void notifySudokuListUpdate() throws RemoteException {
        List<String> sudokuIds = this.grids.stream().map(SudokuGrid::getId).toList();
        players.forEach(p -> {
            try {
                p.notifySudokuListUpdate(sudokuIds);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }
}