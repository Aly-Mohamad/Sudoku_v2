package controller;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import model.Game;

public class GameStorage {
    private static final String BASE = "storage";
    private static final String INCOMPLETE = "Incomplete";

    public void saveGame(Game game, String mode, String fileName) throws IOException {
        File file = new File(BASE + "/" + mode + "/" + fileName + ".csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < 9; i++) {
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < 9; j++) {
                    line.append(game.getValue(i, j));
                    if (j < 8) line.append(",");
                }
                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

    public void deleteGame(String mode, String fileName) throws IOException {
        File oldGame = new File(BASE + "/" + mode + "/" + fileName + ".csv");
        if (!oldGame.exists()) {
            throw new FileNotFoundException("File not found: " + oldGame.getAbsolutePath());
        }
        oldGame.delete();
    }

    public boolean hasBoard(String mode) {
        File folder = new File(BASE + "/" + mode);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".csv"));
        return files != null && files.length > 0;
    }

    public File[] getBoards(String mode) {
        return new File(BASE + "/" + mode).listFiles((dir, name) -> name.endsWith(".csv"));
    }

    public void deleteIncomplete() {
        File dir = new File(BASE + "/" + INCOMPLETE);
        if (dir.exists()) {
            for (File file : dir.listFiles()) file.delete();
        }
    }

    public void saveIncomplete(Game game, String fileName) throws IOException {
        saveGame(game, INCOMPLETE, fileName);
    }

    // Logging support for Undo
    public void appendLog(String entry) throws IOException {
        File file = new File(BASE + "/" + INCOMPLETE + "/log.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(entry);
            writer.newLine();
        }
    }

    public List<String> readLogLines() throws IOException {
        File file = new File(BASE + "/" + INCOMPLETE + "/log.txt");
        if (!file.exists()) return new ArrayList<>();
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public void removeLastLogEntry() throws IOException {
        List<String> lines = readLogLines();
        if (lines.isEmpty()) return;
        lines.remove(lines.size() - 1);
        File file = new File(BASE + "/" + INCOMPLETE + "/log.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public void clearLog() throws IOException {
        File file = new File(BASE + "/" + INCOMPLETE + "/log.txt");
        // Ensure parent directory exists
        file.getParentFile().mkdirs();
        // Write empty content to file, clearing any existing content
        Files.write(file.toPath(), new byte[0]);
    }
}
