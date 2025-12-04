package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RandomPairs generates distinct random (row, col) pairs for Sudoku boards.
 * Each pair is guaranteed to be unique.
 */
public class RandomPairs {

    private final List<int[]> pairs;
    private int index = 0;

    /**
     * Constructor generates all possible pairs (0..8, 0..8) and shuffles them.
     */
    public RandomPairs() {
        pairs = new ArrayList<>();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                pairs.add(new int[]{row, col});
            }
        }
        Collections.shuffle(pairs);
    }

    /**
     * Get the next unique pair.
     * @return int[2] containing {row, col}, or null if all pairs used
     */
    public int[] next() {
        if (index >= pairs.size()) return null;
        return pairs.get(index++);
    }

    /**
     * Get n unique pairs at once
     * @param n number of pairs to get
     * @return List of int[2] arrays
     */
    public List<int[]> getNPairs(int n) {
        if (n > pairs.size()) {
            throw new IllegalArgumentException("Cannot get more than 81 pairs");
        }
        List<int[]> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            result.add(next());
        }
        return result;
    }
}