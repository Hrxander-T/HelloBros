package ui;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import javax.swing.*;

public class LobbyScreen implements Screen {

    private final Navigator navigator;
    private JPanel panel;
    private JPanel tunnelPanel;
    private JLabel tunnelLabel;
    private JLabel instructionLabel;

    public LobbyScreen(Navigator navigator) {
        this.navigator = navigator;
        buildPanel();
    }

    public void showTunnelInfo(String address) {
        SwingUtilities.invokeLater(() -> {
            tunnelLabel.setText(address);
            tunnelPanel.setVisible(true);
            instructionLabel.setVisible(true);
            panel.revalidate();
            panel.repaint();
        });
    }

    private void buildPanel() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(80, 60, 80, 60));

        // -------------------------------------------------
        JLabel portTitle = new JLabel("Port:");
        portTitle.setFont(new Font("Arial", Font.BOLD, 14));
        portTitle.setForeground(new Color(90, 90, 90));
        tunnelLabel = new JLabel("", SwingConstants.CENTER);
        tunnelLabel.setFont(new Font("Arial", Font.BOLD, 14));
        tunnelLabel.setForeground(new Color(40, 40, 40));

        JButton copyBtn = new JButton("Copy");
        copyBtn.setFont(new Font("Arial", Font.BOLD, 12));
        copyBtn.setFocusPainted(false);
        copyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // copyBtn.setBackground(new Color(60, 120, 220));
        // copyBtn.setForeground(Color.WHITE);

        copyBtn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        copyBtn.addActionListener(e -> {
            String port = tunnelLabel.getText();

            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(port), null);

            copyBtn.setText("Copied!");

            Timer timer = new Timer(1200, ev -> copyBtn.setText("Copy"));
            timer.setRepeats(false);
            timer.start();
        });

        tunnelPanel = new JPanel(new BorderLayout(12, 0));

        tunnelPanel.setBackground(new Color(250, 250, 250));

        tunnelPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        tunnelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        tunnelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        tunnelPanel.add(portTitle, BorderLayout.WEST);
        tunnelPanel.add(tunnelLabel, BorderLayout.CENTER);
        tunnelPanel.add(copyBtn, BorderLayout.EAST);

        instructionLabel = new JLabel(
                "Share this address with your friends so they can join");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        instructionLabel.setForeground(new Color(130, 130, 130));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        tunnelPanel.setVisible(false);
        instructionLabel.setVisible(false);

        // -----------------------------------------------
        JLabel title = new JLabel("What do you want to do?");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        ImageIcon original = new ImageIcon("assets/speech_bubble.png");

        Image scaled = original.getImage().getScaledInstance(
                18,
                18,
                Image.SCALE_SMOOTH);

        ImageIcon chatIcon = new ImageIcon(scaled);

        JButton chatBtn = new JButton("Chat", chatIcon);
        chatBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        chatBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton gameBtn = new JButton("🎮 Tic Tac Toe");
        gameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        gameBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton backBtn = new JButton("← Back");
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(tunnelPanel);
        panel.add(instructionLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(chatBtn);
        panel.add(Box.createVerticalStrut(15));
        panel.add(gameBtn);
        panel.add(Box.createVerticalStrut(30));
        panel.add(backBtn);

        chatBtn.addActionListener(e -> {
            navigator.goTo("chat", currentArgs);
        });

        gameBtn.addActionListener(e -> {
            navigator.goTo("game", currentArgs);
        });
        backBtn.addActionListener(e -> navigator.goTo("startup"));
    }

    private Object currentArgs;

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void onShow(Object args) {
        if (args != null)
            currentArgs = args;

    }

    @Override
    public void onHide() {
    }
}