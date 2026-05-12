package ui;

import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class Navigator {

    private final JFrame frame;
    private final Map<String, Screen> screens = new HashMap<>();
    private Screen current;

    public Navigator(JFrame frame) {
        this.frame = frame;
    }

    public void register(String name, Screen screen) {
        screens.put(name, screen);
    }

    public void goTo(String name) {
        Screen next = screens.get(name);
        if (next == null) {
            System.out.println("Navigator: screen not found: " + name);
            return;
        }

        if (current != null) current.onHide();

        current = next;
        current.onShow();

        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            frame.add(current.getPanel());
            frame.revalidate();
            frame.repaint();
        });
    }
}