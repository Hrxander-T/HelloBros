package ui;

import game.TicTacToe;
import game.TicTacToe.Mark;
import java.awt.*;
import javax.swing.*;
import network.NetworkManager;

public class GameScreen implements Screen {

    private final Navigator navigator;
    private final JButton[][] cells = new JButton[3][3];
    private JPanel panel;
    private JLabel statusLabel;
    private JLabel connectionLabel;
    private TicTacToe game;

    private Mark mySymbol; // X or O
    private boolean myTurn;
    private String name;
    private boolean isHost;
    private LobbyArgs lobbyArgs;

    // called when we need to send a move over network
    protected void onMove(int row, int col) {
    }

    public GameScreen(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void onShow(Object args) {
        if (args == null)
            return;
        LobbyArgs a = (LobbyArgs) args;
        this.lobbyArgs = a; // store it
        this.name = a.name;
        this.isHost = a.isHost;
        this.mySymbol = isHost ? Mark.X : Mark.O;
        this.myTurn = isHost;
        this.game = new TicTacToe();
        buildPanel();
        updateStatus();
    }

    @Override
    public void onHide() {
    }

    private void buildPanel() {
        panel = new JPanel(new BorderLayout());

        // ── Header ─────────────────────────────
        JButton backBtn = new JButton("← Back");
        backBtn.setFocusPainted(false);
        backBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        backBtn.addActionListener(e -> navigator.goTo("lobby", lobbyArgs));

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(new Color(33, 33, 33));
        connectionLabel = new JLabel("Waiting...", Assets.HOURGLASS, SwingConstants.CENTER);
        connectionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        connectionLabel.setForeground(new Color(120, 120, 120));

        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        header.setBackground(new Color(245, 245, 245));
        header.add(backBtn, BorderLayout.WEST);
        header.add(statusLabel, BorderLayout.CENTER);
        header.add(connectionLabel, BorderLayout.EAST);

        // ── Board ──────────────────────────────
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        boardPanel.setBackground(Color.DARK_GRAY);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                JButton cell = new JButton("");
                cell.setFont(new Font("Arial", Font.BOLD, 48));
                cell.setBackground(Color.WHITE);
                cell.setFocusPainted(false);

                final int row = r, col = c;
                cell.addActionListener(e -> handleCellClick(row, col));
                cells[r][c] = cell;
                boardPanel.add(cell);
            }
        }

        // ── Footer ─────────────────────────────
        JButton resetBtn = new JButton("New Game");
        resetBtn.addActionListener(e -> {
            if (!game.isGameOver()) {
                int confirm = JOptionPane.showConfirmDialog(panel,
                        "Game is still in progress. Request new game anyway?",
                        "New Game", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION)
                    return;
            }
            appendStatus("-- Requesting new game... --");
            NetworkManager.sendGameRequest();
        });

        JPanel footer = new JPanel();
        footer.add(resetBtn);

        panel.add(header, BorderLayout.NORTH);
        panel.add(boardPanel, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);
    }

    public void setConnectionStatus(String msg) {
        if (connectionLabel == null)
            return;
        SwingUtilities.invokeLater(() -> {
            connectionLabel.setIcon(null);
            connectionLabel.setText(msg);
            if (msg.contains("connected")) {
                connectionLabel.setText("● Connected");
                connectionLabel.setForeground(new Color(34, 139, 34));
            } else if (msg.contains("disconnected")) {
                connectionLabel.setText("○ Disconnected");
                connectionLabel.setForeground(new Color(178, 34, 34));
            } else {
                connectionLabel.setIcon(Assets.HOURGLASS);
                connectionLabel.setText(" " + msg);
                connectionLabel.setForeground(new Color(120, 120, 120));
            }
        });
    }

    // remove onMove() override, replace with:
    private void handleCellClick(int row, int col) {
        System.out.println("Sending move: " + row + "," + col + " isHost=" + NetworkManager.isHost());

        if (!myTurn || game.isGameOver())
            return;
        if (game.getCell(row, col) != Mark.NONE)
            return;
        applyMove(row, col, mySymbol);
        NetworkManager.sendMove(row, col);
    }

    // apply a move — called locally and from network
    public void applyMove(int row, int col, Mark symbol) {
        game.makeMove(row, col);
        updateCell(row, col, symbol);

        Mark winner = game.checkWinner();
        if (winner != null) {
            if (winner == Mark.NONE) {
                statusLabel.setText("Draw!");
            } else {
                statusLabel.setText(winner == mySymbol ? "You win! 🎉" : "You lose!");
            }
            return;
        }

        myTurn = !myTurn;
        updateStatus();
    }

    // called when opponent sends a move
    public void receiveMove(int row, int col) {
        Mark opponentSymbol = (mySymbol == Mark.X) ? Mark.O : Mark.X;
        applyMove(row, col, opponentSymbol);
        myTurn = true;
        updateStatus();
    }

    private void updateCell(int row, int col, Mark symbol) {
        SwingUtilities.invokeLater(() -> {
            JButton cell = cells[row][col];
            cell.setText(symbol == Mark.X ? "X" : "O");
            cell.setForeground(symbol == Mark.X ? Color.BLUE : Color.RED);
            cell.setEnabled(false);
        });
    }

    private void updateStatus() {
        SwingUtilities.invokeLater(() -> {
            if (game.isGameOver())
                return;
            if (myTurn) {
                statusLabel.setText(name + "'s turn");
                statusLabel.setForeground(mySymbol == Mark.X ? new Color(0, 102, 204) : new Color(204, 51, 0));
            } else {
                statusLabel.setText("Opponent's turn...");
                statusLabel.setForeground(new Color(100, 100, 100));
            }
        });
    }

    // opponent requested new game
    public void receiveGameRequest() {
        int response = JOptionPane.showConfirmDialog(panel,
                "Opponent wants a new game. Accept?",
                "New Game Request", JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            NetworkManager.sendGameAccept();
        } else {
            NetworkManager.sendGameDecline();
        }
    }

    // opponent accepted — first = "X" or "O"
    public void receiveGameAccept(boolean goFirst) {
        myTurn = goFirst;
        game.reset();
        clearBoard();
        appendStatus("-- Opponent accepted. New game starting! --");
        // resetGame();
    }

    // opponent declined
    public void receiveGameDecline() {
        appendStatus("-- Opponent declined new game request. --");
    }

    // requester also needs to know who goes first
    // so send first back to yourself via the signal
    private void resetGame() {
        game.reset();
        myTurn = isHost;
        clearBoard();
        updateStatus();
    }

    // add status message below board
    private void appendStatus(String msg) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(msg));
    }

    // also update receiveReset to carry first:
    public void receiveReset() {
        // kept for backward compat — default host goes first
        resetGame();
    }

    // extract board clear into method
    private void clearBoard() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++) {
                cells[r][c].setText("");
                cells[r][c].setEnabled(true);
                cells[r][c].setBackground(Color.WHITE);
            }
    }
}
