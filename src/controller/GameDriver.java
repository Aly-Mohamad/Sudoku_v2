package controller;

import controller.exceptions.SolutionInvalidException;
import model.SudokuBoard;
import model.SudokuValidator;
import java.util.List;


//To be edited not fully implemented



public class GameDriver {

    private SudokuBoard board;
    private String difficulty;

    public GameDriver(SudokuBoard board) throws SolutionInvalidException {
        this.board = board;
        SudokuValidator validator = new SudokuValidator(board.getBoard());
        String result = validator.validate(); // modify validator to return status

        if(result.equals(SudokuValidator.INVALID) || result.equals(SudokuValidator.INCOMPLETE)) {
            throw new SolutionInvalidException("Source board is " + result);
        }
    }

    public SudokuBoard getBoard() {
        return board;
    }

    public String getDifficulty() {
            return difficulty;
    }

    public void startNewGame(String difficulty) {
        this.difficulty = difficulty;
        SudokuBoard SolvedBoard = board.copy();
        this.board = GameGenerator.generateGame(SolvedBoard, difficulty);
    }

    public void loadGame(String filename) {
        // stub
    }

    public void saveGame(String filename) {
        // stub
    }

    public void undoMove() {
        // stub
    }

    public List<String> verifyBoard() {
        // stub
        return null;
    }
}
