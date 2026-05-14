package main;

import javax.swing.*;
import network.NetworkManager;
import ui.*;

/**
 * Entry point of the chat application.
 * Handles screen transitions between Startup and Chat screens.
 */
public class ChatApp {

    // Main window - shared across all screens so we can switch between the
    public static void main(String[] args) {
        // Create the main application window
         JFrame frame1 = new JFrame("Hello Bros");
        frame1.setSize(400, 500);

        // Exit the application when the window is closed
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Center the window on the screen
        frame1.setLocationRelativeTo(null);

        // Make the window visible
        frame1.setVisible(true);

        Navigator navigator = new Navigator(frame1);

        StartupScreen startup = new StartupScreen(navigator);
        ChatScreen chat = new ChatScreen(navigator);
        LobbyScreen lobby = new LobbyScreen(navigator);
        GameScreen game = new GameScreen(navigator);

        NetworkManager.init(chat, game,lobby); 
        
        navigator.register("startup", startup);
        navigator.register("chat", chat);
        navigator.register("lobby", lobby);
        navigator.register("game", game);

        navigator.goTo("startup");
    }

}