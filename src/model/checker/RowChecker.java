package model.checker;

import java.util.ArrayList;
import java.util.List;

public class RowChecker extends Checker {
    private int row;
    public RowChecker(int[][] board, List<String> errors, int row) {
        super(board, errors);
        this.row = row;
    }

    @Override
    public void run() {
        int[] freq = new int[10];
        List<Integer>[] positions = new List[10];

        for (int n = 1; n <= 9; n++) {
            positions[n] = new ArrayList<>();
        }

        for (int col = 0; col < 9; col++) {
            int val = board[row][col];
            if(val == 0) continue;
            freq[val]++;
            positions[val].add(col + 1);
        }

        for (int n = 1; n <= 9; n++) {
            if (freq[n] > 1) {
                String entry = "ROW " + (row + 1) + ", #" + n + ", " + positions[n];
                errors.add(entry);
            }
        }
    }
}
