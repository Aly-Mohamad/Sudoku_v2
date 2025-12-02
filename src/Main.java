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

        System.out.print("Choose the mode to check the sudoku. 0/3/27: ");
        Scanner scanner = new Scanner(System.in);
        int mode = Integer.parseInt(scanner.next());

        while(mode !=0 && mode != 3 && mode != 27){
            System.out.println("You entered an invalid mode.Try again.");
            System.out.print("Choose the mode to check the sudoku. 0/3/27: ");
            mode = Integer.parseInt(scanner.next());
        }

        String path = "src/invalid.csv";
        //String path = "src/sudoku.csv";

        SudokuBoard board = new SudokuBoard(path);
        SudokuValidator validator = new SudokuValidator(board.getBoard());

        validator.validate(mode);
    }
}
