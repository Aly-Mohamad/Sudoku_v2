package model;

import model.checker.CheckerFactory;
import java.util.ArrayList;
import java.util.List;

public class SudokuValidator {
    public static final String VALID = "VALID";
    public static final String INVALID = "INVALID";
    public static final String INCOMPLETE = "INCOMPLETE";

    private final int[][] board;
    private final List<String> errors = new ArrayList<>();

    public SudokuValidator(int[][] board) {
        this.board = board;
    }

    public String validate() {
        if (!isComplete()) {
            return INCOMPLETE;
        }

        for (int i = 0; i < 9; i++) {
            CheckerFactory.createChecker("ROW", board, errors, i).run();
            CheckerFactory.createChecker("COL", board, errors, i).run();
            CheckerFactory.createChecker("BOX", board, errors, i).run();
        }

        return errors.isEmpty() ? VALID : INVALID;
    }

    private boolean isComplete() {
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == 0) return false;
            }
        }
        return true;
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean isValid() {
        return validate().equals(VALID);
    }
}