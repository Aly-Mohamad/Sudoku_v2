package controller;

import model.SudokuBoard;
import java.io.*;
import java.util.Scanner;



//To be edited not fully implemented


public class GameStorage {

    public static void saveBoard(SudokuBoard board, String folder, String filename) throws IOException {
        File dir = new File(folder);
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            int[][] b = board.getBoard();
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    writer.write(String.valueOf(b[r][c]));
                    if (c < 8) writer.write(","); // comma between numbers
                }
                writer.newLine();
            }
        }
    }

    public static SudokuBoard loadBoard(String filepath) throws IOException {
        SudokuBoard board = new SudokuBoard();
        File file = new File(filepath);

        try (Scanner sc = new Scanner(file)) {
            int row = 0;
            while (sc.hasNextLine() && row < 9) {
                String line = sc.nextLine();
                String[] tokens = line.trim().split(",");
                for (int col = 0; col < 9; col++) {
                    board.setCell(row, col, Integer.parseInt(tokens[col]));
                }
                row++;
            }
        }

        return board;
    }
}
