package model.checker;

import java.util.ArrayList;
import java.util.List;

public class ColumnChecker extends Checker {
    private int col;

    public ColumnChecker(int[][] board, List<String> errors, int col) {
        super(board, errors);
        this.col = col;
    }

    @Override
    public void run() {
        int[] freq = new int[10];
        List<Integer>[] positions = new ArrayList[10];

        for (int n = 1; n <= 9; n++) {
            positions[n] = new ArrayList<>();
        }

        for (int row = 0; row < 9; row++) {
            int val = board[row][col];
            if (val == 0) continue;
            freq[val]++;
            positions[val].add(row + 1);
        }

        for (int n = 1; n <= 9; n++) {
            if (freq[n] > 1) {
                errors.add("COL " + (col + 1) + ", duplicate number " + n + " at rows " + positions[n]);
            }
        }
    }
}
