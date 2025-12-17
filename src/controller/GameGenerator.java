package controller;

import model.RandomPairs;
import model.Game;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameGenerator {
    private final RandomPairs randomPairs = new RandomPairs();

    public Game generate(Game solved, DifficultyEnum difficulty) {

        int eraseCount;

        switch (difficulty) {
            case EASY: eraseCount = 10; break;
            case MEDIUM: eraseCount = 20; break;
            case HARD: eraseCount = 25; break;
            default:
                throw new IllegalArgumentException("Invalid difficulty for generation");
        }

        int[][] board = solved.getBoard();
        Game newGame = new Game(board, difficulty);

        Set<Integer> removed = new HashSet<>();
        List<int[]> pairs = randomPairs.generateDistinctPairs(eraseCount * 3);

        for (int[] pair : pairs) {
            if (removed.size() == eraseCount) break;

            int index = pair[0] % 81;

            if (removed.add(index)) {
                int row = index / 9;
                int col = index % 9;
                newGame.setValue(row, col, 0);
            }
        }

        while (removed.size() < eraseCount) {
            int x = new java.util.Random().nextInt(81);
            if (removed.add(x)) {
                int row = x / 9;
                int col = x % 9;
                newGame.setValue(row, col, 0);
            }
        }

        return newGame;
    }
}
