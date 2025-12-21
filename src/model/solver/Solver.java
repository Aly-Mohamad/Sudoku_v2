package model.solver;

import model.Game;
import controller.exceptions.InvalidGameException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Solver {
    private final VerifierFlyweight verifier = new VerifierFlyweight();
    private final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public int[] solve(Game game) throws InvalidGameException {
        int[][] board = game.getBoard();

        // Check if exactly 5 empty cells
        List<int[]> emptyCells = findEmptyCells(board);
        if (emptyCells.size() != 5) {
            throw new InvalidGameException("Solver only works when exactly 5 cells are empty");
        }

        // Create a mutable copy of the board
        int[][] baseBoard = copyBoard(board);

        // Find solution using multithreading
        return findSolutionParallel(baseBoard, emptyCells);
    }

    private int[] findSolutionParallel(final int[][] baseBoard, final List<int[]> emptyCells)
            throws InvalidGameException {
        final AtomicBoolean solutionFound = new AtomicBoolean(false);
        final List<int[]> solutionHolder = new ArrayList<>(1);
        final PermutationIterator iterator = new PermutationIterator(5, 9);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        // Create and submit worker threads
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.execute(new SolverWorker(
                    baseBoard, emptyCells, iterator,
                    verifier, solutionFound, solutionHolder
            ));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidGameException("Solver interrupted");
        }

        if (solutionHolder.isEmpty()) {
            throw new InvalidGameException("No valid solution found");
        }

        return createSolutionArray(emptyCells, solutionHolder.get(0));
    }

    // Worker class that implements Runnable
    private static class SolverWorker implements Runnable {
        private final int[][] baseBoard;
        private final List<int[]> emptyCells;
        private final PermutationIterator iterator;
        private final VerifierFlyweight verifier;
        private final AtomicBoolean solutionFound;
        private final List<int[]> solutionHolder;

        public SolverWorker(
                int[][] baseBoard,
                List<int[]> emptyCells,
                PermutationIterator iterator,
                VerifierFlyweight verifier,
                AtomicBoolean solutionFound,
                List<int[]> solutionHolder) {
            this.baseBoard = baseBoard;
            this.emptyCells = emptyCells;
            this.iterator = iterator;
            this.verifier = verifier;
            this.solutionFound = solutionFound;
            this.solutionHolder = solutionHolder;
        }

        @Override
        public void run() {
            while (!solutionFound.get()) {
                int[] values = null;

                synchronized (iterator) {
                    if (!iterator.hasNext() || solutionFound.get()) {
                        return;
                    }
                    values = iterator.next();
                }

                // Create local copy of board
                int[][] threadBoard = copyBoardInternal(baseBoard);

                // Apply values to empty cells
                for (int j = 0; j < 5; j++) {
                    int[] cell = emptyCells.get(j);
                    threadBoard[cell[0]][cell[1]] = values[j];
                }

                // Check if valid
                if (verifier.isValid(threadBoard)) {
                    // Check if complete (no zeros)
                    if (isComplete(threadBoard)) {
                        synchronized (solutionHolder) {
                            if (!solutionFound.get()) {
                                solutionFound.set(true);
                                solutionHolder.add(values);
                            }
                        }
                        return;
                    }
                }
            }
        }

        private int[][] copyBoardInternal(int[][] board) {
            int[][] copy = new int[9][9];
            for (int i = 0; i < 9; i++) {
                System.arraycopy(board[i], 0, copy[i], 0, 9);
            }
            return copy;
        }

        private boolean isComplete(int[][] board) {
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    if (board[row][col] == 0) {
                        return false;
                    }
                }
            }
            return true;
        }
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