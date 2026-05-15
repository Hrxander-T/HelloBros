package ui;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public final class FilePanel extends JPanel {

    private final JProgressBar progressBar;
    private final JButton saveBtn;

    public FilePanel(String sender, String fileName, byte[] data) {
        setLayout(new BorderLayout(8, 0));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(6, 10, 6, 10)));

        JLabel info = new JLabel("[" + sender + "]: " + fileName);
        info.setFont(new Font("Arial", Font.PLAIN, 14));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        progressBar.setString("Sending...");

        saveBtn = new JButton("Save");
        saveBtn.setVisible(false);
        if (data != null) {
            saveBtn.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(fileName));
                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try (FileOutputStream fos = new FileOutputStream(chooser.getSelectedFile())) {
                        fos.write(data);
                        JOptionPane.showMessageDialog(this, "File saved!");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
                    }
                }
            });
        }

        JPanel bottom = new JPanel(new BorderLayout(4, 0));
        bottom.add(progressBar, BorderLayout.CENTER);
        bottom.add(saveBtn, BorderLayout.EAST);

        add(info, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        if (data == null) {
            progressBar.setString("Receiving...");
            progressBar.setIndeterminate(true); // animated bar, no percent
        } else {
            setDone(data);
        }
    }

    public void setDone(byte[] data) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setValue(100);
            progressBar.setString("Done");
            if (data != null) {
                saveBtn.setVisible(true);
               
            }
        });
    }
    
    

    public void setProgress(int percent) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(percent);
            progressBar.setString(percent + "%");
        });
    }

}