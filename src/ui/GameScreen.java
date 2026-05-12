package ui;

import game.TicTacToe;
import game.TicTacToe.Player;
import java.awt.*;
import javax.swing.*;

public class GameScreen implements Screen {

    private final Navigator navigator;
    private JPanel panel;
    private final JButton[][] cells = new JButton[3][3];
    private JLabel statusLabel;
    private TicTacToe game;

    private Player mySymbol;      // X or O
    private boolean myTurn;
    private String name;
    private boolean isHost;

    // called when we need to send a move over network
    protected void onMove(int row, int col) {}

    public GameScreen(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public JPanel getPanel() { return panel; }

    @Override
    public void onShow(Object args) {
        LobbyArgs a = (LobbyArgs) args;
        this.name    = a.name;
        this.isHost  = a.isHost;
        this.mySymbol = isHost ? Player.X : Player.O;
        this.myTurn   = isHost; // X goes first, host is X
        this.game     = new TicTacToe();
        buildPanel();
        updateStatus();
    }

    @Override
    public void onHide() {}

    private void buildPanel() {
        panel = new JPanel(new BorderLayout());

        // ── Header ─────────────────────────────
        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> navigator.goTo("lobby"));

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.add(backBtn,     BorderLayout.WEST);
        header.add(statusLabel, BorderLayout.CENTER);

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
        resetBtn.addActionListener(e -> resetGame());
        JPanel footer = new JPanel();
        footer.add(resetBtn);

        panel.add(header,     BorderLayout.NORTH);
        panel.add(boardPanel, BorderLayout.CENTER);
        panel.add(footer,     BorderLayout.SOUTH);
    }

    private void handleCellClick(int row, int col) {
        if (!myTurn || game.isGameOver()) return;
        if (game.getCell(row, col) != Player.NONE) return;

        applyMove(row, col, mySymbol);
        onMove(row, col); // send to opponent
    }

    // apply a move — called locally and from network
    public void applyMove(int row, int col, Player symbol) {
        game.makeMove(row, col);
        updateCell(row, col, symbol);

        Player winner = game.checkWinner();
        if (winner != null) {
            if (winner == Player.NONE) {
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
        Player opponentSymbol = (mySymbol == Player.X) ? Player.O : Player.X;
        applyMove(row, col, opponentSymbol);
        myTurn = true;
        updateStatus();
    }

    private void updateCell(int row, int col, Player symbol) {
        SwingUtilities.invokeLater(() -> {
            JButton cell = cells[row][col];
            cell.setText(symbol == Player.X ? "X" : "O");
            cell.setForeground(symbol == Player.X ? Color.BLUE : Color.RED);
            cell.setEnabled(false);
        });
    }

    private void updateStatus() {
        SwingUtilities.invokeLater(() -> {
            if (game.isGameOver()) return;
            statusLabel.setText(myTurn ? "Your turn (" + mySymbol + ")" : "Opponent's turn...");
        });
    }

    private void resetGame() {
        game.reset();
        myTurn = isHost;
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++) {
                cells[r][c].setText("");
                cells[r][c].setEnabled(true);
            }
        updateStatus();
    }
}
