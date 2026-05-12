package ui;

import javax.swing.JPanel;

public interface Screen {
    JPanel getPanel();
    void onShow(Object args);
    void onHide();
}