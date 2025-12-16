package model.solver;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PermutationIterator implements Iterator<int[]> {
    private final int positions;
    private final int maxValue;
    private final long totalPermutations;
    private long currentIndex;

    public PermutationIterator(int positions, int maxValue) {
        this.positions = positions;
        this.maxValue = maxValue;
        this.totalPermutations = (long) Math.pow(maxValue, positions);
        this.currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < totalPermutations;
    }

    @Override
    public int[] next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        int[] permutation = new int[positions];
        long temp = currentIndex;

        for (int i = 0; i < positions; i++) {
            permutation[i] = (int) (temp % maxValue) + 1; // Values 1-9
            temp /= maxValue;
        }

        currentIndex++;
        return permutation;
    }

    public long getTotalPermutations() {
        return totalPermutations;
    }
}