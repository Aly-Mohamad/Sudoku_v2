package controller;

import controller.DifficultyEnum;
import controller.exceptions.InvalidGameException;
import controller.exceptions.NotFoundException;
import controller.exceptions.SolutionInvalidException;
import controller.interfaces.Controllable;
import controller.interfaces.Viewable;
import model.Game;
import model.SudokuValidator;

public class GameControllerAdapter implements Controllable {

    private final Viewable driver;
    private Game currentGame;
    private GameLoader loader;

    public GameControllerAdapter(Viewable driver) {
        this.driver = driver;
        this.loader = new GameLoader();
    }

    // ---------------- Catalog ----------------
    @Override
    public boolean[] getCatalog() {
        boolean[] result = new boolean[2];
        result[0] = driver.getCatalog().isCurrent();
        result[1] = driver.getCatalog().isAllModesExist();
        return result;
    }

    // ---------------- Load Game ----------------
    @Override
    public int[][] getGame(char level) throws NotFoundException {
        DifficultyEnum diff = switch (Character.toUpperCase(level)) {
            case 'E' -> DifficultyEnum.EASY;
            case 'M' -> DifficultyEnum.MEDIUM;
            case 'H' -> DifficultyEnum.HARD;
            default -> throw new NotFoundException("Invalid difficulty: " + level);
        };

        currentGame = driver.getGame(diff);
        return currentGame.getBoard();
    }

    // ---------------- Drive Games ----------------
    @Override
    public void driveGames(String sourcePath) throws SolutionInvalidException {
        try {
            // Load solved game from path
            Game source = loader.loadGame(sourcePath);
            driver.driveGames(source);
            currentGame = source;
        } catch (Exception e) {
            throw new SolutionInvalidException(e.getMessage());
        }
    }

    // ---------------- Verify (cell by cell) ----------------
    @Override
    public boolean[][] verifyGame(int[][] board) {
        boolean[][] valid = new boolean[9][9];

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int val = board[i][j];

                if (val == 0) {
                    valid[i][j] = true;
                } else {
                    board[i][j] = 0;
                    SudokuValidator validator = new SudokuValidator(board);
                    valid[i][j] = validator.isCellValid(i, j);
                    board[i][j] = val;
                }
            }
        }
        return valid;
    }

    // ---------------- Solve ----------------
    @Override
    public int[][] solveGame(int[][] board) throws InvalidGameException {
        currentGame = new Game(board);
        driver.solveGame(currentGame);
        return currentGame.getBoard();
    }
}
