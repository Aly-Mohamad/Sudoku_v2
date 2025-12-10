package model.checker;

import java.util.ArrayList;
import java.util.List;

public class BoxChecker extends Checker {

    private int boxIndex;

    public BoxChecker(int[][] board, List<String> errors, int boxIndex) {
        super(board, errors);
        this.boxIndex = boxIndex;
    }

    @Override
    public void run() {

        int[] freq = new int[10];
        List<Integer>[] positions = new ArrayList[10];

        for (int i = 1; i <= 9; i++) {
            positions[i] = new ArrayList<>();
        }

        int startRow = (boxIndex / 3) * 3;
        int startCol = (boxIndex % 3) * 3;

        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                int val = board[r][c];

                if (val == 0) continue;

                freq[val]++;
                positions[val].add((r - startRow) * 3 + (c - startCol) + 1);
            }
        }

        for (int n = 1; n <= 9; n++) {
            if (freq[n] > 1) {
                errors.add("BOX " + (boxIndex + 1) + ", duplicate number " + n + " at cells " + positions[n]);
            }
        }
    }
}
