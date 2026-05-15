package ui;

import java.awt.Image;
import javax.swing.ImageIcon;

public class Assets {
    public static final ImageIcon DICE = load("assets/dice.png");
    public static final ImageIcon SPEECH_BUBBLE = scale(load("assets/speech_bubble.png"), 24, 24);
    public static final ImageIcon HOURGLASS = load("assets/hourglass.png");
    public static final ImageIcon PAPERCLIP = load("assets/paperclip.png");

    private static ImageIcon load(String path) {
        var url = Assets.class.getClassLoader().getResource(path);
        System.out.println("Loading: " + path + " -> " + url);
        return new ImageIcon(url);
    }

    private static ImageIcon scale(ImageIcon icon, int w, int h) {
        return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }
}