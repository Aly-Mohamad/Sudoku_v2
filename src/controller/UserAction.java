package controller;

/**
 * Represents a user action on the Sudoku board.
 * Used by the View layer to communicate user actions to the Controller.
 */
public class UserAction {
    private final int row;
    private final int col;
    private final int newVal;
    private final int prevVal;
    
    public UserAction(int row, int col, int newVal, int prevVal) {
        this.row = row;
        this.col = col;
        this.newVal = newVal;
        this.prevVal = prevVal;
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
    
    public int getNewVal() {
        return newVal;
    }
    
    public int getPrevVal() {
        return prevVal;
    }
    
    /**
     * Converts UserAction to log string format: (row,col,newVal,prevVal)
     */
    public String toLogString() {
        return "(" + row + "," + col + "," + newVal + "," + prevVal + ")";
    }
}
