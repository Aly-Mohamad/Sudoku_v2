public class Main {
    public static void main(String[] args) {
        String path = "src/invalid.csv";
        //String path = "src/sudoku.csv";

        SudokuBoard board = new SudokuBoard(path);
        SudokuValidator validator = new SudokuValidator(board.getBoard());
        validator.validate();
    }
}
