package ui;

import controller.GameDriver;
import controller.GameStorage;
import controller.DifficultyEnum;
import controller.exceptions.InvalidGameException;
import controller.interfaces.Controllable;
import controller.GameControllerAdapter;
import model.Game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class SudokuGUI extends JFrame {
    private Controllable controller;
    private GameDriver driver;
    private GameBoardView boardView;
    private JButton solveBtn;
    private GameStorage storage;
    private int[][] initialPuzzleState; // Stores the original puzzle (non-zero cells are original)
    private boolean isUpdatingBoard = false; // Flag to prevent logging during programmatic updates

    public SudokuGUI(GameStorage storage) {
        this.storage = storage;
        driver = new GameDriver(storage);
        this.controller = new GameControllerAdapter(driver);

        setTitle("Sudoku");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        boardView = new GameBoardView();
        boardView.setCellChangeListener((row, col, newVal, ignoredPrevVal) -> {
            // Don't log if we're programmatically updating the board
            if (isUpdatingBoard) {
                return;
            }
            
            Game game = driver.getCurrentGame();
            if (game == null) return;
            
            int oldVal = game.getValue(row, col); // true previous value

            // Only log actual user changes (not when value is the same)
            if (newVal != oldVal) {
                try {
                    storage.appendLog("(" + row + "," + col + "," + newVal + "," + oldVal + ")");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Failed to log action: " + e.getMessage());
                }
                game.setValue(row, col, newVal); // update board
                updateSolveButton();
            }
        });
        add(boardView, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton newGameBtn = new JButton("New Game");
        JButton resumeBtn = new JButton("Resume");
        JButton verifyBtn = new JButton("Verify");
        solveBtn = new JButton("Solve");
        JButton saveBtn = new JButton("Save & Exit");
        JButton undoBtn = new JButton("Undo");

        solveBtn.setEnabled(false);

        buttonPanel.add(newGameBtn);
        buttonPanel.add(resumeBtn);
        buttonPanel.add(verifyBtn);
        buttonPanel.add(solveBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(undoBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        newGameBtn.addActionListener(e -> startNewGame());
        resumeBtn.addActionListener(e -> resumeGame());
        verifyBtn.addActionListener(e -> verifyBoard());
        solveBtn.addActionListener(e -> solveBoard());
        saveBtn.addActionListener(e -> saveAndExit());
        undoBtn.addActionListener(e -> undoLastAction());

        setVisible(true);
        handleStartup();
    }

    // --- Undo implementation ---
    private void undoLastAction() {
        try {
            List<String> logs = storage.readLogLines();
            if (logs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No more actions to undo.");
                return;
            }

            String last = logs.get(logs.size() - 1);
            last = last.replaceAll("[()]", "");
            String[] parts = last.split(",");
            int row = Integer.parseInt(parts[0].trim());
            int col = Integer.parseInt(parts[1].trim());
            int prevVal = Integer.parseInt(parts[3].trim());

            // Check if this cell was originally part of the puzzle
            if (initialPuzzleState != null && initialPuzzleState[row][col] != 0) {
                // This cell was originally in the puzzle - don't allow undoing it
                // Just remove the log entry and restore to original value
                storage.removeLastLogEntry();
                driver.getCurrentGame().setValue(row, col, initialPuzzleState[row][col]);
                
                // Update board without triggering log events
                isUpdatingBoard = true;
                try {
                    boardView.displayBoard(driver.getCurrentGame(), initialPuzzleState);
                } finally {
                    isUpdatingBoard = false;
                }
                return;
            }

            // Normal undo for user-entered cells (cells that were originally empty)
            driver.getCurrentGame().setValue(row, col, prevVal);
            storage.removeLastLogEntry();
            
            // Update board without triggering log events
            isUpdatingBoard = true;
            try {
                boardView.displayBoard(driver.getCurrentGame(), initialPuzzleState);
            } finally {
                isUpdatingBoard = false;
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Undo failed: " + ex.getMessage());
        }
    }

    // --- Startup logic ---
    private void handleStartup() {
        boolean[] catalog = controller.getCatalog();
        if (catalog[0]) {
            try {
                driver.resumeIncomplete();
                Game currentGame = driver.getCurrentGame();
                Game solvedGame = driver.getSolvedGame();
                if (currentGame != null && solvedGame != null) {
                    // Reconstruct initial puzzle state by comparing current game with solution
                    // Cells that match the solution are likely original puzzle cells
                    int[][] current = currentGame.getBoard();
                    int[][] solution = solvedGame.getBoard();
                    initialPuzzleState = new int[9][9];
                    for (int i = 0; i < 9; i++) {
                        for (int j = 0; j < 9; j++) {
                            // If current cell matches solution and is non-zero, it's likely original
                            if (current[i][j] != 0 && current[i][j] == solution[i][j]) {
                                initialPuzzleState[i][j] = current[i][j];
                            } else {
                                initialPuzzleState[i][j] = 0;
                            }
                        }
                    }
                    
                    // Update board without triggering log events
                    isUpdatingBoard = true;
                    try {
                        boardView.displayBoard(currentGame, initialPuzzleState);
                    } finally {
                        isUpdatingBoard = false;
                    }
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
        DifficultyEnum difficulty = (DifficultyEnum) JOptionPane.showInputDialog(
                this,
                "Select Difficulty",
                "New Game",
                JOptionPane.PLAIN_MESSAGE,
                null,
                new DifficultyEnum[]{DifficultyEnum.EASY, DifficultyEnum.MEDIUM, DifficultyEnum.HARD},
                DifficultyEnum.EASY
        );

        if (difficulty == null) return;

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
        if (puzzle == null) return;

        try {
            // Clear the log when starting a new game
            storage.clearLog();
            
            controller.driveGames(difficulty.toString().toLowerCase());
            // Store the initial puzzle state (right after generation)
            Game currentGame = driver.getCurrentGame();
            initialPuzzleState = currentGame.getBoard();
            
            // Update board without triggering log events
            isUpdatingBoard = true;
            try {
                boardView.displayBoard(currentGame, initialPuzzleState);
            } finally {
                isUpdatingBoard = false;
            }
            updateSolveButton();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void resumeGame() {
        try {
            driver.resumeIncomplete();
            Game currentGame = driver.getCurrentGame();
            Game solvedGame = driver.getSolvedGame();
            if (currentGame != null && solvedGame != null) {
                // Reconstruct initial puzzle state by comparing current game with solution
                int[][] current = currentGame.getBoard();
                int[][] solution = solvedGame.getBoard();
                initialPuzzleState = new int[9][9];
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        // If current cell matches solution and is non-zero, it's likely original
                        if (current[i][j] != 0 && current[i][j] == solution[i][j]) {
                            initialPuzzleState[i][j] = current[i][j];
                        } else {
                            initialPuzzleState[i][j] = 0;
                        }
                    }
                }
                
                // Update board without triggering log events
                isUpdatingBoard = true;
                try {
                    boardView.displayBoard(currentGame, initialPuzzleState);
                } finally {
                    isUpdatingBoard = false;
                }
                updateSolveButton();
            }
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
            // When solved, all cells should be non-editable (they're all "original" now)
            int[][] solvedAsInitial = new int[9][9];
            for (int i = 0; i < 9; i++) {
                System.arraycopy(solved[i], 0, solvedAsInitial[i], 0, 9);
            }
            
            // Update board without triggering log events
            isUpdatingBoard = true;
            try {
                boardView.displayBoard(new Game(solved), solvedAsInitial);
            } finally {
                isUpdatingBoard = false;
            }
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

    // --- Helper methods ---
    void updateSolveButton() {
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