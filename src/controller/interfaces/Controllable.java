package controller.interfaces;

import controller.Catalog;
import controller.UserAction;
import controller.exceptions.InvalidGameException;
import controller.exceptions.NotFoundException;
import controller.exceptions.SolutionInvalidException;

import java.io.IOException;

public interface Controllable {
    boolean[] getCatalog();

    int[][] getGame(char level) throws NotFoundException;

    void driveGames(String sourcePath) throws SolutionInvalidException;

    boolean[][] verifyGame(int[][] game);

    int[][] solveGame(int[][] game) throws InvalidGameException;

    void logUserAction(UserAction userAction) throws IOException;
}
