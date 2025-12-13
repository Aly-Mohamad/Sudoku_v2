package controller;

import controller.exceptions.InvalidGameException;
import controller.exceptions.NotFoundException;
import controller.exceptions.SolutionInvalidException;
import controller.interfaces.Controllable;
import model.SudokuValidator;
import model.Game;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameDriver implements Controllable {
    private Game currentGame;
    private Game solvedGame;
    private GameStorage storage;
    private GameLoader loader;
    private GameGenerator generator;

    public GameDriver(GameStorage storage) {
        this.storage = storage;
        this.loader = new GameLoader();
        this.generator = new GameGenerator();
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public void startNewGame(char difficulty, String puzzleName) throws IOException {
        String diff;
        switch (Character.toUpperCase(difficulty)) {
            case 'E': diff = "easy"; break;
            case 'M': diff = "medium"; break;
            case 'H': diff = "hard"; break;
            default:
                throw new NotFoundException("Invalid difficulty: " + difficulty);
        }
        this.solvedGame = loader.loadGame(diff, puzzleName); // full solution
        verifyBoard(solvedGame.getBoard());

        // Save solution for future resume
        storage.saveGame(solvedGame, "Solution", "solution");

        driveGames(getGame(difficulty));
    }


    public void resumeIncomplete() throws IOException {
        this.currentGame = loader.loadIncomplete("board");
        this.solvedGame = loader.loadGame("solution", "solution");
    }


    public boolean verifyBoard(int[][] board) {
        SudokuValidator validator = new SudokuValidator(board);
        if (validator.isValid())
            return true;
        else throw new InvalidGameException("Invalid game");
    }

    public void solveBoard() {
        if (countEmptyCells() == 5 && solvedGame != null) {
            this.currentGame = solvedGame.copy();
        }
    }

    public void saveCurrentGame(String mode, String fileName) throws IOException {
        storage.saveGame(currentGame, mode, fileName);
    }

    public boolean hasIncomplete() {
        return storage.hasBoard("Incomplete");
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
    public int[][] getGame(char level) throws NotFoundException {
        DifficultyEnum diff;

        switch (Character.toUpperCase(level)) {
            case 'E': diff = DifficultyEnum.EASY; break;
            case 'M': diff = DifficultyEnum.MEDIUM; break;
            case 'H': diff = DifficultyEnum.HARD; break;
            default:
                throw new NotFoundException("Invalid difficulty: " + level);
        }

        return generator.generate(solvedGame, diff).getBoard();
    }

    @Override
    public void driveGames(int[][] source) {
        this.currentGame = new Game(source);
    }

    @Override
    public boolean[][] verifyGame(int[][] game) {
        boolean[][] valid = new boolean[9][9];

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int value = game[i][j];

                if (value == 0) {
                    valid[i][j] = true;
                } else {
                    game[i][j] = 0;
                    SudokuValidator validator = new SudokuValidator(game);
                    valid[i][j] = validator.isCellValid(i, j);
                    game[i][j] = value;
                }
            }
        }
        return valid;
    }

    public Game getSolvedGame() {
        return solvedGame;
    }


}
