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
        List<Integer>[] positions = new List[10];

        for (int n = 1; n <= 9; n++) {
            positions[n] = new ArrayList<>();
        }

        for (int row = 0; row < 9; row++) {
            int val = board[row][col];
            freq[val]++;
            positions[val].add(row + 1);
        }

        for (int n = 1; n <= 9; n++) {
            if (freq[n] > 1) {
                String entry = "COL " + (col + 1) + ", #" + n + ", " + positions[n];
                errors.add(entry);
            }
        }
    }
}