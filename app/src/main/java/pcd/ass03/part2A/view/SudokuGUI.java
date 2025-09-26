package pcd.ass03.part2A.view;

import pcd.ass03.part2A.model.Player;
import pcd.ass03.part2A.model.SudokuFactory;
import pcd.ass03.part2A.model.SudokuGrid;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.ArrayList;
import java.util.List;

public class SudokuGUI extends JFrame {
    private static final int GRID_SIZE = 9;
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color GRID_COLOR = new Color(120, 120, 120);
    private static final Color SELECTION_COLOR = new Color(173, 216, 230);
    private static final Color PLAYER_COLORS[] = {
        new Color(255, 182, 193), // Rosa chiaro
        new Color(144, 238, 144), // Verde chiaro  
        new Color(255, 218, 185), // Arancione chiaro
        new Color(221, 160, 221), // Viola chiaro
        new Color(255, 255, 224)  // Giallo chiaro
    };
    
    // Componenti principali
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // Schermata Lobby
    private JTextField playerNameField;
    private JList<String> gamesList;
    private DefaultListModel<String> gamesListModel;
    private JButton createGameButton;
    private JButton joinGameButton;
    private List<GameInfo> availableGames;
    
    // Schermata di Gioco
    private SudokuGrid sudokuGrid;
    private Player currentPlayer;
    private JTextField[][] gridCells;
    private JLabel playerLabel;
    private JButton clearCellButton;
    private JButton backToLobbyButton;
    private GameInfo currentGame;
    
    // Per simulare altri giocatori
    private Map<String, Color> playerColors;
    private int colorIndex = 0;
    
    public SudokuGUI() {
        initializeGUI();
        createSampleGames();
    }
    
    private void initializeGUI() {
        setTitle("Cooperative Sudoku - Lobby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Usa CardLayout per gestire le diverse schermate
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Crea le due schermate
        JPanel lobbyPanel = createLobbyPanel();
        JPanel gamePanel = createGamePanel();
        
        mainPanel.add(lobbyPanel, "LOBBY");
        mainPanel.add(gamePanel, "GAME");
        
        add(mainPanel);
        
        // Mostra inizialmente la lobby
        cardLayout.show(mainPanel, "LOBBY");
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        
        playerColors = new HashMap<>();
        availableGames = new ArrayList<>();
    }
    
    private JPanel createLobbyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setPreferredSize(new Dimension(800, 600));
        
        // Header
        JPanel headerPanel = createLobbyHeader();
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Centro - Lista partite
        JPanel centerPanel = createGamesListPanel();
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // Footer - Controlli
        JPanel footerPanel = createLobbyControls();
        panel.add(footerPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createLobbyHeader() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder("Benvenuto nel Cooperative Sudoku"));
        
        // Logo/Titolo
        JLabel titleLabel = new JLabel("COOPERATIVE SUDOKU");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        panel.add(titleLabel);
        
        return panel;
    }
    
    private JPanel createGamesListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder("Partite Disponibili"));
        
        // Player name input
        JPanel playerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        playerPanel.setBackground(BACKGROUND_COLOR);
        playerPanel.add(new JLabel("Il tuo nome:"));
        playerNameField = new JTextField(20);
        playerNameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        playerPanel.add(playerNameField);
        panel.add(playerPanel, BorderLayout.NORTH);
        
        // Lista semplice delle partite
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> gamesList = new JList<>(listModel);
        gamesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gamesList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        gamesList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Salva riferimento alla lista per aggiornamenti
        this.gamesList = gamesList;
        this.gamesListModel = listModel;
        
        JScrollPane scrollPane = new JScrollPane(gamesList);
        scrollPane.setPreferredSize(new Dimension(650, 300));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLobbyControls() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder("Controlli"));
        
        createGameButton = new JButton("Crea Partita");
        createGameButton.setPreferredSize(new Dimension(120, 35));
        createGameButton.addActionListener(e -> showCreateGameDialog());
        panel.add(createGameButton);
        
        joinGameButton = new JButton("Entra");
        joinGameButton.setPreferredSize(new Dimension(80, 35));
        joinGameButton.addActionListener(e -> {
            try {
                joinSelectedGame();
            } catch (IOException | TimeoutException e1) {
                e1.printStackTrace();
            }
        });
        panel.add(joinGameButton);
        
        return panel;
    }
    
    private void showCreateGameDialog() {
        String playerName = playerNameField.getText().trim();
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci il tuo nome prima di creare una partita!");
            return;
        }
        
        JDialog dialog = new JDialog(this, "Crea Partita", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Nome partita
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Nome Partita:"), gbc);
        
        gbc.gridx = 1;
        JTextField gameNameField = new JTextField(15);
        gameNameField.setText("Partita di " + playerName);
        dialog.add(gameNameField, gbc);
        
        // Bottoni
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton createBtn = new JButton("Crea");
        createBtn.setPreferredSize(new Dimension(80, 30));
        createBtn.addActionListener(e -> {
            String gameName = gameNameField.getText().trim();
            if (gameName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Inserisci un nome per la partita!");
                return;
            }
            
            try {
                createAndJoinGame(gameName, playerName);
            } catch (IOException | TimeoutException e1) {
                e1.printStackTrace();
            }
            dialog.dispose();
        });
        
        JButton cancelBtn = new JButton("Annulla");
        cancelBtn.setPreferredSize(new Dimension(80, 30));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, gbc);
        
        dialog.setVisible(true);
    }
    
    private void createAndJoinGame(String gameName, String playerName) throws IOException, TimeoutException {
        // Crea il giocatore
        currentPlayer = new Player(playerName);
        playerColors.put(currentPlayer.getPlayerId(), PLAYER_COLORS[colorIndex % PLAYER_COLORS.length]);
        colorIndex++;
        
        // Crea la partita
        int difficulty = 40; // Difficoltà fissa
        SudokuFactory factory = new SudokuFactory();
        sudokuGrid = factory.generate(difficulty);
        
        GameInfo newGame = new GameInfo(
            "game_" + System.currentTimeMillis(),
            gameName,
            difficulty,
            1
        );
        
        availableGames.add(newGame);
        currentGame = newGame;
        currentPlayer.joinGrid(newGame.gameId);
        
        // Passa alla schermata di gioco
        switchToGameScreen();
    }
    
    private void joinSelectedGame() throws IOException, TimeoutException {
        String playerName = playerNameField.getText().trim();
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci il tuo nome prima di entrare in una partita!");
            return;
        }
        
        int selectedIndex = gamesList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Seleziona una partita dalla lista!");
            return;
        }
        
        GameInfo selectedGame = availableGames.get(selectedIndex);
        
        // Crea il giocatore
        currentPlayer = new Player(playerName);
        playerColors.put(currentPlayer.getPlayerId(), PLAYER_COLORS[colorIndex % PLAYER_COLORS.length]);
        colorIndex++;
        
        // Carica la partita (in un sistema reale si riceverebbe lo stato dal server)
        SudokuFactory factory = new SudokuFactory();
        sudokuGrid = factory.generate(selectedGame.difficulty);
        
        currentGame = selectedGame;
        currentPlayer.joinGrid(selectedGame.gameId);
        
        // Aggiorna il conteggio giocatori
        selectedGame.playerCount++;
        
        // Passa alla schermata di gioco
        switchToGameScreen();
    }
    
    private void switchToGameScreen() {
        setTitle("Cooperative Sudoku - " + currentGame.gameName);
        updateGameDisplay();
        updatePlayerInfo();
        cardLayout.show(mainPanel, "GAME");
    }
    
    private void refreshGamesList() {
        gamesListModel.clear();
        
        for (GameInfo game : availableGames) {
            String status = game.isComplete ? "[COMPLETATA]" : "[" + game.playerCount + " giocatori]";
            gamesListModel.addElement(game.gameName + " " + status);
        }
    }
    
    private void createSampleGames() {
        availableGames.add(new GameInfo("game_001", "Partita Principianti", 25, 2));
        availableGames.add(new GameInfo("game_002", "Sfida Serale", 40, 1));
        availableGames.add(new GameInfo("game_003", "Puzzle Difficile", 55, 3));
        availableGames.add(new GameInfo("game_004", "Partita Completata", 35, 2));
        
        refreshGamesList();
    }
    
    private JPanel createGamePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setPreferredSize(new Dimension(800, 600));
        
        // Header con info giocatore
        JPanel headerPanel = createGameHeader();
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Centro con griglia
        JPanel gridPanel = createGridPanel();
        panel.add(gridPanel, BorderLayout.CENTER);
        
        // Lato con controlli
        JPanel sidePanel = createGameSidePanel();
        panel.add(sidePanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createGameHeader() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder("Informazioni Giocatore"));
        
        playerLabel = new JLabel("Nessun giocatore connesso");
        playerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(playerLabel);
        
        return panel;
    }
    
    private JPanel createGridPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Griglia Sudoku"));
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        JPanel gridPanel = new JPanel(new GridLayout(9, 9, 1, 1));
        gridPanel.setBackground(GRID_COLOR);
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        gridCells = new JTextField[GRID_SIZE][GRID_SIZE];
        
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                JTextField cell = createGridCell(row, col);
                gridCells[row][col] = cell;
                gridPanel.add(cell);
            }
        }
        
        mainPanel.add(gridPanel, BorderLayout.CENTER);
        return mainPanel;
    }
    
    private JTextField createGridCell(int row, int col) {
        JTextField cell = new JTextField(1);
        cell.setHorizontalAlignment(JTextField.CENTER);
        cell.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        cell.setPreferredSize(new Dimension(50, 50));
        
        // Bordo più spesso per separare i quadranti 3x3
        Border border;
        int top = (row % 3 == 0 && row != 0) ? 3 : 1;
        int left = (col % 3 == 0 && col != 0) ? 3 : 1;
        int bottom = (row == 8) ? 3 : 1;
        int right = (col == 8) ? 3 : 1;
        border = BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK);
        cell.setBorder(border);
        
        // Gestione eventi
        final int r = row;
        final int c = col;
        
        cell.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (currentPlayer != null) {
                    selectCell(r, c);
                }
            }
        });
        
        cell.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char keyChar = e.getKeyChar();
                if (currentPlayer == null) {
                    e.consume();
                    return;
                }
                
                if (keyChar >= '1' && keyChar <= '9') {
                    int value = keyChar - '0';
                    if (setValue(r, c, value)) {
                        cell.setText(String.valueOf(value));
                        updateGameDisplay();
                        checkWin();
                    } else {
                        e.consume();
                        updatePlayerInfo();
                    }
                } else if (keyChar == KeyEvent.VK_BACK_SPACE || keyChar == KeyEvent.VK_DELETE || keyChar == '0') {
                    clearCell(r, c);
                    cell.setText("");
                    updateGameDisplay();
                } else {
                    e.consume();
                }
            }
        });
        
        return cell;
    }
    
    private JPanel createGameSidePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder("Controlli"));
        panel.setPreferredSize(new Dimension(200, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        
        backToLobbyButton = new JButton("Esci");
        backToLobbyButton.addActionListener(e -> backToLobby());
        gbc.gridy = 0;
        panel.add(backToLobbyButton, gbc);
        
        clearCellButton = new JButton("Cancella");
        clearCellButton.addActionListener(e -> clearSelectedCell());
        gbc.gridy = 2;
        panel.add(clearCellButton, gbc);
        
        // Informazioni partita
        JTextArea gameInfo = new JTextArea();
        gameInfo.setEditable(false);
        gameInfo.setBackground(panel.getBackground());
               
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        return panel;
    }
    
    private void backToLobby() {
        int result = JOptionPane.showConfirmDialog(this,
            "Sei sicuro di voler uscire dalla partita?",
            "Conferma",
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            if (currentPlayer != null) {
                currentPlayer.leaveGrid();
                if (currentGame != null) {
                    currentGame.playerCount = Math.max(0, currentGame.playerCount - 1);
                }
            }
            
            setTitle("Cooperative Sudoku - Lobby");
            refreshGamesList();
            cardLayout.show(mainPanel, "LOBBY");
        }
    }

    private void selectCell(int row, int col) {
        if (currentPlayer == null) return;
        
        // Deseleziona la cella precedente
        if (currentPlayer.hasSelection()) {
            sudokuGrid.unselectCell(currentPlayer.getPlayerId(), 
                                    currentPlayer.getSelectedRow(), 
                                    currentPlayer.getSelectedCol());
        }
        
        // Seleziona la nuova cella
        try {
            currentPlayer.selectCell(row, col);
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
        sudokuGrid.selectCell(currentPlayer.getPlayerId(), row, col);
        
        updateCellColors();
    }
    
    private boolean setValue(int row, int col, int value) {
        if (sudokuGrid.setValue(row, col, value)) {
            return true;
        }
        return false;
    }
    
    private void clearCell(int row, int col) {
        sudokuGrid.clearValue(row, col);
    }
    
    private void clearSelectedCell() {
        if (currentPlayer != null && currentPlayer.hasSelection()) {
            int row = currentPlayer.getSelectedRow();
            int col = currentPlayer.getSelectedCol();
            clearCell(row, col);
            gridCells[row][col].setText("");
            updateGameDisplay();
        }
    }
    
    private void updateGameDisplay() {
        if (sudokuGrid == null || gridCells == null) return;
        updateDisplay();
    }
        
    // private void newGame() {
    //     initializeGame();
    //     if (currentPlayer != null) {
    //         updatePlayerInfo();
    //     }
    // }
    
    private void updateDisplay() {
        Integer[][] grid = sudokuGrid.getGrid();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                JTextField cell = gridCells[row][col];
                Integer value = grid[row][col];
                
                if (value != null) {
                    cell.setText(String.valueOf(value));
                    cell.setBackground(Color.WHITE);
                    cell.setEditable(false);
                } else {
                    cell.setText("");
                    cell.setBackground(Color.WHITE);
                    cell.setEditable(true);
                }
            }
        }
        updateCellColors();
    }
    
    private void updateCellColors() {
        if (gridCells == null || sudokuGrid == null) return;
        
        // Reset colori
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (sudokuGrid.getGrid()[row][col] == null) {
                    gridCells[row][col].setBackground(Color.WHITE);
                }
            }
        }
        
        // Evidenzia selezione del giocatore corrente
        if (currentPlayer != null && currentPlayer.hasSelection()) {
            int row = currentPlayer.getSelectedRow();
            int col = currentPlayer.getSelectedCol();
            Color playerColor = playerColors.get(currentPlayer.getPlayerId());
            if (playerColor != null) {
                gridCells[row][col].setBackground(playerColor);
            }
        }
    }
    
    private void updatePlayerInfo() {
        if (currentPlayer != null && currentGame != null) {
            playerLabel.setText(String.format("Giocatore: %s [%s] - Partita: %s",
                currentPlayer.getPlayerName(),
                currentPlayer.getPlayerId(),
                currentGame.gameName));
        }
    }
    
    private void checkWin() {
        if (sudokuGrid != null && sudokuGrid.isComplete()) {
            currentGame.isComplete = true;
            
            JOptionPane.showMessageDialog(this, 
                "CONGRATULAZIONI!\\n" +
                "Sudoku completato con successo!\\n" +
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private static class GameInfo {
        public final String gameId;
        public final String gameName;
        public final int difficulty;
        public int playerCount;
        public boolean isComplete;
        
        public GameInfo(String gameId, String gameName, int difficulty, 
                       int playerCount) {
            this.gameId = gameId;
            this.gameName = gameName;
            this.difficulty = difficulty;
            this.playerCount = playerCount;
        }
    }
}