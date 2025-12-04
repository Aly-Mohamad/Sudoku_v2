package controller.exceptions;

//Thrown for invalid or incomplete sudoku
public class SolutionInvalidException extends Exception {

    public SolutionInvalidException(String message) {
        super(message);
    }

    public SolutionInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}