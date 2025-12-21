package ui;

import controller.*;
import controller.exceptions.*;
import controller.interfaces.*;
import model.Game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SudokuGUI extends JFrame {
    private Controllable controller;
    private GameDriver driver;
    private GameBoardView boardView;
    private JButton solveBtn;

    public SudokuGUI(GameStorage storage) {
        driver = new GameDriver(storage);
        this.controller = new GameControllerAdapter(driver);

        setTitle("Sudoku");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        boardView = new GameBoardView();
        boardView.setCellChangeListener(this::updateSolveButton);
        add(boardView, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

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
        handleStartup();
    }

    private void handleStartup() {
        boolean[] catalog = controller.getCatalog();
        if (catalog[0]) {
            try {
                driver.resumeIncomplete();
                Game currentGame = driver.getCurrentGame();
                if (currentGame != null) {
                    boardView.displayBoard(currentGame.getBoard());
                    updateSolveButton();
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to resume incomplete game: " + e.getMessage());
            }
        }
        startNewGame();
    }

    private void startNewGame() {
        // Only allow EASY, MEDIUM, HARD
        DifficultyEnum difficulty = (DifficultyEnum) JOptionPane.showInputDialog(
                this,
                "Select Difficulty",
                "New Game",
                JOptionPane.PLAIN_MESSAGE,
                null,
                new DifficultyEnum[]{DifficultyEnum.EASY, DifficultyEnum.MEDIUM, DifficultyEnum.HARD},
                DifficultyEnum.EASY
        );

        if (difficulty == null) {
            return; // user cancelled
        }

        try {
            controller.driveGames(difficulty.toString().toLowerCase());
            boardView.displayBoard(driver.getCurrentGame().getBoard());
            updateSolveButton();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void resumeGame() {
        try {
            driver.resumeIncomplete();
            boardView.displayBoard(driver.getCurrentGame().getBoard());
            updateSolveButton();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to resume game: " + e.getMessage());
        }
    }

    private void solveBoard() {
        boardView.updateBoard(driver.getCurrentGame());

        if (!isPartialBoardValid(driver.getCurrentGame())) {
            JOptionPane.showMessageDialog(this, "Cannot solve: There are invalid numbers on the board.");
            return;
        }

        if (driver.getCurrentGame().countEmptyCells() != 5) {
            JOptionPane.showMessageDialog(this, "Solve button is only enabled when exactly 5 cells are empty.");
            return;
        }

        try {
            int[][] solved = controller.solveGame(driver.getCurrentGame().getBoard());
            boardView.displayBoard(solved);
            JOptionPane.showMessageDialog(this, "Solved successfully!");
            updateSolveButton();
        } catch (InvalidGameException e) {
            JOptionPane.showMessageDialog(this, "Failed to solve: " + e.getMessage());
        }
    }

    private void verifyBoard() {
        Game game = driver.getCurrentGame();
        if (game == null) {
            JOptionPane.showMessageDialog(this, "No game loaded.");
            return;
        }

        // Sync model from the UI before verifying
        boardView.updateBoard(game);

        Game solved = driver.getSolvedGame();
        if (solved == null) {
            JOptionPane.showMessageDialog(this, "No solution available to verify against.");
            return;
        }

        int[][] current = game.getBoard();
        int[][] solution = solved.getBoard();

        boolean[][] valid = new boolean[9][9];
        boolean anyInvalid = false;
        boolean isComplete = true;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int val = current[i][j];
                if (val == 0) {
                    valid[i][j] = true; // empty is fine for "verify anytime"
                    isComplete = false;
                } else {
                    boolean cellOk = (val == solution[i][j]);
                    valid[i][j] = cellOk;
                    if (!cellOk) anyInvalid = true;
            }
        }
    }

    // Visual feedback
    boardView.highlightValidity(valid);
    boardView.revalidate();
    boardView.repaint();

    // User feedback (so it never feels like "nothing happened")
    if (anyInvalid) {
        JOptionPane.showMessageDialog(this, "Conflicts highlighted in red.");
    } else if (isComplete) {
        JOptionPane.showMessageDialog(this, "Board is complete and correct!");
    } else {
        JOptionPane.showMessageDialog(this, "No conflicts so far.");
    }
}

    private void saveAndExit() {
        boardView.updateBoard(driver.getCurrentGame());
        Game game = driver.getCurrentGame();

        if (game.countEmptyCells() == 0) {
            System.exit(0);
        }

        if (!isPartialBoardValid(game)) {
            JOptionPane.showMessageDialog(this,
                    "Cannot save: The board contains invalid numbers.");
            return;
        }

        try {
            driver.saveCurrentGame("incomplete", "board");
            System.exit(0);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void updateSolveButton() {
        Game game = driver.getCurrentGame();
        solveBtn.setEnabled(game != null && game.countEmptyCells() == 5);
    }

    private boolean isPartialBoardValid(Game game) {
        int[][] current = game.getBoard();
        int[][] solution = driver.getSolvedGame().getBoard();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (current[i][j] != 0 && current[i][j] != solution[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}