package ui;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Startup screen with input fields for Host or Join functionality.
 */
public class StartupScreen {

    // Reference to the main window - we add/remove panels from this
    private final JFrame frame;

    public StartupScreen(JFrame frame) {
        this.frame = frame;
    }

    // Displays the startup screen with input fields and buttons
    public void show() {
        // Create a vertical layout panel (stacks items top to bottom)
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Add padding around the edges (top, left, bottom, right)
        panel.setBorder(BorderFactory.createEmptyBorder(80, 60, 80, 60));

        // Title at the top
        JLabel title = new JLabel("Hello Bros");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Name Input
        JLabel nameLabel = new JLabel("Your Name:");
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // Port Input (for hosting)
        JLabel portLabel = new JLabel("Port:");
        portLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField portField = new JTextField("4999"); // Default port
        portField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // Host Address Input (for joining)
        JLabel hostLabel = new JLabel("Host Address (join only):");
        hostLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField addressField = new JTextField("bore.pub"); // Default tunnel address
        addressField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // Buttons
        JButton hostBtn = new JButton("Host");
        hostBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        hostBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton joinBtn = new JButton("Join");
        joinBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Add all components to the panel with spacing between them
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

        // When "Host" is clicked: start a new server
        hostBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String port = portField.getText().trim();

            // Validate that name is entered
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Enter your name.");
                return;
            }

            // Call the callback - ChatApp will handle creating the server
            onHost(name, Integer.parseInt(port));
        });

        // When "Join" is clicked: connect to existing server
        joinBtn.addActionListener(e -> {
            String name    = nameField.getText().trim();
            String port    = portField.getText().trim();
            String address = addressField.getText().trim();

            // Validate that name is entered
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Enter your name.");
                return;
            }

            // Call the callback - ChatApp will handle creating the client
            onJoin(name, address, Integer.parseInt(port));
        });

        // Add panel to frame
        frame.getContentPane().removeAll();
        frame.add(panel);
        frame.revalidate();
        frame.repaint();
    }

    // Callback method - overridden by ChatApp to handle "Host" action
    protected void onHost(String name, int port) {}

    // Callback method - overridden by ChatApp to handle "Join" action
    protected void onJoin(String name, String address, int port) {}
}