package controller;

import model.Game;
import java.io.*;
import java.util.Random;

public class GameLoader {
    public Game loadGame(String mode_upper) throws IOException {
        String mode = mode_upper.toLowerCase();

        File modeDirectory = new File("storage/" + mode + "/");

        if (!modeDirectory.exists() || !modeDirectory.isDirectory()) {
            throw new FileNotFoundException("Mode directory not found: " + modeDirectory.getAbsolutePath());
        }

        File[] csvFiles = modeDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));

        if (csvFiles == null || csvFiles.length == 0) {
            throw new FileNotFoundException("No game files found in mode: " + mode);
        }

        Random random = new Random();
        File selectedFile = csvFiles[random.nextInt(csvFiles.length)];

        int[][] board = new int[9][9];

        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
            for (int i = 0; i < 9; i++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IOException("Invalid file format: " + selectedFile.getName());
                }
                String[] parts = line.split(",");
                if (parts.length != 9) {
                    throw new IOException("Invalid row format in: " + selectedFile.getName());
                }
                for (int j = 0; j < 9; j++) {
                    board[i][j] = Integer.parseInt(parts[j]);
                }
            }
        }

        DifficultyEnum diff;
        switch (mode) {
            case "easy": diff = DifficultyEnum.EASY; break;
            case "medium": diff = DifficultyEnum.MEDIUM; break;
            case "hard": diff = DifficultyEnum.HARD; break;
            case "incomplete": diff = DifficultyEnum.INCOMPLETE; break;
            case "solution": diff = DifficultyEnum.INCOMPLETE; break; // neutral for solution files
            default: throw new IOException("Invalid mode: " + mode);
        }

        return new Game(board, diff);
    }

    public Game loadIncomplete() throws IOException {
        return loadGame("incomplete");
    }
}