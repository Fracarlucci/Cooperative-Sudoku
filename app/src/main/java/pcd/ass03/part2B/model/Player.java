package pcd.ass03.part2B.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import pcd.ass03.part2A.utils.MessageUtils;
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
    private volatile String currentGridId;
    private volatile int selectedRow = -1;
    private volatile int selectedCol = -1;

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

    public String createSudoku() throws RemoteException {
        System.out.println("Player " + playerId + " is creating a new Sudoku...");
        SudokuGrid sudoku = server.createSudoku(playerId);
        this.sudoku = sudoku;
        this.currentGridId = sudoku.getId();
        server.registerPlayerInSudoku(playerId, currentGridId);
        return sudoku.getId();
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
        server.registerPlayerInSudoku(playerId, gridId);
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
            sendSetValue(row, col, value);
            return false;
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

    public void joinGrid(String gridId) {
        this.currentGridId = gridId;
        try {
            sendRegisterInSudoku(gridId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    public void leaveGrid() {
        this.currentGridId = null;
        clearSelection();
    }

    public String getColor() {
        return color;
    }

    public PlayerInfo getPlayerInfo() {
        return new PlayerInfo(playerId, playerName, isInGame(), 
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
        System.out.println("Player " + playerId + " received update for Sudoku " + sudoku);
    }
}