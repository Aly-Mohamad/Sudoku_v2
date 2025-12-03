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
        List<String>[] positions = new List[10];

        for (int n = 1; n <= 9; n++) {
            positions[n] = new ArrayList<>();
        }

        int startRow = (boxIndex / 3) * 3;
        int startCol = (boxIndex % 3) * 3;

        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                int val = board[r][c];
                freq[val]++;
                positions[val].add("(" + (r + 1) + ", " + (c + 1) + ")");
            }
        }

        for (int n = 1; n <= 9; n++) {
            if (freq[n] > 1) {
                String entry = "BOX " + (boxIndex + 1) + ", #" + n + ", " + positions[n];
                errors.add(entry);
            }
        }
    }
}