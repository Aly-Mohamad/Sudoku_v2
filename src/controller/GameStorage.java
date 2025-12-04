package controller;

import model.SudokuBoard;
import java.io.*;
import java.util.Scanner;



//To be edited not fully implemented


public class GameStorage {


    public GameStorage() {
        createDirectories();
    }


    private void createDirectories() {
        String []modes ={"easy","medium","hard","incomplete"};

        File base = new File("storage");
        if(!base.exists()){
            base.mkdir();
        }
        for(String mode:modes){
            File modeDir = new File(base,mode);
            if(!modeDir.exists()){
                modeDir.mkdir();
            }
        }
    }

    public void saveGame(SudokuBoard board,String mode,String fileName) throws IOException {
        File file = new File("storage/"+mode+"/"+fileName+".csv");

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
            for(int i = 0;i<9;i++){
                StringBuilder line = new StringBuilder();
                for(int j = 0;j<9;j++){
                    line.append(board.getCell(i,j));
                    if(j!=8) line.append(",");
                }
                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

    public SudokuBoard loadGame(String mode,String fileName) throws IOException {
        File oldGame = new File("storage/"+mode+"/"+fileName+".csv");
        if(!oldGame.exists()){
            throw new FileNotFoundException("File not found: "+oldGame.getAbsolutePath());
        }
        int [][]board = new int[9][9];
        try(BufferedReader reader = new BufferedReader(new FileReader(oldGame))){
            for(int i = 0;i<9;i++){
                String line = reader.readLine();
                String[] parts = line.split(",");
                for(int j = 0;j<9;j++){
                    board[i][j] = Integer.parseInt(parts[j]);
                }
            }
        }
        return new SudokuBoard(board);
    }

    public void deleteGame(String mode,String fileName) throws IOException {
        File oldGame = new File("storage/"+mode+"/"+fileName+".csv");
        if(!oldGame.exists()){
            throw new FileNotFoundException("File not found: "+oldGame.getAbsolutePath());
        }
        oldGame.delete();
    }

    public boolean hasBoard(String mode){
        File folder = new File("storage/"+mode);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".csv"));
        return files!=null && files.length>0;
    }

    public File[] getBoards(String mode){
        return new File("storage/"+mode).listFiles((dir, name) -> name.endsWith(".csv"));
    }

    public void deleteIncomplete(){
        File incomplete = new File("storage/incomplete");
        if(incomplete.exists()){
            for(File file:incomplete.listFiles()){
                file.delete();
            }
        }
    }

    public void saveIncomplete(SudokuBoard board,String fileName) throws IOException {
        saveGame(board,"incomplete",fileName);
    }

    public SudokuBoard loadIncomplete(String fileName) throws IOException {
        return loadGame("incomplete",fileName);
    }

    public void saveLog(String logText) throws IOException {
        File file = new File("storage/log.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(logText);
        }
    }

    public String loadLog() throws IOException {
        File file = new File("storage/log.txt");
        if (!file.exists()) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    public void deleteLog() {
        File file = new File("storage/log.txt");
        if (file.exists()) {
            file.delete();
        }
    }

}
