package controller;

import controller.exceptions.InvalidGameException;
import controller.exceptions.NotFoundException;
import controller.exceptions.SolutionInvalidException;
import controller.interfaces.Viewable;
import model.SudokuValidator;
import model.Game;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.solver.Solver;

public class GameDriver implements Viewable {
    private Game currentGame;
    private Game solvedGame;
    private GameStorage storage;
    private GameLoader loader;
    private GameGenerator generator;
    private Solver solver = new Solver();

    public GameDriver(GameStorage storage) {
        this.storage = storage;
        this.loader = new GameLoader();
        this.generator = new GameGenerator();
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public void startNewGame(DifficultyEnum difficulty, String puzzleName) throws IOException {
        try {
            this.solvedGame = getGame(difficulty);
            driveGames(solvedGame);
            verifyBoard(solvedGame.getBoard());

            // Save solution for future resume
            storage.saveGame(solvedGame, "solution", "solution");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void resumeIncomplete() throws IOException {
        this.currentGame = loader.loadIncomplete();
        this.solvedGame = loader.loadGame("solution");
    }

    public boolean verifyBoard(int[][] board) {
        SudokuValidator validator = new SudokuValidator(board);
        if (validator.isValid())
            return true;
        else throw new InvalidGameException("Invalid game");
    }

    public void solveBoard() throws InvalidGameException {
        if (currentGame == null) {
            throw new InvalidGameException("No game loaded");
        }

        if (currentGame.countEmptyCells() != 5) {
            throw new InvalidGameException("Solve only works with exactly 5 empty cells");
        }

        if (!solver.isSolvable(currentGame)) {
            throw new InvalidGameException("Current board has conflicts or is not solvable");
        }

        int[] solution = solver.solve(currentGame);
        applySolution(currentGame, solution);
    }

    private void applySolution(Game game, int[] solution) {
        for (int i = 0; i < solution.length; i += 3) {
            int row = solution[i];
            int col = solution[i + 1];
            int value = solution[i + 2];
            game.setValue(row, col, value);
        }
    }

    public void saveCurrentGame(String mode, String fileName) throws IOException {
        storage.saveGame(currentGame, mode, fileName);
    }

    public boolean hasIncomplete() {
        return storage.hasBoard("incomplete");
    }

    public List<String> listGames(String mode) {
        File[] files = storage.getBoards(mode);
        List<String> names = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                names.add(f.getName().replace(".csv", ""));
            }
        }
        return names;
    }

    public Game getSolvedGame() {
        return solvedGame;
    }

    private int countEmptyCells() {
        int count = 0;
        int[][] boardArr = currentGame.getBoard();
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                if (boardArr[i][j] == 0) count++;
        return count;
    }

    @Override
    public Catalog getCatalog() {
        return new Catalog();
    }

    @Override
    public Game getGame(DifficultyEnum level) throws NotFoundException {
        try {
            return loader.loadGame(level.toString().toLowerCase());
        } catch (IOException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @Override
    public void driveGames(Game sourceGame) throws SolutionInvalidException {
        try {
            this.solvedGame = sourceGame;
            this.currentGame = generator.generate(sourceGame, sourceGame.getDifficulty());

            // Ensure a solution exists on disk so resume works across restarts
            storage.saveGame(this.solvedGame, "solution", "solution");
        } catch (Exception e) {
            throw new SolutionInvalidException(e.getMessage());
        }
    }

    @Override
    public String verifyGame(Game game) {
        try {
            boolean valid = verifyBoard(game.getBoard());
            return valid ? "Board is valid!" : "Board is invalid!";
        } catch (InvalidGameException e) {
            return "Board has errors or incomplete cells.";
        }
    }

    @Override
    public int[] solveGame(Game game) throws InvalidGameException {
        try {
            model.solver.Solver solver = new model.solver.Solver();
            int[] solution = solver.solve(game);
            for (int i = 0; i < solution.length; i += 3) {
                int row = solution[i];
                int col = solution[i + 1];
                int value = solution[i + 2];
                game.setValue(row, col, value);
            }
            return solution;
        } catch (Exception e) {
            throw new InvalidGameException("Failed to solve: " + e.getMessage());
        }
    }
}