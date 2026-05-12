package ui;
import javax.swing.JPanel;

public interface Screen {
    JPanel getPanel();  // returns the screen's UI
    void onShow();      // called when screen becomes active
    void onHide();      // called when screen is hidden
}