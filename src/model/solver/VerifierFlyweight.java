package model.solver;

public class VerifierFlyweight {
    private static final int SIZE = 9;
    private static final int SUBGRID_SIZE = 3;

    public boolean isValid(int[][] board) {
        // Check rows
        for (int row = 0; row < SIZE; row++) {
            boolean[] seen = new boolean[SIZE + 1];
            for (int col = 0; col < SIZE; col++) {
                int val = board[row][col];
                if (val != 0 && seen[val]) {
                    return false;
                }
                seen[val] = true;
            }
        }

        // Check columns
        for (int col = 0; col < SIZE; col++) {
            boolean[] seen = new boolean[SIZE + 1];
            for (int row = 0; row < SIZE; row++) {
                int val = board[row][col];
                if (val != 0 && seen[val]) {
                    return false;
                }
                seen[val] = true;
            }
        }

        // Check 3x3 subgrids
        for (int boxRow = 0; boxRow < SUBGRID_SIZE; boxRow++) {
            for (int boxCol = 0; boxCol < SUBGRID_SIZE; boxCol++) {
                boolean[] seen = new boolean[SIZE + 1];
                for (int row = boxRow * SUBGRID_SIZE; row < (boxRow + 1) * SUBGRID_SIZE; row++) {
                    for (int col = boxCol * SUBGRID_SIZE; col < (boxCol + 1) * SUBGRID_SIZE; col++) {
                        int val = board[row][col];
                        if (val != 0 && seen[val]) {
                            return false;
                        }
                        seen[val] = true;
                    }
                }
            }
        }

        return true;
    }
}