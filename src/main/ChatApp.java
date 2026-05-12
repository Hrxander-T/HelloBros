package main;

import javax.swing.JFrame;
import ui.ChatScreen;
import ui.Navigator;
import ui.StartupScreen;

/**
 * Entry point of the chat application.
 * Handles screen transitions between Startup and Chat screens.
 */
public class ChatApp {

    // Main window - shared across all screens so we can switch between them

    public static void main(String[] args) {
        // Create the main application window
        frame = new JFrame("Hello Bros");
        frame.setSize(400, 500);

        // Exit the application when the window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Center the window on the screen
        frame.setLocationRelativeTo(null);

        // Make the window visible
        frame.setVisible(true);

        Navigator navigator = new Navigator(frame);

        StartupScreen startup = new StartupScreen(navigator);
        ChatScreen chat = new ChatScreen(navigator);

        navigator.register("startup", startup);
        navigator.register("chat", chat);

        navigator.goTo("startup");
    }

    static JFrame frame;
}