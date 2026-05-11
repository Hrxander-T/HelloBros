import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ChatScreen implements MessageListener {

    private final JFrame frame;
    private final String name;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendBtn;
    private JButton reconnectBtn;

    protected void onSend(String msg) {}
    protected void onReconnect() {}  // called when reconnect clicked

    public ChatScreen(JFrame frame, String name) {
        this.frame = frame;
        this.name  = name;
    }

    public void show() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField   = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));

        sendBtn      = new JButton("Send");
        reconnectBtn = new JButton("Reconnect");
        reconnectBtn.setVisible(false); // hidden until disconnected

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField,   BorderLayout.CENTER);
        inputPanel.add(sendBtn,      BorderLayout.EAST);
        inputPanel.add(reconnectBtn, BorderLayout.WEST);

        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.revalidate();
        frame.repaint();

        Runnable send = () -> {
            String msg = inputField.getText().trim();
            if (msg.isEmpty()) return;
            appendMessage("[" + name + "]: " + msg);
            onSend(msg);
            inputField.setText("");
        };

        sendBtn.addActionListener(e -> send.run());
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) send.run();
            }
        });

        reconnectBtn.addActionListener(e -> {
            reconnectBtn.setVisible(false);
            sendBtn.setVisible(true);
            inputField.setEnabled(true);
            appendMessage("-- Reconnecting... --");
            onReconnect();
        });

        appendMessage("-- Connected as " + name + " --");
    }

    public void appendMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(msg + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public void setConnected(boolean connected) {
        SwingUtilities.invokeLater(() -> {
            inputField.setEnabled(connected);
            sendBtn.setVisible(connected);
            reconnectBtn.setVisible(!connected);
        });
    }

    @Override
    public void onMessage(String msg) { appendMessage(msg); }

    @Override
    public void onDisconnected() {
        appendMessage("-- Disconnected --");
        setConnected(false);
    }
}