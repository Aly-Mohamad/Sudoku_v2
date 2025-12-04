
import ui.SudokuGUI;
import controller.GameStorage;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        GameStorage storage = new GameStorage(); // initialize storage
        SwingUtilities.invokeLater(() -> new SudokuGUI(storage)); // launch GUI
    }
}
