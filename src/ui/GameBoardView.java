package ui;

import model.SudokuBoard;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class GameBoardView extends JPanel {

    private JTextField[][] cells;
    private CellChangeListener listener;

    public interface CellChangeListener {
        void cellChanged();
    }

    public GameBoardView() {
        setLayout(new GridLayout(9, 9));
        cells = new JTextField[9][9];

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                JTextField cell = new JTextField();
                cell.setHorizontalAlignment(JTextField.CENTER);

                // Set borders to distinguish 3x3 boxes
                int top = (i % 3 == 0) ? 3 : 1;
                int left = (j % 3 == 0) ? 3 : 1;
                int bottom = (i == 8) ? 3 : 1;
                int right = (j == 8) ? 3 : 1;
                Border border = new MatteBorder(top, left, bottom, right, Color.BLACK);
                cell.setBorder(border);

                final int row = i;
                final int col = j;

                cell.getDocument().addDocumentListener(new DocumentListener() {
                    public void insertUpdate(DocumentEvent e) { notifyChange(); }
                    public void removeUpdate(DocumentEvent e) { notifyChange(); }
                    public void changedUpdate(DocumentEvent e) { notifyChange(); }

                    private void notifyChange() {
                        if (listener != null) listener.cellChanged();
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

    public void displayBoard(SudokuBoard board) {
        int[][] b = board.getBoard();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (b[i][j] == 0) {
                    cells[i][j].setText("");
                    cells[i][j].setEditable(true);
                    cells[i][j].setBackground(Color.WHITE);
                } else {
                    cells[i][j].setText(String.valueOf(b[i][j]));
                    cells[i][j].setEditable(false);
                    cells[i][j].setBackground(Color.LIGHT_GRAY);
                }
            }
        }
    }

    public void updateBoard(SudokuBoard board) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String text = cells[i][j].getText();
                int val = 0;
                if (!text.isEmpty()) {
                    try { val = Integer.parseInt(text); } catch (NumberFormatException ignored) {}
                }
                board.setCell(i, j, val);
            }
        }
    }
}
