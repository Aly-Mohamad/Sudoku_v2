package controller;

import controller.exceptions.InvalidGameException;
import controller.exceptions.NotFoundException;
import controller.exceptions.SolutionInvalidException;
import controller.interfaces.Controllable;
import controller.interfaces.Viewable;
import model.Game;

public class GameControllerAdapter implements Controllable {

    private final Viewable driver;
    private Game currentGame;
    private GameLoader loader;

    public GameControllerAdapter(Viewable driver) {
        this.driver = driver;
        this.loader = new GameLoader();
    }

    @Override
    public boolean[] getCatalog() {
        boolean[] result = new boolean[2];
        try {
            Catalog catalog = driver.getCatalog();
            result[0] = catalog.isCurrent();
            result[1] = catalog.isAllModesExist();
        } catch (Exception e) {
            result[0] = false;
            result[1] = false;
        }
        return result;
    }

    @Override
    public int[][] getGame(char level) throws NotFoundException {
        var diff = switch (Character.toUpperCase(level)) {
            case 'E' -> DifficultyEnum.EASY;
            case 'M' -> DifficultyEnum.MEDIUM;
            case 'H' -> DifficultyEnum.HARD;
            default -> throw new NotFoundException("Invalid difficulty: " + level);
        };

        currentGame = driver.getGame(diff);
        return currentGame.getBoard();
    }

    @Override
    public void driveGames(String sourcePath) throws SolutionInvalidException {
        try {
            Game source = loader.loadGame(sourcePath);
            driver.driveGames(source);
            currentGame = driver.getCurrentGame();
        } catch (Exception e) {
            throw new SolutionInvalidException(e.getMessage());
        }
    }

    @Override
    public boolean[][] verifyGame(int[][] board) {
        boolean[][] valid = new boolean[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int val = board[i][j];
                if (val == 0) {
                    valid[i][j] = true; // empty cells are considered fine
                } else {
                    valid[i][j] = isCellValid(board, i, j, val);
                }
            }
        }
        return valid;
    }

    private boolean isCellValid(int[][] board, int row, int col, int val) {
        if (val < 1 || val > 9) return false;

        // Row
        for (int c = 0; c < 9; c++) {
            if (c != col && board[row][c] == val) return false;
        }
        // Column
        for (int r = 0; r < 9; r++) {
            if (r != row && board[r][col] == val) return false;
        }
        // Box
        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;
        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                if ((r != row || c != col) && board[r][c] == val) return false;
            }
        }
        return true;
    }

    @Override
    public int[][] solveGame(int[][] board) throws InvalidGameException {
        currentGame = new Game(board);
        driver.solveGame(currentGame);
        return currentGame.getBoard();
    }
}