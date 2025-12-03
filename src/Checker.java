import java.util.ArrayList;
import java.util.List;

public abstract class Checker {
    protected int[][] board;
    protected List<String> errors = new ArrayList<>();

    public Checker(int[][] board, List<String> errors) {
        this.board = board;
        this.errors = errors;
    }

    public abstract void run();
}
