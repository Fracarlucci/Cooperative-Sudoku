package pcd.ass03.part2A.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rappresenta un giocatore nel sistema Cooperative Sudoku distribuito.
 * Ogni giocatore ha un ID univoco, un nome e può partecipare a griglie condivise.
 */
public class Player {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
    
    private final String playerId;
    private final String playerName;
    private final long joinTimestamp;
    private volatile String currentGridId; // ID della griglia a cui sta partecipando
    private volatile int selectedRow = -1; // cella attualmente selezionata
    private volatile int selectedCol = -1;
    
    /**
     * Crea un nuovo giocatore con nome specificato
     */
    public Player(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del giocatore non può essere vuoto");
        }
        
        this.playerId = "player_" + ID_GENERATOR.incrementAndGet();
        this.playerName = playerName.trim();
        this.joinTimestamp = System.currentTimeMillis();
        this.currentGridId = null;
    }
    
    /**
     * Crea un giocatore con ID specificato (utile per la deserializzazione)
     */
    public Player(String playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.joinTimestamp = System.currentTimeMillis();
        this.currentGridId = null;
    }
    
    // Getters
    public String getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public long getJoinTimestamp() {
        return joinTimestamp;
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
    
    /**
     * Controlla se il giocatore ha una cella selezionata
     */
    public boolean hasSelection() {
        return selectedRow >= 0 && selectedCol >= 0;
    }
    
    /**
     * Controlla se il giocatore sta partecipando a una griglia
     */
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
    
    /**
     * Restituisce informazioni formattate sul giocatore
     */
    public String getPlayerInfo() {
        return String.format("Player[%s] %s", 
            playerId, playerName);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player player = (Player) obj;
        return Objects.equals(playerId, player.playerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(playerId);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player{")
          .append("id='").append(playerId).append('\'')
          .append(", name='").append(playerName).append('\'')
        
        if (currentGridId != null) {
            sb.append(", grid='").append(currentGridId).append('\'');
        }
        
        if (hasSelection()) {
            sb.append(", selected=(").append(selectedRow).append(",").append(selectedCol).append(")");
        }
        
        return sb.toString();
    }
}