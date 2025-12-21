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
        void cellChanged();
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
                            notifyChange();
                        }
                    }

                    @Override
                    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                        if (isValidInput(fb.getDocument().getLength() - length, text)) {
                            super.replace(fb, offset, length, text, attrs);
                            notifyChange();
                        }
                    }

                    @Override
                    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                        super.remove(fb, offset, length);
                        notifyChange();
                    }

                    private boolean isValidInput(int currentLength, String text) {
                        // Only allow a single character and it must be 1-9
                        return (currentLength + text.length() <= 1) && text.matches("[1-9]?");
                    }

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

    public void displayBoard(Game game) {
        int[][] b = game.getBoard();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cells[i][j] == null) continue;
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

    public void updateBoard(Game game) {
        if (cells == null) return;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cells[i][j] == null) continue;
                String text = cells[i][j].getText();
                int val = 0;
                if (!text.isEmpty()) {
                    try { val = Integer.parseInt(text); } catch (NumberFormatException ignored) {}
                }
                game.setValue(i, j, val);
            }
        }
    }
}
