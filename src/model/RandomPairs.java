package model;

import java.util.*;

public class RandomPairs {
    private static final int MAX_COORD = 80;
    private static final int MAX_UNIQUE_PAIRS = (MAX_COORD + 1) * (MAX_COORD + 1);
    private final Random random;

    public RandomPairs() {
        this.random = new Random();
    }

    public List<int[]> generateDistinctPairs(int n) {
        if (n < 0 || n > MAX_UNIQUE_PAIRS) {
            throw new IllegalArgumentException(
                    "n must be between 0 and " + MAX_UNIQUE_PAIRS + " (inclusive)");
        }
        Set<Integer> used = new HashSet<>();
        List<int[]> result = new ArrayList<>(n);
        while (result.size() < n) {
            int x = random.nextInt(MAX_COORD + 1);
            int y = random.nextInt(MAX_COORD + 1);

            int key = x * (MAX_COORD + 1) + y;
            if (used.add(key)) {
                result.add(new int[]{x, y});
            }
        }
        return result;
    }
}