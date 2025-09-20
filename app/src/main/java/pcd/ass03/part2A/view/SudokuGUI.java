package pcd.ass03.part2A.view;

import pcd.ass03.part2A.model.Player;
import pcd.ass03.part2A.model.SudokuFactory;
import pcd.ass03.part2A.model.SudokuGrid;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

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
    
    private SudokuGrid sudokuGrid;
    private Player currentPlayer;
    private JTextField[][] gridCells;
    private JLabel playerLabel;
    private JButton newGameButton;
    private JButton clearCellButton;
    private JTextField playerNameField;
    private JButton joinButton;
    
    // Per simulare altri giocatori (in un sistema distribuito questi arriverebbero dalla rete)
    private Map<String, Color> playerColors;
    private int colorIndex = 0;
    
    public SudokuGUI() {
        initializeGUI();
        initializeGame();
    }
    
    private void initializeGUI() {
        setTitle("Cooperative Sudoku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);
        getContentPane().setPreferredSize(new Dimension(800, 600));
        
        // Panel superiore con controlli giocatore
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Panel centrale con la griglia Sudoku
        JPanel gridPanel = createGridPanel();
        add(gridPanel, BorderLayout.CENTER);
        
        // Panel laterale con controlli di gioco
        JPanel sidePanel = createSidePanel();
        add(sidePanel, BorderLayout.EAST);
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        
        playerColors = new HashMap<>();
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder("Player Login"));
        
        panel.add(new JLabel("Nome:"));
        playerNameField = new JTextField(15);
        panel.add(playerNameField);
        
        joinButton = new JButton("Entra nel Gioco");
        joinButton.addActionListener(e -> joinGame());
        panel.add(joinButton);
        
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
                        updateDisplay();
                        checkWin();
                    } else {
                        e.consume();
                        updatePlayerInfo();
                    }
                } else if (keyChar == KeyEvent.VK_BACK_SPACE || keyChar == KeyEvent.VK_DELETE || keyChar == '0') {
                    clearCell(r, c);
                    cell.setText("");
                    updateDisplay();
                } else {
                    e.consume();
                }
            }
        });
        
        return cell;
    }
    
    private JPanel createSidePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder("Controlli"));
        panel.setPreferredSize(new Dimension(250, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        
        newGameButton = new JButton("Nuova Partita");
        newGameButton.addActionListener(e -> newGame());
        gbc.gridy = 0;
        panel.add(newGameButton, gbc);
        
        clearCellButton = new JButton("Cancella Cella");
        clearCellButton.addActionListener(e -> clearSelectedCell());
        clearCellButton.setEnabled(false);
        gbc.gridy = 1;
        panel.add(clearCellButton, gbc);
                
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        return panel;
    }
    
    private void initializeGame() {
        SudokuFactory factory = new SudokuFactory();
        sudokuGrid = factory.generate(40); // 40 celle vuote
        updateDisplay();
    }
    
    private void joinGame() {
        String playerName = playerNameField.getText().trim();
        if (playerName.isEmpty()) {
            return;
        }
        
        currentPlayer = new Player(playerName);
        currentPlayer.joinGrid("main_grid");
        
        // Assegna colore al giocatore
        playerColors.put(currentPlayer.getPlayerId(), PLAYER_COLORS[colorIndex % PLAYER_COLORS.length]);
        colorIndex++;
        
        updatePlayerInfo();
        clearCellButton.setEnabled(true);
        playerNameField.setEnabled(false);
        joinButton.setText("Giocatore Connesso");
        joinButton.setEnabled(false);
        
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
        currentPlayer.selectCell(row, col);
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
            updateDisplay();
        }
    }
    
    private void newGame() {
        initializeGame();
        if (currentPlayer != null) {
            updatePlayerInfo();
        }
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
        if (currentPlayer != null) {
            playerLabel.setText("Giocatore: " + currentPlayer.getPlayerName() + 
                               " [" + currentPlayer.getPlayerId() + "]");
        }
    }
    
    private void checkWin() {
        if (sudokuGrid.isComplete()) {
            JOptionPane.showMessageDialog(this, 
                "Sudoku risolto con successo!\n",
                "VITTORIA!", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
}