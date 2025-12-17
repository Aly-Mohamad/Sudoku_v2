package ui;

import controller.*;
import controller.exceptions.InvalidGameException;
import controller.exceptions.NotFoundException;
import controller.exceptions.SolutionInvalidException;
import controller.interfaces.Controllable;
import controller.interfaces.Viewable;
import controller.GameControllerAdapter;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;


public class SudokuGUI extends JFrame {
    private Controllable controller;
    private GameDriver driver;
    private GameBoardView boardView;
    private JButton solveBtn;
    //private Catalog catalog = new Catalog();

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
//            try {
//                //controller.driveGames("Incomplete");
//                //boardView.displayBoard(boardView.getBoard());
//                boardView.displayBoard(driver.getCurrentGame().getBoard());
//                updateSolveButton();
//                return;
//            } catch (SolutionInvalidException e) {
//                JOptionPane.showMessageDialog(this, e.getMessage());
//            }
            boardView.displayBoard(driver.getCurrentGame().getBoard());
            updateSolveButton();
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
                DifficultyEnum.values(),
                DifficultyEnum.EASY
        );

        if (difficulty == null) {
            return; // user cancelled
        }
        char diff = switch (difficulty) {
            case EASY -> 'E';
            case MEDIUM -> 'M';
            case HARD -> 'H';
            default ->  'E';
        };


        try {
            int[][] board = controller.getGame(diff);
            controller.driveGames(difficulty.toString());
            boardView.displayBoard(board);
            updateSolveButton();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

//    private void startNewGame() {
//        DifficultyEnum difficulty = (DifficultyEnum) JOptionPane.showInputDialog(
//                this,
//                "Select Difficulty",
//                "New Game",
//                JOptionPane.PLAIN_MESSAGE,
//                null,
//                new DifficultyEnum[]{DifficultyEnum.EASY, DifficultyEnum.MEDIUM, DifficultyEnum.HARD},
//                DifficultyEnum.EASY
//        );
//
//        if (difficulty == null) return;
//
//        String[] games = driver.listGames(difficulty.toString().toLowerCase()).toArray(new String[0]);
//        if (games.length == 0) {
//            JOptionPane.showMessageDialog(this, "No puzzles available for " + difficulty);
//            return;
//        }
//
//        String puzzle = (String) JOptionPane.showInputDialog(
//                this,
//                "Select Puzzle",
//                "New Game",
//                JOptionPane.PLAIN_MESSAGE,
//                null,
//                games,
//                games[0]
//        );
//        if (puzzle == null) return;
//
//        try {
////            char diffChar = difficulty == DifficultyEnum.EASY ? 'E'
////                    : difficulty == DifficultyEnum.MEDIUM ? 'M' : 'H';
//
//            driver.startNewGame(difficulty, puzzle);
//            driveGames(driver.getCurrentGame());
//            updateSolveButton();
//        } catch (IOException | InvalidGameException | SolutionInvalidException e) {
//            JOptionPane.showMessageDialog(this, e.getMessage());
//        }
//    }

    private void resumeGame() {
        try {
            controller.driveGames("Incomplete");
            boardView.displayBoard(boardView.getBoard());
            updateSolveButton();
        } catch (SolutionInvalidException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
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
            int[][] solved = controller.solveGame(boardView.getBoard());
            boardView.displayBoard(solved);
            JOptionPane.showMessageDialog(this, "Solved successfully!");
            updateSolveButton();
        } catch (InvalidGameException e) {
            JOptionPane.showMessageDialog(this, "Failed to solve: " + e.getMessage());
        }
    }

    private void verifyBoard() {
        boolean[][] valid = controller.verifyGame(boardView.getBoard());
        boardView.highlightValidity(valid);
    }


//    private void verifyBoard() {
//        boardView.updateBoard(driver.getCurrentGame());
//
//        if (isPartialBoardValid(driver.getCurrentGame())) {
//            JOptionPane.showMessageDialog(this, "Sudoku is correct so far!");
//        } else {
//            JOptionPane.showMessageDialog(this, "There are invalid numbers!");
//        }
//
//        updateSolveButton();
//    }


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



//    @Override
//    public Game getGame(DifficultyEnum level) throws NotFoundException {
//        return driver.getCurrentGame();
//    }

//    @Override
//    public int[][] getGame(char level) throws NotFoundException {
//        DifficultyEnum diff;
//        switch (Character.toUpperCase(level)) {
//            case 'E': diff = DifficultyEnum.EASY; break;
//            case 'M': diff = DifficultyEnum.MEDIUM; break;
//            case 'H': diff = DifficultyEnum.HARD; break;
//            default:
//                throw new NotFoundException("Invalid difficulty: " + level);
//        }
//        return driver.getGame(diff).getBoard();
//    }
//
//    @Override
//    public void driveGames(Game source) throws SolutionInvalidException {
//        driver.driveGames(source.getBoard());
//        boardView.displayBoard(driver.getCurrentGame());
//    }
//
//    @Override
//    public String verifyGame(Game game) {
//        try {
//            boolean valid = driver.verifyBoard(game.getBoard());
//            return valid ? "Board is valid!" : "Board is invalid!";
//        } catch (InvalidGameException e) {
//            return "Board has errors or incomplete cells.";
//        }
//    }
//
//    @Override
//    public int[] solveGame(Game game) throws InvalidGameException {
//        try {
//            // Use the solver
//            model.solver.Solver solver = new model.solver.Solver();
//            int[] solution = solver.solve(game);
//
//            // Apply the solution to the game
//            for (int i = 0; i < solution.length; i += 3) {
//                int row = solution[i];
//                int col = solution[i + 1];
//                int value = solution[i + 2];
//                game.setValue(row, col, value);
//            }
//
//            // Update the board view
//            boardView.displayBoard(game);
//            updateSolveButton();
//
//            return solution;
//        } catch (Exception e) {
//            throw new InvalidGameException("Failed to solve: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public void logUserAction(String userAction) {
//    }
}
