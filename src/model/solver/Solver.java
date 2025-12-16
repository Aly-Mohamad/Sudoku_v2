package model.solver;

import model.Game;
import controller.exceptions.InvalidGameException;
import java.util.ArrayList;
import java.util.List;

public class Solver {
    private final VerifierFlyweight verifier = new VerifierFlyweight();

    public int[] solve(Game game) throws InvalidGameException {
        int[][] board = game.getBoard();

        // Check if exactly 5 empty cells
        List<int[]> emptyCells = findEmptyCells(board);
        if (emptyCells.size() != 5) {
            throw new InvalidGameException("Solver only works when exactly 5 cells are empty");
        }

        // Create a mutable copy of the board
        int[][] workingBoard = copyBoard(board);

        // Find solution
        int[] solution = findSolution(workingBoard, emptyCells);

        if (solution == null) {
            throw new InvalidGameException("No valid solution found");
        }

        // Return solution array: [row1, col1, val1, row2, col2, val2, ...]
        return createSolutionArray(emptyCells, solution);
    }

    private List<int[]> findEmptyCells(int[][] board) {
        List<int[]> emptyCells = new ArrayList<>();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    emptyCells.add(new int[]{row, col});
                }
            }
        }
        return emptyCells;
    }

    private int[][] copyBoard(int[][] board) {
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, 9);
        }
        return copy;
    }

    private int[] findSolution(int[][] board, List<int[]> emptyCells) {
        PermutationIterator iterator = new PermutationIterator(5, 9);

        while (iterator.hasNext()) {
            int[] values = iterator.next();

            // Apply values to empty cells
            for (int i = 0; i < 5; i++) {
                int[] cell = emptyCells.get(i);
                board[cell[0]][cell[1]] = values[i];
            }

            // Check if valid
            if (verifier.isValid(board)) {
                // Check if complete (no zeros)
                boolean complete = true;
                for (int row = 0; row < 9 && complete; row++) {
                    for (int col = 0; col < 9; col++) {
                        if (board[row][col] == 0) {
                            complete = false;
                            break;
                        }
                    }
                }

                if (complete) {
                    return values;
                }
            }

            // Reset board for next iteration
            for (int i = 0; i < 5; i++) {
                int[] cell = emptyCells.get(i);
                board[cell[0]][cell[1]] = 0;
            }
        }

        return null;
    }

    private int[] createSolutionArray(List<int[]> emptyCells, int[] values) {
        int[] result = new int[emptyCells.size() * 3];
        int index = 0;

        for (int i = 0; i < emptyCells.size(); i++) {
            int[] cell = emptyCells.get(i);
            result[index++] = cell[0];  // row
            result[index++] = cell[1];  // col
            result[index++] = values[i]; // value
        }

        return result;
    }

    public boolean isSolvable(Game game) {
        try {
            int[][] board = game.getBoard();
            List<int[]> emptyCells = findEmptyCells(board);

            if (emptyCells.size() != 5) {
                return false;
            }

            // Check if current board is valid (excluding empty cells)
            int[][] tempBoard = copyBoard(board);
            for (int[] cell : emptyCells) {
                tempBoard[cell[0]][cell[1]] = 0;
            }

            return verifier.isValid(tempBoard);
        } catch (Exception e) {
            return false;
        }
    }
}