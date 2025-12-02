import java.util.List;

public class CheckerFactory {
    public static Checker createChecker(String type, int[][] board, List<String> errors, int index) {
        return switch (type) {
            case "ROW" -> new RowChecker(board, errors, index);
            case "COL" -> new ColumnChecker(board, errors, index);
            case "BOX" -> new BoxChecker(board, errors, index);
            default -> throw new IllegalArgumentException("Unknown checker type");
        };
    }
}
