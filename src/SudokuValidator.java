import java.util.ArrayList;
import java.util.List;

public class SudokuValidator {
    private int[][] board;
    private List<String> errors = new ArrayList<>();

    public SudokuValidator(int[][] board) {
        this.board = board;
    }

    public void validate() {
        for (int i = 0; i < 9; i++) {
            CheckerFactory.createChecker("ROW", board, errors, i).run();
            CheckerFactory.createChecker("COL", board, errors, i).run();
            CheckerFactory.createChecker("BOX", board, errors, i).run();
        }
        printResult();
    }

    private void printResult() {
        if (errors.isEmpty()) {
            System.out.println("VALID");
            return;
        }

        System.out.println("INVALID");

        for (String e : errors)
            if (e.startsWith("ROW"))
                System.out.println(e);

        for (String e : errors)
            if (e.startsWith("COL"))
                System.out.println(e);

        for (String e : errors)
            if (e.startsWith("BOX"))
                System.out.println(e);
    }
}
