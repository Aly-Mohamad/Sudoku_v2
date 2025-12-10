package controller;

import model.Game;
import java.io.*;

public class GameLoader {
    public Game loadGame(String mode, String fileName) throws IOException {
        File file = new File("storage/" + mode + "/" + fileName + ".csv");

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }

        int[][] board = new int[9][9];

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            for (int i = 0; i < 9; i++) {
                String line = reader.readLine();
                String[] parts = line.split(",");

                for (int j = 0; j < 9; j++) {
                    board[i][j] = Integer.parseInt(parts[j]);
                }
            }
        }

        return new Game(board);
    }

    public Game loadIncomplete(String filename) throws IOException {
        return loadGame("Incomplete",filename);
    }
}
