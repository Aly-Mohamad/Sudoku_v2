package controller.interfaces;

import controller.Catalog;
import controller.UserAction;
import controller.exceptions.InvalidGameException;
import controller.exceptions.NotFoundException;
import controller.exceptions.SolutionInvalidException;

import java.io.IOException;

public interface Controllable {
    Catalog getCatalog();

    int[][] getGame(char level) throws NotFoundException;

    void driveGames(int[][] source) throws SolutionInvalidException;

    boolean[][] verifyGame(int[][] game);

    void solveBoard();

    int[][] solveGame(int[][] game) throws InvalidGameException;

    //void logUserAction(UserAction userAction) throws IOException;
}
