package controller;

import controller.exceptions.SolutionInvalidException;
import model.SudokuBoard;
import model.SudokuValidator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameDriver {

    private SudokuBoard currentBoard;
    private SudokuBoard solvedBoard;
    private String difficulty;
    private GameStorage storage;

    public GameDriver(GameStorage storage) {
        this.storage = storage;
    }

    public SudokuBoard getCurrentBoard() {
        return currentBoard;
    }

    public void startNewGame(String difficulty, String puzzleName) throws IOException {
        this.difficulty = difficulty;
        this.solvedBoard = storage.loadGame(difficulty, puzzleName);
        this.currentBoard = GameGenerator.generateGame(solvedBoard, difficulty);
    }

    public void resumeIncomplete() throws IOException {
        this.currentBoard = storage.loadIncomplete("board");
        this.solvedBoard = currentBoard.copy(); // copy so solving still works
        this.difficulty = "incomplete";
    }

    public boolean verifyBoard() {
        SudokuValidator validator = new SudokuValidator(currentBoard.getBoard());
        return validator.isValid();
    }

    public void solveBoard() {
        if (countEmptyCells() == 5 && solvedBoard != null) {
            this.currentBoard = solvedBoard.copy();
        }
    }

    public void saveCurrentGame(String mode, String fileName) throws IOException {
        storage.saveGame(currentBoard, mode, fileName);
    }


    public boolean hasIncomplete() {
        return storage.hasBoard("incomplete");
    }

    public void saveIncomplete() throws IOException {
        storage.saveIncomplete(currentBoard, "board");
    }


    public List<String> listGames(String difficulty) {
        File[] files = storage.getBoards(difficulty);
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
        int[][] boardArr = currentBoard.getBoard();
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                if (boardArr[i][j] == 0) count++;
        return count;
    }
}
