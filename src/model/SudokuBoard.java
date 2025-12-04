package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SudokuBoard {

    private int[][] board;

    public SudokuBoard(String filename) {
        int[][] array = new int[9][9];
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null && i < 9) {
                String[] parts = line.split(",");
                for (int j = 0; j < 9; j++) {
                    array[i][j] = Integer.parseInt(parts[j].replaceAll("[^\\d]", ""));
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.board = array;
    }

    public SudokuBoard(int[][] boardArray) {
        board = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(boardArray[i], 0, board[i], 0, 9);
        }
    }


    public int[][] getBoard() {
        return board;
    }

    public void setCell(int row, int col, int value) {
        board[row][col] = value;
    }

    public int getCell(int row, int col) {
        return board[row][col];
    }

    public boolean isEmpty(int row, int col) {
        return board[row][col] == 0;
    }

    public SudokuBoard copy(){
        return new SudokuBoard(this.board);
    }

}