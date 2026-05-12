package ui;

import java.awt.*;
import javax.swing.*;

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
        panel.setBorder(BorderFactory.createEmptyBorder(80, 60, 80, 60));

        JLabel title = new JLabel("HelloBros");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel("Your Name:");
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel portLabel = new JLabel("Port:");
        portLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField portField = new JTextField("4999");
        portField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel hostLabel = new JLabel("Host Address (join only):");
        hostLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField addressField = new JTextField("bore.pub");
        addressField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JButton hostBtn = new JButton("Host");
        hostBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        hostBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton joinBtn = new JButton("Join");
        joinBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(portLabel);
        panel.add(portField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(hostLabel);
        panel.add(addressField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(hostBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(joinBtn);

        hostBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            if (name.isEmpty()) { JOptionPane.showMessageDialog(null, "Enter your name."); return; }
            navigator.goTo("chat", new ChatArgs(name, port, null, true));
        });

        joinBtn.addActionListener(e -> {
            String name    = nameField.getText().trim();
            int port       = Integer.parseInt(portField.getText().trim());
            String address = addressField.getText().trim();
            if (name.isEmpty()) { JOptionPane.showMessageDialog(null, "Enter your name."); return; }
            navigator.goTo("chat", new ChatArgs(name, port, address, false));
        });
    }
    @Override public void onShow(Object args) {}
    @Override public JPanel getPanel() { return panel; }
    @Override public void onHide() {}
}