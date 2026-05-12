package ui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import network.*;
import tunnel.*;

public class ChatScreen implements Screen {

    private final Navigator navigator;
    private JPanel panel;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendBtn;
    private JButton reconnectBtn;
    private JPanel tunnelPanel;
    private JLabel tunnelLabel;

    private Client client;
    private Server server;
    private TunnelProvider tunnel;

    public ChatScreen(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void onShow(Object args) {

        switch (args) {
            case ChatArgs a -> {
                buildPanel(a.name);
                if (a.isHost)
                    startServer(a.name, a.port);
                else
                    startClient(a.name, a.address, a.port);
            }
            case LobbyArgs a -> {
                buildPanel(a.name);
                if (a.isHost)
                    startServer(a.name, a.port);
                else
                    startClient(a.name, a.address, a.port);
            }
            default -> {
            }
        }
    }

    @Override
    public void onHide() {
        // cleanup on leaving screen
        if (client != null) {
            client = null;
        }
        if (tunnel != null) {
            tunnel.stop();
            tunnel = null;
        }
    }

    private void buildPanel(String name) {
        panel = new JPanel(new BorderLayout());

        // ── Tunnel info bar ────────────────────
        tunnelLabel = new JLabel("Starting tunnel...");
        tunnelLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        JButton copyBtn = new JButton("Copy");
        copyBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        copyBtn.addActionListener(e -> {
            String port = tunnelLabel.getText().contains(":")
                    ? tunnelLabel.getText().split(":")[1].trim()
                    : tunnelLabel.getText();
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(port), null);
        });

        tunnelPanel = new JPanel(new BorderLayout());
        tunnelPanel.setBackground(new Color(230, 245, 255));
        tunnelPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        tunnelPanel.add(new JLabel("Share: "), BorderLayout.WEST);
        tunnelPanel.add(tunnelLabel, BorderLayout.CENTER);
        tunnelPanel.add(copyBtn, BorderLayout.EAST);
        tunnelPanel.setVisible(false);

        // ── Chat area ──────────────────────────
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(chatArea);

        // ── Input area ─────────────────────────
        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        sendBtn = new JButton("Send");
        reconnectBtn = new JButton("Reconnect");
        JButton fileBtn = new JButton(new ImageIcon("assets/paperclip.png"));
        reconnectBtn.setVisible(false);

        JPanel inputPanel = new JPanel(new BorderLayout());
        JPanel rightBtns = new JPanel(new BorderLayout());
        rightBtns.add(fileBtn, BorderLayout.WEST);
        rightBtns.add(sendBtn, BorderLayout.EAST);

        inputPanel.add(reconnectBtn, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(rightBtns, BorderLayout.EAST);

        // ── Header bar ─────────────────────────
        JButton backBtn = new JButton("← Back");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        backBtn.addActionListener(e -> {
            onHide();
            navigator.goTo("lobby");
        });

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(backBtn, BorderLayout.WEST);
        headerPanel.add(tunnelPanel, BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        // ── Actions ────────────────────────────
        Runnable send = () -> {
            String msg = inputField.getText().trim();
            if (msg.isEmpty())
                return;
            appendMessage("[" + name + "]: " + msg);
            if (client != null)
                client.send(msg);
            if (server != null) {
                Server.broadcast("MSG", "[" + name + "]: " + msg, null);
                Server.saveToFile("[" + name + "]: " + msg);
            }
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
            int result = chooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                appendMessage("-- Sending: " + file.getName() + " --");
                if (client != null)
                    client.sendFile(file);
            }
        });

        reconnectBtn.addActionListener(e -> {
            reconnectBtn.setVisible(false);
            sendBtn.setVisible(true);
            inputField.setEnabled(true);
            appendMessage("-- Reconnecting... --");
            if (client != null)
                client.start();
        });

        appendMessage("-- Connected as " + name + " --");
    }

    private void startServer(String name, int port) {
        server = new Server(port, new MessageListener() {
            @Override
            public void onMessage(String msg) {
                appendMessage(msg);
            }

            @Override
            public void onDisconnected() {
                appendMessage("-- Client disconnected --");
            }

            @Override
            public void onGameMove(String moveData) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        server.start();

        tunnel = TunnelFactory.create(TunnelFactory.Provider.BORE);
        tunnel.start(port, new TunnelProvider.TunnelListener() {
            @Override
            public void onReady(String address) {
                showTunnelInfo(address);
                appendMessage("-- Tunnel ready: " + address + " --");
            }

            @Override
            public void onError(String error) {
                appendMessage("-- Tunnel error: " + error + " --");
            }
        });
    }

    private void startClient(String name, String address, int port) {
        client = new Client(address, port, name, new MessageListener() {
            @Override
            public void onMessage(String msg) {
                appendMessage(msg);
            }

            @Override
            public void onDisconnected() {
                appendMessage("-- Disconnected --");
                setConnected(false);
            }

            @Override
            public void onGameMove(String moveData) {
            } // not used in chat

        });
        client.start();
    }

    public void appendMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(msg + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public void showTunnelInfo(String address) {
        SwingUtilities.invokeLater(() -> {
            tunnelLabel.setText(address);
            tunnelPanel.setVisible(true);
            panel.revalidate();
            panel.repaint();
        });
    }

    public void setConnected(boolean connected) {
        SwingUtilities.invokeLater(() -> {
            inputField.setEnabled(connected);
            sendBtn.setVisible(connected);
            reconnectBtn.setVisible(!connected);
        });
    }

    public void showFileNotification(String fileName, String filePath) {
    SwingUtilities.invokeLater(() -> {
        JPanel notif = new JPanel(new BorderLayout());
        notif.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        JLabel label = new JLabel("📎 " + fileName);
        JButton openBtn = new JButton("Open");

        openBtn.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(new File(filePath));
            } catch (IOException ex) {
                appendMessage("-- Could not open file --");
            }
        });

        notif.add(label,   BorderLayout.CENTER);
        notif.add(openBtn, BorderLayout.EAST);

        chatArea.add(notif); // won't work on JTextArea
    });
}
}