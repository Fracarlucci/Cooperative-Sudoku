package pcd.ass03.part2A.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import pcd.ass03.part2A.utils.MessageUtils;


/**
 * Rappresenta un giocatore nel sistema Cooperative Sudoku distribuito.
 * Ogni giocatore ha un ID univoco, un nome e può partecipare a griglie condivise.
 */
public class Player {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
    
    private final List<SudokuGrid> sudokus = new ArrayList<>();
    private Channel channel;
    private final String playerId;
    private final String playerName;
    private volatile String currentGridId;
    private volatile int selectedRow = -1;
    private volatile int selectedCol = -1;
    
    public Player(String playerName) throws IOException, TimeoutException {
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del giocatore non può essere vuoto");
        }
        
        this.playerId = "player_" + ID_GENERATOR.incrementAndGet();
        this.playerName = playerName.trim();
        this.currentGridId = null;

        this.setupConnection();

    }

    public Player(String playerId, String playerName) throws IOException, TimeoutException {
        this.playerId = playerId;
        this.playerName = playerName;
        this.currentGridId = null;
        
        this.setupConnection();

        channel.exchangeDeclare(ChannelsEnum.CHANNEL_CREATE_SUDOKU.getName(), "fanout");
        channel.exchangeDeclare(ChannelsEnum.CHANNEL_SELECT_CELL.getName(), "fanout");
        channel.exchangeDeclare(ChannelsEnum.CHANNEL_SET_VALUE.getName(), "fanout");

        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName, ChannelsEnum.CHANNEL_CREATE_SUDOKU.getName(), "");
        channel.basicConsume(queueName, true, addSudokuCallBack(), t -> {});

        // channel.queueBind(queueName, ChannelsEnum.CHANNEL_SELECT_CELL.getName(), "");
        // channel.basicConsume(queueName, true, selectCellCallBack(), t -> {});

        // channel.queueBind(queueName, ChannelsEnum.CHANNEL_SET_VALUE.getName(), "");
        // channel.basicConsume(queueName, true, setValueCallBack(), t -> {});

    }

    private DeliverCallback addSudokuCallBack() {
        return (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            SudokuGrid receivedSudoku = MessageUtils.deserializeSudokuGrid(message);

            if (sudokus.stream().noneMatch(grid -> grid.getId() == (receivedSudoku.getId()))) {
                sudokus.add(receivedSudoku);
                // notifyGridCreated(); // updateView
            }
        };
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
    
    public void selectCell(int row, int col) {
        if (row < 0 || row >= 9 || col < 0 || col >= 9) {
            throw new IllegalArgumentException("Coordinata cella non valida: (" + row + "," + col + ")");
        }
        this.selectedRow = row;
        this.selectedCol = col;
    }
    
    public void clearSelection() {
        this.selectedRow = -1;
        this.selectedCol = -1;
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

    private void setupConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        this.channel = connection.createChannel();
    }
}