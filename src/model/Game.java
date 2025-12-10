package model;

import controller.DifficultyEnum;

public class Game {
    private int[][] board;
    private DifficultyEnum difficulty;

    public Game(int[][] board) {
        this(board, null);
    }

    public Game(int[][] board, DifficultyEnum difficulty) {
        if (board == null || board.length != 9) {
            throw new IllegalArgumentException("Board must be non-null and 9x9");
        }
        this.board = new int[9][9];
        for (int r = 0; r < 9; r++) {
            if (board[r] == null || board[r].length != 9) {
                throw new IllegalArgumentException("Board must be 9x9");
            }
            System.arraycopy(board[r], 0, this.board[r], 0, 9);
        }
        this.difficulty = difficulty;
    }

    public int[][] getBoard() {
        int[][] copy = new int[9][9];
        for (int r = 0; r < 9; r++) {
            System.arraycopy(board[r], 0, copy[r], 0, 9);
        }
        return copy;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public int getValue(int row, int col) {
        return board[row][col];
    }

    public void setValue(int row, int col, int val) {
        board[row][col] = val;
    }

    public DifficultyEnum getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyEnum difficulty) {
        this.difficulty = difficulty;
    }

    public int countEmptyCells() {
        int count = 0;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board[r][c] == 0) count++;
            }
        }
        return count;
    }

    public Game copy() {
        return new Game(getBoard());
    }
}
