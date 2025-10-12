package pcd.ass03.part2A.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import pcd.ass03.part2A.controller.SudokuController;
import pcd.ass03.part2A.model.message.SelectCellMessage;
import pcd.ass03.part2A.model.message.SetValueMessage;
import pcd.ass03.part2A.model.message.UnselectCellMessage;
import pcd.ass03.part2A.utils.MessageUtils;

public class Player {
    private static final int GRID_SIZE = 9;
    
    private final List<SudokuGrid> sudokus = new ArrayList<>();
    private Channel channel;
    private Connection connection;
    private final String playerId;
    private final String playerName;
    private final String color = String.format("#%06x", (int)(Math.random() * 0xFFFFFF));
    private volatile String currentGridId;
    private volatile int selectedRow = -1;
    private volatile int selectedCol = -1;

    private SudokuController controller;

    public Player(String playerId, String playerName) throws IOException, TimeoutException, InterruptedException {
        this.playerId = playerId;
        this.playerName = playerName;
        
        this.setupConnection();
        this.setupExchangesAndConsumers();
    }

    public void setController(SudokuController controller) {
        this.controller = controller;
    }

    // Create the necessary exchanges and consumers for RabbitMQ communication.
    private void setupExchangesAndConsumers() throws IOException, InterruptedException {
        channel.exchangeDeclare(ChannelsEnum.CHANNEL_CREATE_SUDOKU.getName(), "fanout");
        channel.exchangeDeclare(ChannelsEnum.CHANNEL_SELECT_CELL.getName(), "fanout");
        channel.exchangeDeclare(ChannelsEnum.CHANNEL_UNSELECT_CELL.getName(), "fanout");
        channel.exchangeDeclare(ChannelsEnum.CHANNEL_SET_VALUE.getName(), "fanout");

        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName, ChannelsEnum.CHANNEL_CREATE_SUDOKU.getName(), "");
        channel.basicConsume(queueName, true, addSudokuCallBack(), t -> {});

        String selectQueue = channel.queueDeclare().getQueue();
        channel.queueBind(selectQueue, ChannelsEnum.CHANNEL_SELECT_CELL.getName(), "");
        channel.basicConsume(selectQueue, true, selectCellCallBack(), t -> {});

        String unselectQueue = channel.queueDeclare().getQueue();
        channel.queueBind(unselectQueue, ChannelsEnum.CHANNEL_UNSELECT_CELL.getName(), "");
        channel.basicConsume(unselectQueue, true, unselectCellCallBack(), t -> {});

        String setValueQueue = channel.queueDeclare().getQueue();
        channel.queueBind(setValueQueue, ChannelsEnum.CHANNEL_SET_VALUE.getName(), "");
        channel.basicConsume(setValueQueue, true, setValueCallBack(), t -> {});
    }

    // MESSAGE SENDING METHODS

    public void createSudoku(SudokuGrid grid) throws IOException {
        sudokus.add(grid);
        String message = MessageUtils.serializeSudokuGrid(grid.getId(), grid.getCreator(), grid.getGrid());
                
        setupConnectionIfNeeded();

        channel.basicPublish(ChannelsEnum.CHANNEL_CREATE_SUDOKU.getName(), "", null, message.getBytes(StandardCharsets.UTF_8));
    }

    private void sendSelectCell(int row, int col) throws IOException {
        String message = currentGridId + " " + playerId + " " + row + " " + col + " " + color;
        setupConnectionIfNeeded();
        channel.basicPublish(ChannelsEnum.CHANNEL_SELECT_CELL.getName(), "", null, message.getBytes(StandardCharsets.UTF_8));
    }

    private void sendUnselectCell(int row, int col) throws IOException {
        String message = currentGridId + " " + playerId + " " + row + " " + col;
        setupConnectionIfNeeded();

        channel.basicPublish(ChannelsEnum.CHANNEL_UNSELECT_CELL.getName(), "", null, message.getBytes(StandardCharsets.UTF_8));
    }

    private void sendSetValue(int row, int col, Integer value) throws IOException {
        String message = currentGridId + " " + playerId + " " + row + " " + col + " " + (value == null || value < 1 || value > 9 ? -1 : value.toString());
        setupConnectionIfNeeded();

        channel.basicPublish(ChannelsEnum.CHANNEL_SET_VALUE.getName(), "", null, message.getBytes(StandardCharsets.UTF_8));
    }

    // CALLBACKS FOR MESSAGE CONSUMERS

    private DeliverCallback addSudokuCallBack() {
        return (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            SudokuGrid receivedSudoku = MessageUtils.deserializeSudokuGrid(message);
            
            if (sudokus.isEmpty() || sudokus.stream().noneMatch(grid -> grid.getId().equals(receivedSudoku.getId()))) {
                sudokus.add(receivedSudoku);
                if (controller != null) {
                    controller.notifySudokuCreated(receivedSudoku, receivedSudoku.getCreator());
                }
            }
        };
    }

    private DeliverCallback selectCellCallBack() {
        return (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            SelectCellMessage selectCellMessage = MessageUtils.deserializeSelectCellMessage(message);
            
            // Ignores his own messages
            if (!selectCellMessage.playerId().equals(this.playerId) && controller != null) {
                controller.notifyCellSelected(selectCellMessage);
            }
        };
    }

    private DeliverCallback unselectCellCallBack() {
        return (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            UnselectCellMessage unselectCellMessage = MessageUtils.deserializeUnselectCellMessage(message);
            
            // Ignores his own messages
            if (!unselectCellMessage.playerId().equals(this.playerId) && controller != null) {
                controller.notifyCellUnselected(unselectCellMessage);
            }
        };
    }

    private DeliverCallback setValueCallBack() {
        return (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            SetValueMessage setValueMessage = MessageUtils.deserializeSetValueMessage(message);

            // Ignores his own messages
            if (!setValueMessage.playerId().equals(this.playerId)) {
                sudokus.stream()
                    .filter(grid -> grid.getId().equals(setValueMessage.sudokuId()))
                    .findFirst()
                    .ifPresent(grid -> {
                        if (setValueMessage.value().equals("-1")) {
                            grid.cancelValue(setValueMessage.row(), setValueMessage.col());
                        } else {
                            grid.setValue(setValueMessage.row(), setValueMessage.col(), Integer.parseInt(setValueMessage.value()));
                        }

                        if (controller != null) {
                            controller.notifyCellValueChanged(setValueMessage);
                        }
                    });
            }
        };
    }

    // If it is a valid move, it sets the value both locally and sends the message to the other players
    public boolean tryToSetValue(int row, int col, int value) {
        if (currentGridId == null) {
            throw new IllegalStateException("Player is not in a game");
        }
        SudokuGrid currentGrid = sudokus.stream()
                                        .filter(grid -> grid.getId().equals(currentGridId))
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalStateException("Current grid not found"));
        try {
            if (row != this.selectedRow || col != this.selectedCol) {
                throw new IllegalArgumentException("Cell (" + row + "," + col + ") is not selected");
            }
            if (currentGrid.setValue(row, col, value)) {
                sendSetValue(row, col, value);
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }                        
        
    }
    
    // If it is a valid move, it selects the cell both locally and sends the message to the other players
    // it also unselects the previously selected cell
    public void selectCell(int row, int col) throws NumberFormatException, IOException {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) {
            throw new IllegalArgumentException("Coordinata cella non valida: (" + row + "," + col + ")");
        }
        if (this.selectedCol >= 0 && this.selectedRow >= 0) {
            sendUnselectCell(this.selectedRow, this.selectedCol);
        }
        this.selectedRow = row;
        this.selectedCol = col;
        sendSelectCell(row, col);
    }

    public void unselectCell(int row, int col) throws NumberFormatException, IOException {
        sendUnselectCell(row, col);
        clearSelection();
    }
    
    private void clearSelection() {
        this.selectedRow = -1;
        this.selectedCol = -1;
    }

    private void setupConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
    }
    
    private void setupConnectionIfNeeded() throws IOException {
        try {
            if (channel == null || !channel.isOpen()) {
                setupConnection();
                // If we recreate the connection, we also need to recreate the exchanges and consumers
                setupExchangesAndConsumers();
            }
        } catch (TimeoutException | InterruptedException e) {
            throw new IOException("Failed to setup connection", e);
        }
    }

    public String getPlayerId() {
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
    }
    
    public void leaveGrid() {
        this.currentGridId = null;
        clearSelection();
    }

    public String getColor() {
        return color;
    }

    public List<String> getSudokusId() {
        return sudokus.stream().map(SudokuGrid::getId).toList();
    }

    public PlayerInfo getPlayerInfo() {
        return new PlayerInfo(playerId, playerName, isInGame(), 
                              currentGridId, selectedRow, selectedCol);
    }

    public SudokuGrid getCurrentGrid() {
        return sudokus.stream()
                      .filter(grid -> grid.getId().equals(currentGridId))
                      .findFirst()
                      .orElse(null);
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
}