package ui;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

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
        goTo(name, null);
    }

    public void goTo(String name, Object args) {
        Screen next = screens.get(name);
        if (next == null) { System.out.println("Screen not found: " + name); return; }

        if (current != null) current.onHide();
        current = next;
        current.onShow(args);

        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            frame.add(current.getPanel());
            frame.revalidate();
            frame.repaint();
        });
    }
}