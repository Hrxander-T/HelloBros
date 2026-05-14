package ui;

import network.NetworkManager;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class LobbyScreen implements Screen {

    private final Navigator navigator;
    private JPanel panel;
    private JLabel roomIDLabel;
    private JPanel roomPanel;
    private JLabel instructionLabel;
    private JLabel statusLabel;
    private Object currentArgs;

    public LobbyScreen(Navigator navigator) {
        this.navigator = navigator;
        buildPanel();
    }

    private void buildPanel() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(60, 60, 60, 60));

        // ── Title ──────────────────────────────
        JLabel title = new JLabel("What do you want to do?");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Room ID panel (host only) ──────────
        JLabel roomIDTitle = new JLabel("Room ID:");
        roomIDTitle.setFont(new Font("Arial", Font.BOLD, 14));
        roomIDTitle.setForeground(new Color(90, 90, 90));

        roomIDLabel = new JLabel("", SwingConstants.CENTER);
        roomIDLabel.setFont(new Font("Arial", Font.BOLD, 22));
        roomIDLabel.setForeground(new Color(40, 40, 40));

        JButton copyBtn = new JButton("Copy");
        copyBtn.setFocusPainted(false);
        copyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        copyBtn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        copyBtn.addActionListener(e -> {
            Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(roomIDLabel.getText()), null);
            copyBtn.setText("Copied!");
            Timer timer = new Timer(1200, ev -> copyBtn.setText("Copy"));
            timer.setRepeats(false);
            timer.start();
        });

        roomPanel = new JPanel(new BorderLayout(12, 0));
        roomPanel.setBackground(new Color(250, 250, 250));
        roomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        roomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        roomPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        roomPanel.add(roomIDTitle,  BorderLayout.WEST);
        roomPanel.add(roomIDLabel,  BorderLayout.CENTER);
        roomPanel.add(copyBtn,      BorderLayout.EAST);
        roomPanel.setVisible(false);

        instructionLabel = new JLabel("Share this Room ID with your friend");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        instructionLabel.setForeground(new Color(130, 130, 130));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructionLabel.setVisible(false);

        // ── Status ─────────────────────────────
        statusLabel = new JLabel("Starting tunnel...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(150, 150, 150));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setVisible(false);

        // ── Buttons ────────────────────────────
        JButton chatBtn = new JButton("Chat", Assets.SPEECH_BUBBLE);
        chatBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        chatBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton gameBtn = new JButton("🎮 Tic Tac Toe");
        gameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        gameBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton backBtn = new JButton("← Back");
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // ── Layout ─────────────────────────────
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(roomPanel);
        panel.add(instructionLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(chatBtn);
        panel.add(Box.createVerticalStrut(15));
        panel.add(gameBtn);
        panel.add(Box.createVerticalStrut(30));
        panel.add(backBtn);

        // ── Actions ────────────────────────────
        chatBtn.addActionListener(e -> navigator.goTo("chat", currentArgs));
        gameBtn.addActionListener(e -> navigator.goTo("game", currentArgs));
        backBtn.addActionListener(e -> {
            NetworkManager.reset();
            navigator.goTo("startup");
        });
    }

    // ── Public API ─────────────────────────
    public void showRoomID(String roomID) {
        SwingUtilities.invokeLater(() -> {
            roomIDLabel.setText(roomID);
            roomPanel.setVisible(true);
            instructionLabel.setVisible(true);
            statusLabel.setVisible(false);
            panel.revalidate();
            panel.repaint();
        });
    }

    public void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(msg);
            statusLabel.setVisible(true);
        });
    }

    @Override public JPanel getPanel() { return panel; }

    @Override
    public void onShow(Object args) {
        if (args != null) currentArgs = args;
        // if host and tunnel already ready, show room ID
        String roomID = NetworkManager.getRoomID();
        if (roomID != null) {
            showRoomID(roomID);
        } else if (NetworkManager.isHost()) {
            setStatus("Starting tunnel...");
        }
    }

    @Override public void onHide() {}



    
}