import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Chat screen UI - displays messages and handles user input.
 * Implements MessageListener to receive messages from the network.
 */
public class ChatScreen implements MessageListener {

    // Reference to the main application window
    private final JFrame frame;

    // The user's name (shown with their messages)
    private final String name;

    // Text area where all chat messages are displayed
    private JTextArea chatArea;

    // Hook for sending messages - overridden by ChatApp to connect to network
    // This is a template method pattern: provides default behavior but allows customization
    protected void onSend(String msg) {}

    public ChatScreen(JFrame frame, String name) {
        this.frame = frame;
        this.name  = name;
    }

    // Sets up the chat UI and displays it
    public void show() {
        // Create the chat display area
        chatArea = new JTextArea();
        chatArea.setEditable(false);     // Users can only read, not edit messages
        chatArea.setLineWrap(true);      // Wrap long lines to fit the width
        chatArea.setWrapStyleWord(true); // Wrap at word boundaries, not character boundaries
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));

        // Add scrollbar to the chat area
        JScrollPane scrollPane = new JScrollPane(chatArea);

        // Create the input field where user types messages
        JTextField inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));

        // Create the send button
        JButton sendBtn = new JButton("Send");

        // Create bottom panel with input field and send button
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        // Replace previous screen content with chat UI
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Refresh the window
        frame.revalidate();
        frame.repaint();

        // Send Message Logic - use Runnable so we can call from both button and Enter key
        Runnable send = () -> {
            String msg = inputField.getText().trim();
            if (msg.isEmpty()) return;

            // Show the message in our own chat area
            appendMessage("[" + name + "]: " + msg);

            // Send to network (this calls the overridden method in ChatApp)
            onSend(msg);

            // Clear the input field after sending
            inputField.setText("");
        };

        // Send button click handler
        sendBtn.addActionListener(e -> send.run());

        // Enter key handler in the input field
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    send.run();
                }
            }
        });

        // Show connection status message
        appendMessage("-- Connected as " + name + " --");
    }

    // Add a message to the chat display
    // Called from network thread when messages are received
    public void appendMessage(String msg) {
        // Swing components must be updated from the Event Dispatch Thread (EDT)
        // SwingUtilities.invokeLater() ensures this thread-safe update
        SwingUtilities.invokeLater(() -> {
            chatArea.append(msg + "\n");
            // Move cursor to the end so new messages are visible (auto-scroll)
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    // Implementation of MessageListener interface
    @Override
    public void onMessage(String msg) {
        appendMessage(msg);
    }

    // Implementation of MessageListener interface
    @Override
    public void onDisconnected() {
        appendMessage("-- Disconnected --");
    }
}