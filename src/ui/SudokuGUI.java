package ui;

import controller.Catalog;
import controller.GameDriver;
import controller.GameStorage;
import controller.DifficultyEnum;
import controller.exceptions.InvalidGameException;
import controller.exceptions.NotFoundException;
import controller.exceptions.SolutionInvalidException;
import controller.interfaces.Viewable;
import model.Game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class SudokuGUI extends JFrame implements Viewable {

    private GameDriver driver;
    private GameBoardView boardView;
    private JButton solveBtn;
    private Catalog catalog = new Catalog();
    private GameStorage storage;

    public SudokuGUI(GameStorage storage) {
        this.storage = storage;
        driver = new GameDriver(storage);

        setTitle("Sudoku");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        boardView = new GameBoardView();
        boardView.setCellChangeListener((row, col, newVal, ignoredPrevVal) -> {
            Game game = driver.getCurrentGame();
            int oldVal = game.getValue(row, col); // true previous value

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

            driver.getCurrentGame().setValue(row, col, prevVal);
            storage.removeLastLogEntry();
            boardView.displayBoard(driver.getCurrentGame());

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Undo failed: " + ex.getMessage());
        }
    }

    // --- Startup logic ---
    private void handleStartup() {
        if (driver.hasIncomplete()) {
            try {
                driver.resumeIncomplete();
                driveGames(driver.getCurrentGame());
                updateSolveButton();
                return;
            } catch (IOException | SolutionInvalidException e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
        startNewGame();
    }

    // --- Game actions ---
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
            char diffChar = difficulty == DifficultyEnum.EASY ? 'E'
                    : difficulty == DifficultyEnum.MEDIUM ? 'M' : 'H';

            driver.startNewGame(diffChar, puzzle);
            driveGames(driver.getCurrentGame());
            updateSolveButton();
        } catch (IOException | InvalidGameException | SolutionInvalidException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void resumeGame() {
        if (!driver.hasIncomplete()) {
            JOptionPane.showMessageDialog(this, "No incomplete game to resume.");
            return;
        }
        try {
            driver.resumeIncomplete();
            driveGames(driver.getCurrentGame());
            updateSolveButton();
        } catch (IOException | SolutionInvalidException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void solveBoard() {
        boardView.updateBoard(driver.getCurrentGame());

        if (!isPartialBoardValid(driver.getCurrentGame())) {
            JOptionPane.showMessageDialog(this, "Cannot solve: There are invalid numbers on the board.");
            return;
        }

        try {
            solveGame(driver.getCurrentGame());
            driveGames(driver.getCurrentGame());
            updateSolveButton();
        } catch (InvalidGameException | SolutionInvalidException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void verifyBoard() {
        boardView.updateBoard(driver.getCurrentGame());

        if (isPartialBoardValid(driver.getCurrentGame())) {
            JOptionPane.showMessageDialog(this, "Sudoku is correct so far!");
        } else {
            JOptionPane.showMessageDialog(this, "There are invalid numbers!");
        }

        updateSolveButton();
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
            driver.saveCurrentGame("Incomplete", "board");
            System.exit(0);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // --- Helper methods ---
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

    // --- Viewable interface methods ---
    @Override
    public Catalog getCatalog() { return catalog; }

    @Override
    public Game getGame(DifficultyEnum level) throws NotFoundException {
        return driver.getCurrentGame();
    }

    @Override
    public void driveGames(Game source) throws SolutionInvalidException {
        driver.driveGames(source.getBoard());
        boardView.displayBoard(driver.getCurrentGame());
    }

    @Override
    public String verifyGame(Game game) {
        try {
            boolean valid = driver.verifyBoard(game.getBoard());
            return valid ? "Board is valid!" : "Board is invalid!";
        } catch (InvalidGameException e) {
            return "Board has errors or incomplete cells.";
        }
    }

    @Override
    public int[] solveGame(Game game) throws InvalidGameException {
        driver.solveBoard();
        return null;
    }

    @Override
    public void logUserAction(String userAction) { }
}
