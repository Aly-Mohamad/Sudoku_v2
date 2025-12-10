package ui;

import controller.Catalog;
import controller.GameDriver;
import controller.GameStorage;
import controller.DifficultyEnum;
import controller.exceptions.InvalidGameException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SudokuGUI extends JFrame {

    private GameDriver driver;
    private GameBoardView boardView;
    private JPanel buttonPanel;
    private JButton solveBtn;
    private Catalog c = getCatalogue();

    public SudokuGUI(GameStorage storage) {
        driver = new GameDriver(storage);
        setTitle("Sudoku");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());


        //create another panel

        boardView = new GameBoardView();
        boardView.setCellChangeListener(() -> updateSolveButton());
        add(boardView, BorderLayout.CENTER);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton newGameBtn = new JButton("New Game");
        JButton resumeBtn = new JButton("Resume");
        JButton verifyBtn = new JButton("Verify");
        solveBtn = new JButton("Solve");
        JButton saveBtn = new JButton("Save & Exit");

        solveBtn.setEnabled(false);

        buttonPanel.add(newGameBtn);
        buttonPanel.add(resumeBtn);
        buttonPanel.add(verifyBtn);
        buttonPanel.add(solveBtn);
        buttonPanel.add(saveBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        newGameBtn.addActionListener(e -> startNewGame());
        resumeBtn.addActionListener(e -> resumeGame());
        verifyBtn.addActionListener(e -> verifyBoard());
        solveBtn.addActionListener(e -> solveBoard());
        saveBtn.addActionListener(e -> saveAndExit());

        setVisible(true);
    }

    private void startNewGame() {
        DifficultyEnum[] options = DifficultyEnum.values();
        char diffchar = ' ';
        DifficultyEnum difficulty = (DifficultyEnum) JOptionPane.showInputDialog(
                this,
                "Select Difficulty",
                "New Game",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (difficulty != null) {
            String[] games = driver.listGames(difficulty.toString().toLowerCase()).toArray(new String[0]);
            if (games.length == 0) {
                JOptionPane.showMessageDialog(this, "No puzzles available for " + difficulty);
                return;
            }
            String puzzle = (String) JOptionPane.showInputDialog(
                    this,
                    "Select Puzzle",
                    "New Game",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    games,
                    games[0]
            );

            if (puzzle != null) {
                try {
                    switch (difficulty) {
                        case EASY : diffchar = 'E'; break;
                        case MEDIUM : diffchar = 'M'; break;
                        case HARD : diffchar = 'H'; break;
                    }
                    driver.startNewGame(diffchar, puzzle);

                    boardView.displayBoard(driver.getCurrentGame());
                    updateSolveButton();

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to load puzzle: " + ex.getMessage());
                } catch (InvalidGameException ex){
                    JOptionPane.showMessageDialog(this, "Invalid Game");
                }
            }
        }
    }

    private void resumeGame() {
        if (!driver.hasIncomplete()) {
            JOptionPane.showMessageDialog(this, "No incomplete game to resume.");
            return;
        }
        try {
            driver.resumeIncomplete();
            boardView.displayBoard(driver.getCurrentGame());
            updateSolveButton();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load incomplete game: " + e.getMessage());
        }
    }

    private void verifyBoard() {
       try {
           boardView.updateBoard(driver.getCurrentGame());
           boolean valid = driver.verifyBoard(driver.getCurrentGame().getBoard());
           if (valid) {
               JOptionPane.showMessageDialog(this, "Board is valid!");
           }
           updateSolveButton();
       } catch (InvalidGameException ex) {
           JOptionPane.showMessageDialog(this, "Board has errors or incomplete cells.");
       }
    }

    private void solveBoard() {
        driver.solveBoard();
        boardView.displayBoard(driver.getCurrentGame());
        updateSolveButton();
    }

    private void saveAndExit() {
        boardView.updateBoard(driver.getCurrentGame());
        try {
            driver.saveCurrentGame("Incomplete","board");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save game: " + e.getMessage());
        }
        System.exit(0);
    }

    private int countEmptyCells() {
        int count = 0;
        int[][] board = driver.getCurrentGame().getBoard();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) count++;
            }
        }
        return count;
    }

    private void updateSolveButton() {
        solveBtn.setEnabled(countEmptyCells() == 5);
    }

    private Catalog getCatalogue() {
        return new Catalog();
    }
}
