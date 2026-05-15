package ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.Dialog;
import java.util.LinkedHashMap;
import java.util.Map;
import network.NetworkManager;

public class MessagePanel extends JPanel {
    private final String messageId;
    private final JLabel reactionLabel;
    private final Map<String, Integer> reactionCounts = new LinkedHashMap<>();

    MessagePanel(String messageId, String text) {
        this.messageId = messageId;
        setLayout(new BorderLayout(8, 0));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));

        JLabel msgLabel = new JLabel(text);
        msgLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        reactionLabel = new JLabel("");
        reactionLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));

        add(msgLabel,      BorderLayout.CENTER);
        add(reactionLabel, BorderLayout.EAST);

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e))
                    showEmojiPicker();
            }
        });
    }

    private void showEmojiPicker() {
        String[] emojis = {"👍", "❤️", "😂", "😮", "😢"};

        JDialog picker = new JDialog(
            SwingUtilities.getWindowAncestor(this), "React", Dialog.ModalityType.MODELESS
        );
        picker.setLayout(new FlowLayout());
        picker.setSize(260, 80);
        picker.setLocationRelativeTo(this);

        for (String emoji : emojis) {
            JButton btn = new JButton(emoji);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            btn.setBorderPainted(false);
            btn.addActionListener(e -> {
                NetworkManager.sendReaction(messageId, emoji);
                picker.dispose();
            });
            picker.add(btn);
        }

        picker.setVisible(true);
    }

    public void addReaction(String emoji, String sender) {
        reactionCounts.merge(emoji, 1, Integer::sum);
        updateReactionLabel();
    }

    private void updateReactionLabel() {
        StringBuilder sb = new StringBuilder();
        reactionCounts.forEach((emoji, count) ->
            sb.append(emoji).append(" ").append(count).append("  ")
        );
        reactionLabel.setText(sb.toString().trim());
    }

    public String getMessageId() { return messageId; }
}