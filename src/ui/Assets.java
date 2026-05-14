package ui;

import java.awt.Image;
import javax.swing.ImageIcon;

public class Assets {
    public static final ImageIcon DICE = new ImageIcon("assets/dice.png");
    public static final ImageIcon SPEECH_BUBBLE = new ImageIcon(
        new ImageIcon("assets/speech_bubble.png").getImage()
            .getScaledInstance(24, 24, Image.SCALE_SMOOTH)
    );

    public static final ImageIcon HOURGLASS = new ImageIcon("assets/hourglass.png");
    public static final ImageIcon PAPERCLIP = new ImageIcon("assets/paperclip.png");
}