package ui;

import model.Game;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class GameBoardView extends JPanel {

    private JTextField[][] cells;
    private CellChangeListener listener;

    public interface CellChangeListener {
        void cellChanged(int row, int col, int newVal, int prevVal);
    }

    public GameBoardView() {
        setLayout(new GridLayout(9, 9));
        cells = new JTextField[9][9];

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                JTextField cell = new JTextField();
                cell.setHorizontalAlignment(JTextField.CENTER);

                int top = (i % 3 == 0) ? 3 : 1;
                int left = (j % 3 == 0) ? 3 : 1;
                int bottom = (i == 8) ? 3 : 1;
                int right = (j == 8) ? 3 : 1;
                Border border = new MatteBorder(top, left, bottom, right, Color.BLACK);
                cell.setBorder(border);

                final int row = i;
                final int col = j;

                // DocumentFilter to allow only digits 1-9
                ((AbstractDocument) cell.getDocument()).setDocumentFilter(new DocumentFilter() {
                    @Override
                    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                        if (isValidInput(fb.getDocument().getLength(), string)) {
                            super.insertString(fb, offset, string, attr);
                            notifyChange(row, col);
                        }
                    }

                    @Override
                    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                        if (isValidInput(fb.getDocument().getLength() - length, text)) {
                            super.replace(fb, offset, length, text, attrs);
                            notifyChange(row, col);
                        }
                    }

                    @Override
                    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                        super.remove(fb, offset, length);
                        notifyChange(row, col);
                    }

                    private boolean isValidInput(int currentLength, String text) {
                        return (currentLength + text.length() <= 1) && text.matches("[1-9]?");
                    }

                    private void notifyChange(int r, int c) {
                        if (listener != null) {
                            String text = cells[r][c].getText();
                            int newVal = text.isEmpty() ? 0 : Integer.parseInt(text);
                            // Pass newVal; prevVal will be determined in SudokuGUI
                            listener.cellChanged(r, c, newVal, 0);
                        }
                    }
                });


                cells[i][j] = cell;
                add(cell);
            }
        }
    }

    public void setCellChangeListener(CellChangeListener l) {
        this.listener = l;
    }

    public void displayBoard(Game game) {
        displayBoard(game, null);
    }

    public void displayBoard(Game game, int[][] initialPuzzleState) {
        int[][] b = game.getBoard();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (b[i][j] == 0) {
                    cells[i][j].setText("");
                    cells[i][j].setEditable(true);
                    cells[i][j].setBackground(Color.WHITE);
                } else {
                    cells[i][j].setText(String.valueOf(b[i][j]));
                    // Only make non-editable if it was in the original puzzle
                    boolean isOriginal = (initialPuzzleState != null && initialPuzzleState[i][j] != 0);
                    cells[i][j].setEditable(!isOriginal);
                    cells[i][j].setBackground(isOriginal ? Color.LIGHT_GRAY : Color.WHITE);
                }
            }
        }
    }

    public void updateBoard(Game game) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String text = cells[i][j].getText();
                int val = 0;
                if (!text.isEmpty()) {
                    try { val = Integer.parseInt(text); } catch (NumberFormatException ignored) {}
                }
                game.setValue(i, j, val);
            }
        }
    }

    public int[][] getBoard() {
        int[][] board = new int[9][9];

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String text = cells[i][j].getText();
                if (text != null && !text.isEmpty()) {
                    try {
                        board[i][j] = Integer.parseInt(text);
                    } catch (NumberFormatException e) {
                        board[i][j] = 0;
                    }
                } else {
                    board[i][j] = 0;
                }
            }
        }
        return board;
    }

    public void highlightValidity(boolean[][] valid) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {

                // Skip empty cells
                if (cells[i][j].getText().isEmpty()) {
                    cells[i][j].setBackground(Color.WHITE);
                    continue;
                }

                if (valid[i][j]) {
                    // Keep the original background
                    if (cells[i][j].isEditable()) {
                        cells[i][j].setBackground(Color.WHITE);
                    } else {
                        cells[i][j].setBackground(Color.LIGHT_GRAY);
                    }
                } else {
                    // Invalid cell
                    cells[i][j].setBackground(new Color(255, 150, 150));
                }
            }
        }
    }

}
