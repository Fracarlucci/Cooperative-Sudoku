package pcd.ass03.part2A.view;

import pcd.ass03.part2A.controller.SudokuController;
import pcd.ass03.part2A.controller.SudokuControllerImpl;
import pcd.ass03.part2A.model.Cell;
import pcd.ass03.part2A.model.PlayerInfo;
import pcd.ass03.part2A.model.SudokuGrid;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class SudokuGUI extends JFrame implements SudokuView {
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

    private SudokuController controller;
    
    // Schermata Lobby
    private JList<String> gamesList;
    private DefaultListModel<String> gamesListModel;
    private JButton createGameButton;
    private JButton joinGameButton;
    private List<GameInfo> availableGames;
    
    // Schermata di Gioco
    private SudokuGrid sudokuGrid;
    private PlayerInfo currentPlayerInfo;
    private JTextField[][] gridCells;
    private JLabel playerLabel;
    private JButton clearCellButton;
    private JButton backToLobbyButton;
    private GameInfo currentGame;
    private String selectedGridId;
    
    // Per simulare altri giocatori
    private Map<String, Color> playerColors;
    private int colorIndex = 0;
    
    public SudokuGUI(SudokuController controller) {
        this.controller = controller;
        this.currentPlayerInfo = controller.getCurrentPlayerInfo();
        
        // Imposta questa GUI come view nel controller
        if (controller instanceof SudokuControllerImpl) {
            ((SudokuControllerImpl) controller).setView(this);
        }
        
        initializeGUI();
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
        
        createGameButton = new JButton("Crea Partita");
        createGameButton.setPreferredSize(new Dimension(120, 35));
        createGameButton.addActionListener(e -> createAndJoinGame());
        panel.add(createGameButton);
        
        joinGameButton = new JButton("Entra");
        joinGameButton.setPreferredSize(new Dimension(80, 35));
        joinGameButton.addActionListener(e -> {
            int selectedIndex = gamesList.getSelectedIndex();
            if (selectedIndex >= 0) {
                joinSelectedGame(selectedIndex);
            } else {
                JOptionPane.showMessageDialog(this, "Seleziona una partita dalla lista!");
            }
        });
        panel.add(joinGameButton);
        
        return panel;
    }

    private void createAndJoinGame() {
        this.currentPlayerInfo = controller.getCurrentPlayerInfo();
        
        sudokuGrid = controller.newGame();
        currentGame = new GameInfo(
            sudokuGrid.getId(),
            40,
            currentPlayerInfo.playerName()
        );

        switchToGameScreen();

        this.selectedGridId = currentGame.gameId; // TODO: è PUBLIC!!!
        this.controller.joinGame(currentGame.gameId); //SE metti sudokuGrid da null
    }

    private void joinSelectedGame(int selectedIndex) {
        if (selectedIndex < 0 || selectedIndex >= availableGames.size()) {
            JOptionPane.showMessageDialog(this, "Seleziona una partita valida dalla lista!");
            return;
        }
        GameInfo selectedGame = availableGames.get(selectedIndex);
        
        this.selectedGridId = selectedGame.gameId;
        currentGame = selectedGame;
        
        switchToGameScreen();
        this.controller.joinGame(selectedGame.gameId);
    }
    
    private void switchToGameScreen() {
        setTitle("Cooperative Sudoku - " + currentGame.gameId);
        updateGameDisplay();
        updatePlayerInfo();
        cardLayout.show(mainPanel, "GAME");
    }
    
    public void refreshGamesList() {
        gamesListModel.clear();
        
        for (GameInfo game : availableGames) {
            gamesListModel.addElement("Partita " + game.gameId + " di " + game.creator);
        }
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
        panel.setBorder(BorderFactory.createTitledBorder(""));
        
        playerLabel = new JLabel("Nessun giocatore connesso");
        playerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(playerLabel);
        
        return panel;
    }
    
    private JPanel createGridPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
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
                if (currentPlayerInfo != null) {
                    controller.selectCell(r, c);
                }
            }
        });
        
        cell.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char keyChar = e.getKeyChar();
                if (currentPlayerInfo == null) {
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
            if (currentPlayerInfo != null) {
                this.controller.leaveGame();
            }

            this.sudokuGrid = null;
            
            setTitle("Cooperative Sudoku - Lobby");
            refreshGamesList();
            cardLayout.show(mainPanel, "LOBBY");
        }
    }
    
    private boolean setValue(int row, int col, int value) {
        if (this.controller.setCellValue(row, col, value)) {
            return true;
        }
        return false;
    }
    
    private void clearCell(int row, int col) {
        this.controller.setCellValue(row, col, -1);
    }
    
    private void clearSelectedCell() {
        if (currentPlayerInfo != null) {
            int row = currentPlayerInfo.selectedRow();
            int col = currentPlayerInfo.selectedCol();
            clearCell(row, col);
            gridCells[row][col].setText("");
            updateGameDisplay();
        }
    }
    
    private void updateGameDisplay() {
        if (sudokuGrid == null || gridCells == null) return;
        updateDisplay();
    }
    
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
        if (currentPlayerInfo != null) {
            int row = currentPlayerInfo.selectedRow();
            int col = currentPlayerInfo.selectedCol();
            Color playerColor = playerColors.get(currentPlayerInfo.playerId());
            System.out.println("Player " + currentPlayerInfo.playerId() + " selected cell: " + row + " " + col);
            if (playerColor != null && (row >= 0 && col >= 0 && row < GRID_SIZE && col < GRID_SIZE)) {
                gridCells[row][col].setBackground(playerColor);
            }
        }
    }
    
    private void updatePlayerInfo() {
        if (currentPlayerInfo != null && currentGame != null) {
            playerLabel.setText(String.format("Giocatore: %s - Partita: %s",
                currentPlayerInfo.playerName(),
                currentGame.gameId
                ));
        }
    }
    
    public void checkWin() {
        if (sudokuGrid != null && sudokuGrid.isComplete()) {
            
            JOptionPane.showMessageDialog(this, 
                "CONGRATULAZIONI!! " +
                "Sudoku completato con successo!");
        }
    }

    @Override
    public void updateView(String currentGridId, Map<String, Cell> selectedCells, List<String> availableSudokusId, SudokuGrid currentGrid) {
        // Aggiorna le informazioni del giocatore
        this.currentPlayerInfo = controller.getCurrentPlayerInfo();
        updatePlayerInfo();

        this.sudokuGrid = currentGrid;
        List<String> playerToAssignColor = selectedCells.keySet().stream().filter(id -> !playerColors.containsKey(id)).toList();
        for (String playerId : playerToAssignColor) {
            playerColors.put(playerId, PLAYER_COLORS[colorIndex % PLAYER_COLORS.length]);
            colorIndex++;
        }
        
        // Aggiorna la griglia se siamo in gioco
        if (currentGridId != null && sudokuGrid != null && sudokuGrid.getId().equals(currentGridId)) {
            updateGameDisplay();
            
            // Aggiorna i colori delle celle selezionate
            updateCellSelections(selectedCells);
        } else {
            // Aggiorna la lista dei giochi disponibili solo se siamo nella lobby
            refreshGamesList();
        }
    }
    
    /**
     * Aggiorna i colori delle celle per mostrare le selezioni dei vari giocatori
     */
    private void updateCellSelections(Map<String, Cell> selectedCells) {
        if (gridCells == null || sudokuGrid == null) return;
        
        // Reset colori per tutte le celle editabili
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (sudokuGrid.getGrid()[row][col] == null) {
                    gridCells[row][col].setBackground(Color.WHITE);
                }
            }
        }

        // Applica i colori per le celle selezionate
        for (Map.Entry<String, Cell> entry : selectedCells.entrySet()) {
            String playerId = entry.getKey();
            Cell cell = entry.getValue();
            
            // Verifica che la cella sia nella griglia corrente (usa .equals() per confrontare stringhe!)
            if (cell.sudokuId().equals(sudokuGrid.getId()) && 
                cell.row() >= 0 && cell.row() < GRID_SIZE && 
                cell.col() >= 0 && cell.col() < GRID_SIZE) {
                
                Color playerColor = playerColors.get(playerId);
                if (playerColor != null) {
                    gridCells[cell.row()][cell.col()].setBackground(playerColor);
                }
            }
        }
    }
    
    private static class GameInfo {
        public final String gameId;
        public final int difficulty;
        public String creator;

        public GameInfo(String gameId, int difficulty, String creator) {
            this.gameId = gameId;
            this.difficulty = difficulty;
            this.creator = creator;
        }
    }

    @Override
    public void addGame(String id, String creator) {
        // Verifica se la partita esiste già prima di aggiungerla
        boolean alreadyExists = availableGames.stream()
            .anyMatch(game -> game.gameId.equals(id));
        
        if (!alreadyExists) {
            this.availableGames.add(new GameInfo(id, 40, creator));
        }
    }
}