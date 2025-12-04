package controller;

import java.io.File;

public class GameCatalogue {
    private GameStorage storage;

    public GameCatalogue(GameStorage storage){
        this.storage = storage;
    }

    public String[] listGames(String difficulty){
        File[] files = storage.getBoards(difficulty);
        String[] names = new String[files.length];
        for(int i = 0;i<files.length;i++){
            names[i] = files[i].getName().replace(".csv","");
        }
        return names;
    }

    public boolean hasIncomplete(){
        return storage.hasBoard("incomplete");
    }
}
