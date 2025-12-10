package controller;

import controller.exceptions.NotFoundException;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Catalog {
    private boolean current;
    private boolean allModesExist;

    public Catalog() {
        this.current = checkForCurrentGame();
        this.allModesExist = checkAllModesExist();
    }

    private final List<String> difficultyFolders = Arrays.asList("easy", "medium", "hard");
    private final String incompleteFolder = "Incomplete";
    private final String baseFolder = "storage";

    private boolean checkForCurrentGame() {
        File folder = new File(baseFolder + "/" + incompleteFolder);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new NotFoundException("Folder not found");
        }

        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".csv")
        );

        return files != null && files.length > 0;
    }

    private boolean checkAllModesExist() {
        boolean allExist = true;

        for (String difficulty : difficultyFolders) {
            String folderPath = baseFolder + File.separator + difficulty;
            File folder = new File(folderPath);

            if (!folder.exists() || !folder.isDirectory()) {
                allExist = false;
                continue;
            }

            File[] files = folder.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".csv")
            );

            boolean hasCsvFile = files != null && files.length > 0;

            if (!hasCsvFile) {
                allExist = false;
            }
        }
        return allExist;
    }

    public boolean isCurrent() {
        return current;
    }

    public boolean isAllModesExist() {
        return allModesExist;
    }
}
