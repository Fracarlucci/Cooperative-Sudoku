package pcd.ass03.part2A.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import pcd.ass03.part2A.model.message.SelectCellMessage;
import pcd.ass03.part2A.model.message.SetValueMessage;
import pcd.ass03.part2A.model.message.UnselectCellMessage;
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
    private final String color = String.format("#%06x", (int)(Math.random() * 0xFFFFFF));
    private volatile int currentGridId;
    private volatile int selectedRow = -1;
    private volatile int selectedCol = -1;
    
    public Player(String playerName) throws IOException, TimeoutException, InterruptedException {
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del giocatore non può essere vuoto");
        }
        
        this.playerId = "player_" + ID_GENERATOR.incrementAndGet();
        this.playerName = playerName.trim();
        this.currentGridId = -1;

        this.setupConnection();
        this.setupExchangesAndConsumers();
    }

    public Player(String playerId, String playerName) throws IOException, TimeoutException, InterruptedException {
        this.playerId = playerId;
        this.playerName = playerName;
        this.currentGridId = -1;
        
        this.setupConnection();
        this.setupExchangesAndConsumers();
    }

    private void setupExchangesAndConsumers() throws IOException, InterruptedException {
        channel.exchangeDeclare(ChannelsEnum.CHANNEL_CREATE_SUDOKU.getName(), "fanout");
        channel.exchangeDeclare(ChannelsEnum.CHANNEL_SELECT_CELL.getName(), "fanout");
        channel.exchangeDeclare(ChannelsEnum.CHANNEL_UNSELECT_CELL.getName(), "fanout");
        channel.exchangeDeclare(ChannelsEnum.CHANNEL_SET_VALUE.getName(), "fanout");

        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName, ChannelsEnum.CHANNEL_CREATE_SUDOKU.getName(), "");
        channel.basicConsume(queueName, true, addSudokuCallBack(), t -> {});

        String unselectQueue = channel.queueDeclare().getQueue();
        channel.queueBind(unselectQueue, ChannelsEnum.CHANNEL_UNSELECT_CELL.getName(), "");
        channel.basicConsume(unselectQueue, true, unselectCellCallBack(), t -> {});

        String setValueQueue = channel.queueDeclare().getQueue();
        channel.queueBind(setValueQueue, ChannelsEnum.CHANNEL_SET_VALUE.getName(), "");
        channel.basicConsume(setValueQueue, true, setValueCallBack(), t -> {});

        Thread.sleep(100);
    }

    public void createSudoku(SudokuGrid grid) throws IOException {
        sudokus.add(grid);
        String message = MessageUtils.serializeSudokuGrid(grid.getId(), grid.getGrid());
        
        System.out.println("Publishing sudoku " + grid.getId() + " by player " + playerName);
        
        setupConnectionIfNeeded();

        channel.basicPublish(ChannelsEnum.CHANNEL_CREATE_SUDOKU.getName(), "", null, message.getBytes(StandardCharsets.UTF_8));
    }

    public void selectCell(int gridId, int row, int col) throws IOException {
        String message = gridId + " " + playerId + " " + row + " " + col + " " + color;
        setupConnectionIfNeeded();
        channel.basicPublish(ChannelsEnum.CHANNEL_SELECT_CELL.getName(), "", null, message.getBytes(StandardCharsets.UTF_8));
    }

    public void setValue(int value) throws IOException {
        if (currentGridId == -1) {
            throw new IllegalStateException("Nessuna griglia selezionata");
        }
        if (selectedRow < 0 || selectedCol < 0) {
            throw new IllegalStateException("Nessuna cella selezionata");
        }
        String valueStr = (value < 1 || value > 9) ? "" : String.valueOf(value);
        String message = currentGridId + " " + playerId + " " + selectedRow + " " + selectedCol + " " + valueStr;
        setupConnectionIfNeeded();

        channel.basicPublish(ChannelsEnum.CHANNEL_SET_VALUE.getName(), "", null, message.getBytes(StandardCharsets.UTF_8));
    }

    public void unselectCell(int gridId, int row, int col) throws IOException {
        String message = gridId + " " + row + " " + col;
        setupConnectionIfNeeded();

        channel.basicPublish(ChannelsEnum.CHANNEL_UNSELECT_CELL.getName(), "", null, message.getBytes(StandardCharsets.UTF_8));
    }

    public void setValue(int gridId, int row, int col, Integer value) throws IOException {
        String message = gridId + " " + playerId + " " + row + " " + col + " " + (value == null ? "" : value.toString()) + " " + color;
        setupConnectionIfNeeded();

        channel.basicPublish(ChannelsEnum.CHANNEL_SET_VALUE.getName(), "", null, message.getBytes(StandardCharsets.UTF_8));
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

    private DeliverCallback selectCellCallBack() {
        return (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            SelectCellMessage selectCellMessage = MessageUtils.deserializeSelectCellMessage(message);
            System.out.println("Player " + playerName + " received cell selection: " + message);
            // notifyCellSelected(); // updateView
        };
    }

    private DeliverCallback unselectCellCallBack() {
        return (consumerTag, delivery) -> {
            try{
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            UnselectCellMessage unselectCellMessage = MessageUtils.deserializeUnselectCellMessage(message);
            System.out.println("Player " + playerName + " received cell unselection: " + message);
            // notifyCellSelected(); // updateView
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    private DeliverCallback setValueCallBack() {
        return (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            SetValueMessage setValueMessage = MessageUtils.deserializeSetValueMessage(message);
            System.out.println("Player " + playerName + " received value set: " + message);

            sudokus.stream()
                .filter(grid -> grid.getId() == setValueMessage.sudokuId())
                .findFirst()
                .ifPresent(grid -> {
                    if (setValueMessage.value().equals("")) {
                        grid.cancelValue(setValueMessage.row(), setValueMessage.col());
                    } else {
                        grid.setValue(setValueMessage.row(), setValueMessage.col(), Integer.parseInt(setValueMessage.value()));
                    }
                    // notifyCellValueSet(); // updateView
                });
        };
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public int getCurrentGridId() {
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
        return currentGridId != -1;
    }

    public void joinGrid(int gridId) {
        this.currentGridId = gridId;
    }
    
    public void leaveGrid() {
        this.currentGridId = -1;
        clearSelection();
    }

    public List<Integer> getSudokusId() {
        return sudokus.stream().map(SudokuGrid::getId).toList();
    }

    public boolean tryToSetValue(int row, int col, int value) {
        if (currentGridId == -1) {
            throw new IllegalStateException("Player is not in a game");
        }
        SudokuGrid currentGrid = sudokus.stream()
                                        .filter(grid -> Integer.toString(grid.getId()).equals(currentGridId))
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalStateException("Current grid not found"));
        try {
            if (row != this.selectedRow || col != this.selectedCol) {
                throw new IllegalArgumentException("Cell (" + row + "," + col + ") is not selected");
            }
            if (currentGrid.setValue(row, col, value)) {
                this.setValue(this.currentGridId, row, col, value);
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }                        
        
    }
    
    public void selectCell(int row, int col) throws NumberFormatException, IOException {
        if (row < 0 || row >= 9 || col < 0 || col >= 9) {
            throw new IllegalArgumentException("Coordinata cella non valida: (" + row + "," + col + ")");
        }
        if (this.selectedCol >= 0 && this.selectedRow >= 0) {
            unselectCell(this.currentGridId, this.selectedRow, this.selectedCol);
        }
        this.selectedRow = row;
        this.selectedCol = col;
        selectCell(this.currentGridId, row, col);
    }

    public void unselectCell(int row, int col) throws NumberFormatException, IOException {
        if (row > 0 || col > 0) {
            throw new IllegalArgumentException("Coordinata cella non valida: (" + row + "," + col + ")");
        }
        unselectCell(this.currentGridId, row, col);
        // TODO se si mette il messaggio nel clearSelection qui va modificato
        clearSelection();
    }
    
    public void clearSelection() {
        // TODO forse è da mandare una unselect
        this.selectedRow = -1;
        this.selectedCol = -1;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player{")
          .append("id='").append(playerId).append('\'')
          .append(", name='").append(playerName).append('\'');
        
        if (currentGridId != -1) {
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
    
    private void setupConnectionIfNeeded() throws IOException {
        try {
            if (channel == null || !channel.isOpen()) {
                setupConnection();
                // Se ricreiamo la connessione, dobbiamo ricreare anche gli exchange e consumer
                setupExchangesAndConsumers();
            }
        } catch (TimeoutException | InterruptedException e) {
            throw new IOException("Failed to setup connection", e);
        }
    }
}