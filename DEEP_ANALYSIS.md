# Deep Code Analysis - Critical Runtime Issues

After carefully tracing execution flow, here are the **actual bugs** causing the program to fail:

---

## 🔴 CRITICAL BUG #1: Missing solvedGame Assignment

**Location:** `src/controller/GameDriver.java:135-141`

**Problem:**
When `driveGames()` is called, it generates a puzzle but **doesn't save the solvedGame**:

```java
@Override
public void driveGames(Game sourceGame) throws SolutionInvalidException{
    try {
        this.currentGame = generator.generate(sourceGame, sourceGame.getDifficulty());
        // ❌ Missing: this.solvedGame = sourceGame;
    } catch (Exception e) {
        throw new SolutionInvalidException(e.getMessage());
    }
}
```

**Impact:**
- Later, `isPartialBoardValid()` at line 240 calls `driver.getSolvedGame().getBoard()`
- `getSolvedGame()` returns null
- **NullPointerException** when saving or validating!

**Fix:**
```java
@Override
public void driveGames(Game sourceGame) throws SolutionInvalidException{
    try {
        this.solvedGame = sourceGame;  // ✓ Save the solved game
        this.currentGame = generator.generate(sourceGame, sourceGame.getDifficulty());
    } catch (Exception e) {
        throw new SolutionInvalidException(e.getMessage());
    }
}
```

---

## 🔴 CRITICAL BUG #2: Missing Directory Creation

**Location:** `src/controller/GameStorage.java:10-11`

**Problem:**
When saving to "solution" folder, if it doesn't exist, `FileWriter` will throw `FileNotFoundException`:

```java
public void saveGame(Game game, String mode, String fileName) throws IOException {
    File file = new File(BASE + "/" + mode + "/" + fileName + ".csv");
    // ❌ No check/creation of parent directory
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        // ...
    }
}
```

**Impact:**
- First time saving solution will fail if folder doesn't exist
- Also affects "Incomplete" folder

**Fix:**
```java
public void saveGame(Game game, String mode, String fileName) throws IOException {
    File file = new File(BASE + "/" + mode + "/" + fileName + ".csv");
    file.getParentFile().mkdirs();  // ✓ Create directory if it doesn't exist
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        // ...
    }
}
```

---

## 🔴 CRITICAL BUG #3: Wasteful getGame Call

**Location:** `src/ui/SudokuGUI.java:103`

**Problem:**
`startNewGame()` calls `controller.getGame(diff)` which loads a solved game, but this result is never used:

```java
int[][] board = controller.getGame(diff);  // Loads solved game, never used
controller.driveGames(difficulty.toString().toLowerCase());  // Loads different random game
```

**Impact:**
- Loads a game that's immediately discarded
- `driveGames()` loads a different random game anyway
- Wasteful but not breaking

**Fix:**
Remove the unused line:
```java
// Remove: int[][] board = controller.getGame(diff);
controller.driveGames(difficulty.toString().toLowerCase());
```

---

## 🔴 CRITICAL BUG #4: solveBoard Uses Wrong Source

**Location:** `src/ui/SudokuGUI.java:179`

**Problem:**
Uses `boardView.getBoard()` instead of `driver.getCurrentGame()`:

```java
int[][] solved = controller.solveGame(boardView.getBoard());  // ❌ From view
```

**Issue:**
- View might not be synced with driver's currentGame
- Should use `driver.getCurrentGame().getBoard()` for consistency

**Fix:**
```java
// First update view to sync, then use driver's game
boardView.updateBoard(driver.getCurrentGame());
int[][] solved = controller.solveGame(driver.getCurrentGame().getBoard());
```

Actually wait, the code already updates at line 166. But line 179 uses boardView.getBoard() which might be out of sync. Should use driver.getCurrentGame() after the update.

---

## 🟡 LOGIC ISSUE: verifyBoard Modifies Board

**Location:** `src/controller/GameControllerAdapter.java:61-79`

**Problem:**
`verifyGame()` temporarily modifies the board array:

```java
board[i][j] = 0;  // ❌ Modifies original array
SudokuValidator validator = new SudokuValidator(board);
valid[i][j] = validator.isCellValid(i, j);
board[i][j] = val;  // Restores it
```

**Issue:**
- Modifies the original board array
- Could cause issues if called concurrently or if validator holds references
- Should work on a copy

---

## 🟡 MINOR: Catalog throws Exception in Constructor

**Location:** `src/controller/Catalog.java:21-26`

**Problem:**
If "Incomplete" folder doesn't exist, constructor throws exception:

```java
if (!folder.exists() || !folder.isDirectory()) {
    throw new NotFoundException("Folder not found");  // ❌ Constructor throws
}
```

**Issue:**
- Throwing exception in constructor is bad practice
- Should handle gracefully (return false instead)

---

## Summary of Required Fixes

### Must Fix (Program Won't Work):
1. **Add `this.solvedGame = sourceGame;` in `driveGames()`** - Prevents NPE
2. **Add `file.getParentFile().mkdirs();` in `saveGame()`** - Prevents save failures

### Should Fix (Logic/Performance):
3. Remove unused `getGame()` call in `startNewGame()`
4. Use `driver.getCurrentGame()` consistently in `solveBoard()`

### Nice to Fix (Code Quality):
5. Fix `verifyGame()` to work on copy instead of modifying original
6. Fix Catalog constructor to not throw exceptions

---

## Priority Order:
1. **Bug #1** (solvedGame) - **CRITICAL** - Will cause NPE
2. **Bug #2** (directory creation) - **CRITICAL** - Will cause save failures
3. Bug #3 (unused code) - Minor cleanup
4. Bug #4 (solveBoard source) - Logic improvement

