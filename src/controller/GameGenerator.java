package controller;

import model.RandomPairs;
import model.SudokuBoard;

import java.util.List;

public class GameGenerator {
    //Easier to call or edit in the future
    private static final int EASY = 15;
    private static final int MEDIUM = 20;
    private static final int HARD = 25;

    public static SudokuBoard generateGame(SudokuBoard solvedBoard,String difficulty) {
        SudokuBoard puzzle = solvedBoard.copy();


        int cellsToRemove = getCellsToRemove(difficulty.toUpperCase());
        List<int[]> RemovePairs = new RandomPairs().getNPairs(cellsToRemove);
        for (int[] pair : RemovePairs) {
            puzzle.setCell(pair[0], pair[1], 0);
        }
        return puzzle;
    }

    private static int getCellsToRemove(String difficulty){
        switch (difficulty.toUpperCase()){
            case "EASY": return EASY;
            case "MEDIUM": return MEDIUM;
            case "HARD": return HARD;
        }
        return 0;
    }
}
