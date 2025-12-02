import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

//        if (args.length != 2) {
//            System.out.println("Usage: java -jar app.jar <csv-path> <mode>");
//            return;
//        }
//
//        String path = args[0];
//        int mode = Integer.parseInt(args[1]);

        System.out.print("Enter mode 0/3/27: ");
        Scanner scanner = new Scanner(System.in);
        int mode = scanner.nextInt();
        String path = "src/invalid_sudoku.csv";

        SudokuBoard board = new SudokuBoard(path);
        SudokuValidator validator = new SudokuValidator(board.getBoard());

        validator.validate(mode);
    }
}
