# Sudoku Project - Issues Report

This document contains all issues discovered during code tracing analysis.

## 🔴 CRITICAL ISSUES (Will cause runtime errors or incorrect behavior)

### 1. **GameDriver.driveGames() - Wrong variable used** (Line 137)
**Location:** `src/controller/GameDriver.java:137`
**Issue:** Uses `solvedGame` instead of `sourceGame` parameter
```java
this.currentGame = generator.generate(solvedGame, sourceGame.getDifficulty());
```
**Problem:** When `driveGames()` is called, `solvedGame` may be null or stale. Should use `sourceGame` which is the parameter passed in.
**Impact:** Will cause NullPointerException or generate puzzle from wrong game.

### 2. **SudokuGUI.handleStartup() - NullPointerException** (Line 78)
**Location:** `src/ui/SudokuGUI.java:78`
**Issue:** Accesses `driver.getCurrentGame().getBoard()` when `currentGame` is null
```java
if (catalog[0]) {
    boardView.displayBoard(driver.getCurrentGame().getBoard());  // NPE!
    updateSolveButton();
}
```
**Problem:** At startup, `currentGame` in GameDriver is null. Need to load/resume incomplete game first.
**Impact:** Application will crash on startup if incomplete game exists.

### 3. **GameControllerAdapter.driveGames() - Wrong game assigned** (Line 53)
**Location:** `src/controller/GameControllerAdapter.java:53`
**Issue:** Sets `currentGame = source` instead of the generated puzzle
```java
driver.driveGames(source);
currentGame = source;  // Wrong! Should be generated puzzle
```
**Problem:** `driver.driveGames()` generates a puzzle and stores it in driver's `currentGame`, but adapter's `currentGame` is set to the source solved game instead.
**Impact:** Adapter's currentGame points to solved game, not the puzzle.

### 4. **SudokuGUI.startNewGame() - Case-sensitive path issue** (Line 107)
**Location:** `src/ui/SudokuGUI.java:107`
**Issue:** Uses `difficulty.toString()` which returns "EASY", "MEDIUM", "HARD" (uppercase)
```java
controller.driveGames(difficulty.toString());  // Passes "EASY" but folder is "easy"
```
**Problem:** Storage folders are lowercase ("easy", "medium", "hard"), but `difficulty.toString()` returns uppercase.
**Impact:** FileNotFoundException when trying to load game.

### 5. **SudokuGUI.handleStartup() - Logic flow issue** (Line 81)
**Location:** `src/ui/SudokuGUI.java:66-81`
**Issue:** Always calls `startNewGame()` even after resuming incomplete game
```java
if (catalog[0]) {
    boardView.displayBoard(driver.getCurrentGame().getBoard());
    updateSolveButton();
}
startNewGame();  // Always executes, overwrites resumed game
```
**Problem:** If incomplete game exists, it's displayed but then immediately overwritten by `startNewGame()`.
**Impact:** Cannot resume incomplete games.

### 6. **GameStorage/GameLoader - Case mismatch for "solution"** (Multiple locations)
**Location:** `src/controller/GameDriver.java:41` and `src/controller/GameLoader.java`
**Issue:** GameDriver saves to "Solution" but GameLoader expects "solution"
```java
// GameDriver line 41
storage.saveGame(solvedGame, "Solution", "solution");  // Folder: "Solution"

// GameLoader expects "solution" (lowercase) - line 51
Game source = loader.loadGame(sourcePath);
```
**Problem:** Case-sensitive filesystem means "Solution" ≠ "solution".
**Impact:** Cannot load saved solution games.

### 7. **GameControllerAdapter.driveGames() - Missing access to generated game**
**Location:** `src/controller/GameControllerAdapter.java:48-57`
**Issue:** No way to get the generated puzzle from driver after calling `driveGames()`
**Problem:** `driveGames()` returns void and doesn't expose the generated game. Adapter can't access it.
**Impact:** Adapter's currentGame becomes stale/incorrect.

### 8. **SudokuGUI.resumeGame() - Wrong method used** (Line 159)
**Location:** `src/ui/SudokuGUI.java:159`
**Issue:** Calls `controller.driveGames("Incomplete")` instead of proper resume logic
```java
controller.driveGames("Incomplete");  // This generates a puzzle, doesn't resume
```
**Problem:** `driveGames()` is for generating puzzles from solved games, not loading incomplete games. `GameDriver.resumeIncomplete()` exists but is never called.
**Impact:** Resume functionality doesn't work correctly - tries to generate puzzle from incomplete game instead of loading it.

## 🟡 LOGIC/DESIGN ISSUES

### 9. **Game.setBoard() - Encapsulation violation** (Line 35)
**Location:** `src/model/Game.java:35-37`
**Issue:** Directly assigns board reference instead of copying
```java
public void setBoard(int[][] board) {
    this.board = board;  // Should copy the array
}
```
**Problem:** External code can modify the internal board state.
**Impact:** Data integrity issues.

### 10. **Game.copy() - Missing difficulty field** (Line 65)
**Location:** `src/model/Game.java:65-67`
**Issue:** Doesn't copy difficulty field
```java
public Game copy() {
    return new Game(getBoard());  // Missing difficulty parameter
}
```
**Problem:** Copied game loses difficulty information.
**Impact:** Difficulty metadata lost.

### 11. **GameDriver.startNewGame() - Unused parameter** (Line 34)
**Location:** `src/controller/GameDriver.java:34`
**Issue:** `puzzleName` parameter is never used
**Problem:** Parameter suggests ability to select specific puzzle, but not implemented.
**Impact:** Misleading API.

### 12. **SudokuGUI.startNewGame() - Unused variable** (Line 106)
**Location:** `src/ui/SudokuGUI.java:106-108`
**Issue:** `board` variable from `getGame()` is never used
```java
int[][] board = controller.getGame(diff);  // Never used
controller.driveGames(difficulty.toString());
boardView.displayBoard(board);  // Should display generated puzzle, not this board
```
**Problem:** Displays wrong board (solved game instead of generated puzzle).
**Impact:** User sees solved game instead of puzzle.

### 13. **GameDriver.verifyBoard() - Inconsistent return behavior** (Line 55)
**Location:** `src/controller/GameDriver.java:55-60`
**Issue:** Always returns true or throws exception, never returns false
```java
public boolean verifyBoard(int[][] board) {
    if (validator.isValid())
        return true;
    else throw new InvalidGameException("Invalid game");  // Never returns false
}
```
**Problem:** Method signature suggests it can return false, but it never does.
**Impact:** Confusing API.

### 14. **GameDriver.solveGame() - Creates redundant solver** (Line 203)
**Location:** `src/controller/GameDriver.java:203`
**Issue:** Creates new Solver instance instead of using existing field
```java
model.solver.Solver solver = new model.solver.Solver();  // Field exists on line 22
```
**Problem:** Wastes memory, should use `this.solver`.
**Impact:** Minor performance issue.

### 15. **GameStorage.saveGame() - Missing directory creation** (Line 10)
**Location:** `src/controller/GameStorage.java:10-11`
**Issue:** Doesn't create directory if it doesn't exist
```java
File file = new File(BASE + "/" + mode + "/" + fileName + ".csv");
// No check/creation of parent directory
```
**Problem:** Will throw FileNotFoundException if "Solution" or other mode folders don't exist.
**Impact:** Save operations may fail.

## 📝 CODE QUALITY ISSUES

### 15. **Commented-out code**
Multiple files contain large blocks of commented code that should be removed or properly implemented.

### 16. **Inconsistent error handling**
Some methods throw exceptions, others return boolean/null, making error handling inconsistent.

### 17. **Missing null checks**
Several places access objects without checking for null (e.g., `getCurrentGame()` returns can be null).

---

## Summary

**Total Issues Found:** 18
- **Critical:** 8 (will cause runtime errors)
- **Logic/Design:** 6 (will cause incorrect behavior)
- **Code Quality:** 3 (should be addressed)

**Priority Fixes:**
1. Fix `GameDriver.driveGames()` to use `sourceGame` instead of `solvedGame`
2. Fix `SudokuGUI.handleStartup()` to properly handle null currentGame
3. Fix case sensitivity issues with "Solution" vs "solution"
4. Fix `SudokuGUI.startNewGame()` to use correct difficulty path
5. Fix logic flow in `handleStartup()` to not overwrite resumed games

