package pcd.ass03.part2B.view;

import pcd.ass03.part2B.controller.SudokuController;
import pcd.ass03.part2B.controller.SudokuControllerImpl;
import pcd.ass03.part2B.model.Cell;
import pcd.ass03.part2B.model.GameInfo;
import pcd.ass03.part2B.model.PlayerInfo;
import pcd.ass03.part2B.model.SudokuGrid;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class SudokuGUIimpl extends JFrame implements SudokuGUI {
    private static final int GRID_SIZE = 9;
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color GRID_COLOR = new Color(120, 120, 120);
    private static final Color PLAYER_COLORS[] = {
        new Color(255, 182, 193),
        new Color(144, 238, 144), 
        new Color(255, 218, 185),
        new Color(221, 160, 221),
        new Color(255, 255, 224)
    };
    
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private SudokuController controller;
    
    // Lobby
    private JList<String> gamesList;
    private DefaultListModel<String> gamesListModel;
    private List<GameInfo> availableGames;
    
    // Game screen
    private SudokuGrid sudokuGrid;
    private PlayerInfo currentPlayerInfo;
    private JTextField[][] gridCells;
    private JLabel playerLabel;
    private JButton clearCellButton;
    private JButton backToLobbyButton;
    private GameInfo currentGame;
    private JLabel winLabel;
    
    private Map<String, Color> playerColors;
    private int colorIndex = 0;
    
    public SudokuGUIimpl(SudokuController controller) {
        this.controller = controller;
        this.currentPlayerInfo = controller.getCurrentPlayerInfo();
        
        if (controller instanceof SudokuControllerImpl) {
            ((SudokuControllerImpl) controller).setView(this);
        }
        
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("Cooperative Sudoku - Lobby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        JPanel lobbyPanel = createLobbyPanel();
        JPanel gamePanel = createGamePanel();
        
        mainPanel.add(lobbyPanel, "LOBBY");
        mainPanel.add(gamePanel, "GAME");
        
        add(mainPanel);
        
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
        
        JPanel headerPanel = createLobbyHeader();
        panel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = createGamesListPanel();
        panel.add(centerPanel, BorderLayout.CENTER);
        
        JPanel footerPanel = createLobbyControls();
        panel.add(footerPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createLobbyHeader() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(BACKGROUND_COLOR);
        
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
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> gamesList = new JList<>(listModel);
        gamesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gamesList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        gamesList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
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
        
        JButton createGameButton = new JButton("Crea Partita");
        createGameButton.setPreferredSize(new Dimension(120, 35));
        createGameButton.addActionListener(e -> createAndJoinGame());
        panel.add(createGameButton);
        
        JButton joinGameButton = new JButton("Entra");
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
            currentPlayerInfo.playerName()
        );

        switchToGameScreen();
        this.controller.joinGame(currentGame.gameId());
    }

    private void joinSelectedGame(int selectedIndex) {
        if (selectedIndex < 0 || selectedIndex >= availableGames.size()) {
            JOptionPane.showMessageDialog(this, "Seleziona una partita valida dalla lista!");
            return;
        }
        currentGame = availableGames.get(selectedIndex);

        switchToGameScreen();
        this.controller.joinGame(currentGame.gameId());
    }
    
    private void switchToGameScreen() {
        setTitle("Cooperative Sudoku - " + currentGame.gameId());
        updateGameDisplay();
        updatePlayerInfo();
        cardLayout.show(mainPanel, "GAME");
    }
    
    public void refreshGamesList() {
        gamesListModel.clear();
        
        for (GameInfo game : availableGames) {
            gamesListModel.addElement("Partita " + game.gameId() + " di " + game.creator());
        }
    }
    
    private JPanel createGamePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setPreferredSize(new Dimension(800, 600));
        
        JPanel headerPanel = createGameHeader();
        panel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel gridPanel = createGridPanel();
        panel.add(gridPanel, BorderLayout.CENTER);
        
        JPanel sidePanel = createGameSidePanel();
        panel.add(sidePanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createGameHeader() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(""));
        
        playerLabel = new JLabel("");
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
        
        Border border;
        int top = (row % 3 == 0 && row != 0) ? 3 : 1;
        int left = (col % 3 == 0 && col != 0) ? 3 : 1;
        int bottom = (row == 8) ? 3 : 1;
        int right = (col == 8) ? 3 : 1;
        border = BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK);
        cell.setBorder(border);
        
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
                
                // Check if this cell is selected by the current player
                if (currentPlayerInfo.selectedRow() != r || currentPlayerInfo.selectedCol() != c) {
                    e.consume();
                    return;
                }
                
                if (keyChar >= '1' && keyChar <= '9') {
                    int value = keyChar - '0';
                    if (controller.setCellValue(r, c, value)) {
                        cell.setText(String.valueOf(value));
                        updateGameDisplay();
                    } else {
                        e.consume();
                        updatePlayerInfo();
                    }
                } else if (keyChar == KeyEvent.VK_DELETE || keyChar == '0') {
                    controller.setCellValue(r, c, -1);
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

        this.winLabel = new JLabel("");
        winLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        winLabel.setForeground(new Color(34, 139, 34));
        gbc.gridy = 3;
        panel.add(winLabel, gbc);
        
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

    private void clearSelectedCell() {
        if (currentPlayerInfo != null) {
            int row = currentPlayerInfo.selectedRow();
            int col = currentPlayerInfo.selectedCol();
            if (row > -1 && col > -1) {
                controller.setCellValue(row, col, -1);
                gridCells[row][col].setText("");
                updateGameDisplay();
            }
        }
    }

    private void updateGameDisplay() {
        if (sudokuGrid == null || gridCells == null) return;
        checkWin();
        updateDisplay();
    }
    
    private void updateDisplay() {
        if (sudokuGrid == null || gridCells == null) return;
        Integer[][] grid = sudokuGrid.getGrid();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                JTextField cell = gridCells[row][col];
                Integer value = grid[row][col];
                
                if (value != null) {
                    cell.setText(String.valueOf(value));
                    cell.setEditable(false);
                } else {
                    cell.setText("");
                    cell.setEditable(true);
                }
            }
        }
    }
    
    private void updatePlayerInfo() {
        if (currentPlayerInfo != null && currentGame != null) {
            playerLabel.setText(String.format("Giocatore: %s - Partita: %s",
                currentPlayerInfo.playerName(),
                currentGame.gameId()
            ));
        }
    }
    
    public void checkWin() {
        if (controller.isSudokuComplete()) {
            clearCellButton.setEnabled(false);
            winLabel.setText("Sudoku completato!!");
        } else {
            clearCellButton.setEnabled(true);
            winLabel.setText("");
        }
    }

    @Override
    public void updateView(Map<String, Cell> selectedCells, SudokuGrid currentGrid) {
        this.currentPlayerInfo = controller.getCurrentPlayerInfo();
        updatePlayerInfo();

        this.sudokuGrid = currentGrid;
        // Assign colors to new players
        List<String> playerToAssignColor = selectedCells.keySet().stream().filter(id -> !playerColors.containsKey(id)).toList();
        for (String playerId : playerToAssignColor) {
            playerColors.put(playerId, PLAYER_COLORS[colorIndex % PLAYER_COLORS.length]);
            colorIndex++;
        }

        // If we are in game, update the grid
        // else update games list
        if (sudokuGrid != null) {
            updateGameDisplay();
            updateCellSelections(selectedCells);
        } else {
            refreshGamesList();
        }
    }
    
    private void updateCellSelections(Map<String, Cell> selectedCells) {
        if (gridCells == null || sudokuGrid == null) return;
        
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                gridCells[row][col].setBackground(Color.WHITE);
            }
        }

        // Apply player colors to selected cells
        for (Map.Entry<String, Cell> entry : selectedCells.entrySet()) {
            String playerId = entry.getKey();
            Cell cell = entry.getValue();
            
            // Verify that the cell belongs to the current sudoku grid
            if (cell.row() >= 0 && cell.row() < GRID_SIZE && 
                cell.col() >= 0 && cell.col() < GRID_SIZE) {
                
                Color playerColor = playerColors.get(playerId);
                if (playerColor != null) {
                    gridCells[cell.row()][cell.col()].setBackground(playerColor);
                }
            }
        }
    }

    @Override
    public void addGame(String id, String creator) {
        boolean alreadyExists = availableGames.stream()
            .anyMatch(game -> game.gameId().equals(id));
        
        if (!alreadyExists) {
            this.availableGames.add(new GameInfo(id, creator));
        }
        refreshGamesList();
    }
}