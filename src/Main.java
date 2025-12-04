import model.SudokuBoard;
import model.SudokuValidator;

public class Main {
    public static void main(String[] args) {
        // String path = "src/invalid.csv";
        String path = "src/sudoku.csv";

        SudokuBoard board = new SudokuBoard(path);
        SudokuValidator validator = new SudokuValidator(board.getBoard());

        String result = validator.validate();

        System.out.println("Validation Result: " + result);

        if (result.equals("INVALID")) {
            System.out.println("Errors found:");
            for (String error : validator.getErrors()) {
                System.out.println(error);
            }
        }
    }
}