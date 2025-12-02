import java.util.ArrayList;
import java.util.List;

public class SudokuValidator {
    private int[][] board;
    private List<String> errors = new ArrayList<>();

    public SudokuValidator(int[][] board) {
        this.board = board;
    }

    public void validate(int mode) {
        List<Thread> threads = new ArrayList<>();

        if (mode == 0) {
            for (int i = 0; i < 9; i++) {
                CheckerFactory.createChecker("ROW", board, errors, i).run();
                CheckerFactory.createChecker("COL", board, errors, i).run();
                CheckerFactory.createChecker("BOX", board, errors, i).run();
            }
        }

        else if (mode == 3) {
            threads.add(new Thread(() -> {
                for (int i = 0; i < 9; i++)
                    CheckerFactory.createChecker("ROW", board, errors, i).run();
            }));

            threads.add(new Thread(() -> {
                for (int i = 0; i < 9; i++)
                    CheckerFactory.createChecker("COL", board, errors, i).run();
            }));

            threads.add(new Thread(() -> {
                for (int i = 0; i < 9; i++)
                    CheckerFactory.createChecker("BOX", board, errors, i).run();
            }));
        }

        else if (mode == 27) {
            for (int i = 0; i < 9; i++) {
                threads.add(new Thread(CheckerFactory.createChecker("ROW", board, errors, i)));
                threads.add(new Thread(CheckerFactory.createChecker("COL", board, errors, i)));
                threads.add(new Thread(CheckerFactory.createChecker("BOX", board, errors, i)));
            }
        }

        for (Thread t : threads) t.start();

        for (Thread t : threads)
            try {t.join();} catch (Exception ignored) {}

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
