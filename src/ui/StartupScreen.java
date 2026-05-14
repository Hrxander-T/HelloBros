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
        panel.setBorder(BorderFactory.createEmptyBorder(80, 60, 80, 60));

        JLabel title = new JLabel("HelloBros");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel("Your Name:");
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
        namePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JTextField nameField = new JTextField(NameGenerator.generate());
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JButton randomBtn = new JButton(Assets.DICE);
        randomBtn.setMargin(new Insets(0, 8, 0, 8));
        randomBtn.setFocusable(false);
        randomBtn.addActionListener(e -> nameField.setText(NameGenerator.generate()));

        namePanel.add(nameField);
        namePanel.add(randomBtn);

        JLabel portLabel = new JLabel("Port:");
        portLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField portField = new JTextField("4999");
        portField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel hostLabel = new JLabel("Connection Type:");
        hostLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JComboBox<String> addressBox = new JComboBox<>(new String[] { "Local", "Remote" });
        addressBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        addressBox.setSelectedIndex(1);

        JButton hostBtn = new JButton("Host");
        hostBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        hostBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton joinBtn = new JButton("Join");
        joinBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        panel.add(nameLabel);
        panel.add(namePanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(portLabel);
        panel.add(portField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(hostLabel);
        panel.add(addressBox);
        panel.add(Box.createVerticalStrut(20));
        panel.add(hostBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(joinBtn);

        hostBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Enter your name.");
                return;
            }
            LobbyArgs args = new LobbyArgs(name, port, null, true);
            NetworkManager.connect(args);
            navigator.goTo("lobby", new LobbyArgs(name, port, null, true));
        });

        joinBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            String address = addressBox.getSelectedItem().equals("Local") ? "localhost" : "bore.pub";
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Enter your name.");
                return;
            }
            LobbyArgs args = new LobbyArgs(name, port, address, false);

            NetworkManager.connect(args);
            navigator.goTo("lobby", new LobbyArgs(name, port, address, false));
        });
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