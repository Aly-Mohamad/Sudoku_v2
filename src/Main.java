import ui.SudokuGUI;
import controller.GameStorage;


public class Main {
    public static void main(String[] args) {
        GameStorage storage = new GameStorage();
        new SudokuGUI(storage);
    }
}
