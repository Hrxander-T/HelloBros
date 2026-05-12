package ui;
import java.awt.*;
import javax.swing.*;

public class LobbyScreen implements Screen {

    private final Navigator navigator;
    private JPanel panel;

    public LobbyScreen(Navigator navigator) {
        this.navigator = navigator;
        buildPanel();
    }

    private void buildPanel() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(80, 60, 80, 60));

        JLabel title = new JLabel("What do you want to do?");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton chatBtn = new JButton("💬 Chat");
        chatBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        chatBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton gameBtn = new JButton("🎮 Tic Tac Toe");
        gameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        gameBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton backBtn = new JButton("← Back");
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panel.add(title);
        panel.add(Box.createVerticalStrut(40));
        panel.add(chatBtn);
        panel.add(Box.createVerticalStrut(15));
        panel.add(gameBtn);
        panel.add(Box.createVerticalStrut(30));
        panel.add(backBtn);

        chatBtn.addActionListener(e -> navigator.goTo("chat", getArgs()));
        gameBtn.addActionListener(e -> navigator.goTo("game", getArgs()));
        backBtn.addActionListener(e -> navigator.goTo("startup"));
    }

    private LobbyArgs getArgs() {
        return (LobbyArgs) currentArgs;
    }

    private Object currentArgs;

    @Override
    public JPanel getPanel() { return panel; }

    @Override
    public void onShow(Object args) { currentArgs = args; }

    @Override
    public void onHide() {}
}