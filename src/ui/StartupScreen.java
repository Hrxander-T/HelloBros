package ui;

import java.awt.*;
import javax.swing.*;
import network.NetworkManager;
import util.NameGenerator;

public class StartupScreen implements Screen {

    private final Navigator navigator;
    private JPanel panel;

    public StartupScreen(Navigator navigator) {
        this.navigator = navigator;
        buildPanel();
    }

    private void buildPanel() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // ── Title ──────────────────────────────
        JLabel title = new JLabel("HelloBros");
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Connect with your bros");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(new Color(130, 130, 130));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Name field ─────────────────────────
        JTextField nameField = new JTextField(NameGenerator.generate());
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        nameField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton randomBtn = new JButton(Assets.DICE);
        randomBtn.setFocusable(false);
        randomBtn.setMargin(new Insets(0, 8, 0, 8));
        randomBtn.addActionListener(e -> nameField.setText(NameGenerator.generate()));

        JPanel namePanel = new JPanel(new BorderLayout(5, 0));
        namePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        namePanel.add(nameField, BorderLayout.CENTER);
        namePanel.add(randomBtn, BorderLayout.EAST);

        JLabel nameLabel = new JLabel("Your Name");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        nameLabel.setForeground(new Color(100, 100, 100));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Host button ────────────────────────
        JButton hostBtn = new JButton("Host a Room");
        hostBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        hostBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        hostBtn.setFont(new Font("Arial", Font.BOLD, 14));
        hostBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        hostBtn.setFocusPainted(false);
        

        // ── Divider ────────────────────────────
        JPanel divider = new JPanel(new BorderLayout(8, 0));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        divider.setOpaque(false);
        JSeparator leftLine = new JSeparator();
        JSeparator rightLine = new JSeparator();
        JLabel orLabel = new JLabel("or");
        orLabel.setForeground(new Color(150, 150, 150));
        orLabel.setFont(new Font("Arial", Font.BOLD, 12));
        orLabel.setHorizontalAlignment(SwingConstants.CENTER);
        divider.add(leftLine, BorderLayout.WEST);
        divider.add(orLabel, BorderLayout.CENTER);
        divider.add(rightLine, BorderLayout.EAST);

        // ── Room ID field ──────────────────────
        JLabel roomLabel = new JLabel("Room ID");
        roomLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        roomLabel.setForeground(new Color(100, 100, 100));
        roomLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField roomField = new JTextField();
        roomField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        roomField.setFont(new Font("Arial", Font.PLAIN, 14));
        roomField.setToolTipText("e.g. X4K9P2");

        // ── Join button ────────────────────────
        JButton joinBtn = new JButton("Join Room");
        joinBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        joinBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        joinBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        joinBtn.setFocusPainted(false);

        // ── Layout ─────────────────────────────
        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(30));
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(namePanel);
        panel.add(Box.createVerticalStrut(16));
        panel.add(hostBtn);
        panel.add(Box.createVerticalStrut(16));
        panel.add(divider);
        panel.add(Box.createVerticalStrut(16));
        panel.add(roomLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(roomField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(joinBtn);

        // ── Actions ────────────────────────────
        hostBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Enter your name.");
                return;
            }
            LobbyArgs args = new LobbyArgs(name, 4999, null, true);
            NetworkManager.connect(args);
            navigator.goTo("lobby", args);
        });

        joinBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String roomID = roomField.getText().trim().toUpperCase();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Enter your name.");
                return;
            }
            if (roomID.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Enter Room ID.");
                return;
            }
            if (roomID.length() != 6) {
                JOptionPane.showMessageDialog(null, "Room ID must be 6 characters.");
                return;
            }
            LobbyArgs args = new LobbyArgs(name, 0, roomID, false);
            NetworkManager.connect(args);
            navigator.goTo("lobby", args);
        });

        // pressing Enter in room field triggers join
        roomField.addActionListener(e -> joinBtn.doClick());
    }

    @Override
    public void onShow(Object args) {
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void onHide() {
    }
}