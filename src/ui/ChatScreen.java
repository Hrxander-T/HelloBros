package ui;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.ChatArgs;
import model.LobbyArgs;
import network.NetworkManager;

public class ChatScreen implements Screen {

    private final Navigator navigator;

    // UI components — built once, reused on every visit
    private JPanel panel;
    private JTextField inputField;
    private JButton sendBtn;
    private JButton reconnectBtn;
    private JPanel tunnelPanel;
    private JLabel tunnelLabel;

    // Add these:
    private JPanel messageList;
    private JScrollPane scrollPane;
    private final Map<String, MessagePanel> messageMap = new LinkedHashMap<>();

    private String name;
    private Object currentArgs;
    private boolean panelBuilt = false; // build panel only once

    public ChatScreen(Navigator navigator) {
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
        currentArgs = args;

        String incoming = switch (args) {
            case LobbyArgs a -> a.name;
            case ChatArgs a -> a.name;
            default -> "";
        };

        if (!panelBuilt) {
            name = incoming;
            buildPanel();
            panelBuilt = true;
            appendMessage("-- Connected as " + name + " --");
        }
    }

    @Override
    public void onHide() {
    } // network owned by NetworkManager, nothing to clean here

    // ── Build UI (once) ────────────────────
    private void buildPanel() {
        panel = new JPanel(new BorderLayout());

        // tunnel bar
        tunnelLabel = new JLabel("");
        tunnelLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        JButton copyBtn = new JButton("Copy");
        copyBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        copyBtn.addActionListener(e -> {
            String text = tunnelLabel.getText();
            String port = text.contains(":") ? text.split(":")[1].trim() : text;
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(port), null);
        });

        tunnelPanel = new JPanel(new BorderLayout());
        tunnelPanel.setBackground(new Color(230, 245, 255));
        tunnelPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        tunnelPanel.add(new JLabel("Share: "), BorderLayout.WEST);
        tunnelPanel.add(tunnelLabel, BorderLayout.CENTER);
        tunnelPanel.add(copyBtn, BorderLayout.EAST);
        tunnelPanel.setVisible(false);

        // header
        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> navigator.goTo("lobby", currentArgs));

        JPanel header = new JPanel(new BorderLayout());
        header.add(backBtn, BorderLayout.WEST);
        header.add(tunnelPanel, BorderLayout.CENTER);

        // chat area
        messageList = new JPanel();
        messageList.setLayout(new BoxLayout(messageList, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(messageList);

        // input area
        inputField = new JTextField();
        sendBtn = new JButton("Send");
        reconnectBtn = new JButton("Reconnect");
        JButton fileBtn = new JButton(Assets.PAPERCLIP);
        reconnectBtn.setVisible(false);

        JPanel rightBtns = new JPanel(new BorderLayout());
        rightBtns.add(fileBtn, BorderLayout.WEST);
        rightBtns.add(sendBtn, BorderLayout.EAST);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(reconnectBtn, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(rightBtns, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        // ── Actions ────────────────────────────
        Runnable send = () -> {
            String msg = inputField.getText().trim();
            if (msg.isEmpty())
                return;
            String id = String.valueOf(System.currentTimeMillis());
            appendMessage(id + "|[" + name + "]: " + msg);
            NetworkManager.sendMessage(name, id, msg);
            inputField.setText("");
        };

        sendBtn.addActionListener(e -> send.run());
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    send.run();
            }
        });

        fileBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                appendMessage("-- Sending: " + file.getName() + " --");
                NetworkManager.sendFile(file);
            }
        });

        reconnectBtn.addActionListener(e -> {
            setConnected(true);
            appendMessage("-- Reconnecting... --");
            // NetworkManager handles reconnect
        });
    }

    // ── Public API ─────────────────────────
    public void appendMessage(String msg) {
        if (messageList == null)
            return;
        SwingUtilities.invokeLater(() -> {
            String id;
            String displayText;

            if (msg.contains("|")) {
                String[] parts = msg.split("\\|", 2);
                id = parts[0];
                displayText = parts[1];
            } else {
                // system messages like "-- Connected --"
                id = String.valueOf(System.currentTimeMillis());
                displayText = msg;
            }

            MessagePanel mp = new MessagePanel(id, displayText);
            messageMap.put(id, mp);
            messageList.add(mp);
            messageList.revalidate();
            scrollPane.getVerticalScrollBar().setValue(
                    scrollPane.getVerticalScrollBar().getMaximum());
        });
    }

    public void appendReaction(String msgId, String emoji, String sender) {
        SwingUtilities.invokeLater(() -> {
            MessagePanel mp = messageMap.get(msgId);
            if (mp != null)
                mp.addReaction(emoji, sender);
        });
    }

    public void showTunnelInfo(String address) {
        if (tunnelLabel == null)
            return;
        SwingUtilities.invokeLater(() -> {
            tunnelLabel.setText(address);
            tunnelPanel.setVisible(true);
            if (panel != null)
                panel.revalidate();
        });
    }

    public void setConnected(boolean connected) {
        if (inputField == null)
            return;
        SwingUtilities.invokeLater(() -> {
            inputField.setEnabled(connected);
            sendBtn.setVisible(connected);
            reconnectBtn.setVisible(!connected);
        });
    }
}
