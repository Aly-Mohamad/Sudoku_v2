import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Checker implements Runnable {
    protected int[][] board;
    protected List<String> errors = new ArrayList<>();

    public Checker(int[][] board, List<String> errors) {
        this.board = board;
        this.errors = errors;
    }

    protected void addError(String msg) {
        synchronized (errors) {
            errors.add(msg);
        }
    }

    public abstract void run();
}
