package controller;

import model.SudokuBoard;
import model.SudokuValidator;

import java.io.IOException;

public class GameLoader {
    private GameStorage storage;

    public GameLoader(GameStorage storage){
        this.storage = storage;
    }

    public SudokuBoard loadGame(String difficulty,String filename) throws IOException {
        return storage.loadGame(difficulty,filename);
    }

    public SudokuBoard loadIncomplete(String filename) throws IOException {
        return storage.loadIncomplete(filename);
    }

    public boolean isValid(SudokuBoard board) {
        SudokuValidator validator = new SudokuValidator(board.getBoard());
        return validator.isValid();
    }

}
