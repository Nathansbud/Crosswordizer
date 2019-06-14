import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import processing.core.PApplet;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;


import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

public class Crosswordizer extends PApplet {
    private static Crosswordizer app = new Crosswordizer();
    private static Board board;

    @Override
    public void settings() {
        fullScreen();
    }
    @Override
    public void setup() {

    }

    @Override
    public void draw() {
        background(0);
    }

    private static void loadPuzzle() {
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader f = new FileReader("data" + File.separator + "puzzle.json");
            JSONObject puzzleData = (JSONObject) jsonParser.parse(f);
            f.close();

            Iterator keys = puzzleData.keySet().iterator();
            Iterator values = puzzleData.values().iterator();
            Board b = new Board();
            String currentKey;

            while(keys.hasNext()) {
                currentKey = (String)keys.next();
                switch(currentKey) {
                    case "date":
                        b.setDate((String)values.next());
                        break;
                    case "weekday":
                        b.setWeekday((String)values.next());
                        break;
                    case "size":
                        b.setSize(Integer.parseInt(String.valueOf(values.next()))); //Seems to come out a long which is hard to int cast
                        break;
                    case "down_clues":
                    case "across_clues":
                        for(Object obj : (JSONArray)values.next()) {
                            Clue c = new Clue();

                            Iterator clueKeys = ((JSONObject)obj).keySet().iterator();
                            Iterator clueValues = ((JSONObject)obj).values().iterator();

                            while(clueKeys.hasNext()) {
                                switch((String)clueKeys.next()) {
                                    case "number":
                                        c.setNumber(Integer.parseInt(String.valueOf(clueValues.next())));
                                        break;
                                    case "question":
                                        c.setQuestion((String)clueValues.next());
                                        break;
                                    case "answer":
                                        c.setAnswer((String)clueValues.next());
                                        break;
                                    default:
                                        break;
                                }
                            }
                            if(currentKey.equals("down_clues")) {
                                c.setDirection(Clue.Direction.DOWN);
                                b.addDownClue(c);
                            } else {
                                c.setDirection(Clue.Direction.ACROSS);
                                b.addAcrossClue(c);
                            }
                        }
                        break;
                    case "board":
                        break;
                    default:
                         break;
                }
            }

            board = b;
        } catch(ParseException | IOException e) {
            System.out.println("Stuff failed :(((");
        }
    }

    public static void main(String[] args) {
        try {
            Runtime.getRuntime().exec("/Users/zackamiton/Code/Crosswordizer/scraper/scraper.py");
        } catch(IOException e) {
            e.printStackTrace();
        }

//        PApplet.runSketch(new String[]{"Crossword"}, app);
        loadPuzzle(); //Load board
        for(Clue c : board.getAcrossClues()) {
            System.out.println(c.getQuestion() + " : " + c.getAnswer());
        }

    }
}
