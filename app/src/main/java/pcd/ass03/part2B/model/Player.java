package pcd.ass03.part2B.model;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import pcd.ass03.part2B.controller.SudokuController;
import pcd.ass03.part2B.model.message.SelectCellMessage;
import pcd.ass03.part2B.model.message.SetValueMessage;
import pcd.ass03.part2B.model.message.UnselectCellMessage;

public class Player extends UnicastRemoteObject implements UserCallbackInterface {
    private static final int GRID_SIZE = 9;
    
    private SudokuGrid sudoku;
    private final String playerId;
    private final String playerName;
    private final Server server;
    private final String color = String.format("#%06x", (int)(Math.random() * 0xFFFFFF));
    private String currentGridId;
    private int selectedRow = -1;
    private int selectedCol = -1;

    private SudokuController controller;

    public Player(String playerId, String playerName) throws RemoteException {
        this.playerId = playerId;
        this.playerName = playerName;
        this.sudoku = null;
        try {
            Registry registry = LocateRegistry.getRegistry();

            this.server = (Server) registry.lookup("SudokuServer");
            this.server.registerPlayer(this); // registra il player al server, per avere le callback
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to RMI server", e);
        }
    }

    public void setController(SudokuController controller) {
        this.controller = controller;
    }

    // MESSAGE SENDING METHODS

    public SudokuGrid createSudoku() throws RemoteException {
        System.out.println("Player " + playerId + " is creating a new Sudoku...");
        SudokuGrid sudoku = server.createSudoku(playerName);
        this.sudoku = sudoku;
        this.currentGridId = sudoku.getId();
        server.joinGame(playerId, currentGridId);

        return sudoku;
    }

    private void sendSelectCell(int row, int col) throws RemoteException {
        server.selectCell(new SelectCellMessage(currentGridId, playerId, row, col, color));
    }

    private void sendUnselectCell(int row, int col) throws RemoteException {
        server.unselectCell(new UnselectCellMessage(currentGridId, playerId, row, col));
    }

    private void sendSetValue(int row, int col, Integer value) throws RemoteException {
        server.setCellValue(new SetValueMessage(currentGridId, playerId, row, col, value == null || value < 1 || value > 9 ? "-1" : value.toString()));
    }

    private void sendRegisterInSudoku(String gridId) throws RemoteException {
        SudokuGrid currentSudoku = server.joinGame(playerId, gridId);
        if (currentSudoku != null) {
            this.sudoku = currentSudoku;
            this.currentGridId = gridId;
            controller.updateView(gridId, currentSudoku);
        } else {
            throw new IllegalArgumentException("Sudoku with ID " + gridId + " does not exist");
        }
    }

    // If it is a valid move, it sets the value both locally and sends the message to the other players
    public boolean tryToSetValue(int row, int col, int value) {
        if (currentGridId == null) {
            throw new IllegalStateException("Player is not in a game");
        }
        try {
            if (row != this.selectedRow || col != this.selectedCol) {
                throw new IllegalArgumentException("Cell (" + row + "," + col + ") is not selected");
            }
            if (value < 1 || value > 9) {
                return false;
            }
            sendSetValue(row, col, value);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }                        
        
    }
    
    // If it is a valid move, it selects the cell both locally and sends the message to the other players
    // it also unselects the previously selected cell
    public void selectCell(int row, int col) {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) {
            throw new IllegalArgumentException("Coordinata cella non valida: (" + row + "," + col + ")");
        }
        if (this.selectedCol >= 0 && this.selectedRow >= 0) {
            try {
                sendUnselectCell(this.selectedRow, this.selectedCol);
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }
        }
        this.selectedRow = row;
        this.selectedCol = col;
        try {
            sendSelectCell(row, col);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void unselectCell(int row, int col) throws IOException {
        sendUnselectCell(row, col);
        clearSelection();
    }
    
    private void clearSelection() {
        this.selectedRow = -1;
        this.selectedCol = -1;
    }

    @Override
    public String getPlayerId() throws RemoteException {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public String getCurrentGridId() {
        return currentGridId;
    }

    public int getSelectedRow() {
        return selectedRow;
    }
    
    public int getSelectedCol() {
        return selectedCol;
    }
    
    public boolean hasSelection() {
        return selectedRow >= 0 && selectedCol >= 0;
    }
    
    public boolean isInGame() {
        return currentGridId != null;
    }

    public boolean checkSudokuComplete() {
        if (sudoku == null) {
            return false;
        }
        try {
            return sudoku.isComplete();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void joinGrid(String gridId) {
        try {
            sendRegisterInSudoku(gridId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    public void leaveGrid() {
        this.currentGridId = null;
        try {
			this.server.leaveGame(this.playerId, this.currentGridId);
		} catch (RemoteException e) {
            System.out.println("An error occurred while leaving the game");
			e.printStackTrace();
		}
        clearSelection();
    }

    public String getColor() {
        return color;
    }

    public PlayerInfo getPlayerInfo() {
        return new PlayerInfo(playerId, playerName,
                              currentGridId, selectedRow, selectedCol);
    }

    public SudokuGrid getCurrentGrid() {
        return sudoku;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player{")
            .append("id='").append(playerId).append('\'')
            .append(", name='").append(playerName).append('\'');
        
        if (currentGridId != null) {
            sb.append(", grid='").append(currentGridId).append('\'');
        }
        
        if (hasSelection()) {
            sb.append(", selected=(").append(selectedRow).append(",").append(selectedCol).append(")");
        }
        
        return sb.toString();
    }

    @Override
    public void notifyUser(SudokuGrid sudoku) throws RemoteException {
        this.sudoku = sudoku;
        this.controller.updateView(this.currentGridId, this.sudoku);
    }

    @Override
    public void notifySudokuListUpdate(String sudokuId, String creator) throws RemoteException {
        this.controller.updateSudokuList(sudokuId, creator);
    }
}