import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import processing.core.PApplet;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;


import java.io.*;
import java.util.Iterator;

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

    public static Object checkedGet(JSONObject jsonObj, String key) {
        return (jsonObj.containsKey(key)) ? (jsonObj.get(key)) : null;
    }

    private static void condLoadPuzzle() {
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader f = new FileReader("data" + File.separator + "puzzle.json");
            JSONObject puzzleData = (JSONObject)jsonParser.parse(f);
            f.close();

            Board b = new Board();

            b.setSize(Integer.parseInt(String.valueOf(checkedGet(puzzleData, "size"))));
            b.setDate((String)checkedGet(puzzleData, "date"));
            b.setWeekday((String)checkedGet(puzzleData, "weekday")); //Checked gets for futureproofing

            Object[] clueSet = new Object[]{puzzleData.get("down_clues"), puzzleData.get("across_clues")};
            int count = 0;

            for(Object obj : clueSet) {
                if (obj != null) {
                    for(Object o : (JSONArray)obj) {
                        Clue c = new Clue();
                        JSONObject clue = (JSONObject)o;

                        c.setMarker(String.valueOf(checkedGet(clue, "marker")));
                        c.setQuestion((String)checkedGet(clue, "question"));
                        c.setAnswer((String)checkedGet(clue, "answer"));

                        if(count == 0) {
                            c.setDirection(Clue.Direction.DOWN);
                            b.addDownClue(c);
                        } else {
                            c.setDirection(Clue.Direction.ACROSS);
                            b.addAcrossClue(c);
                        }
                    }
                }
                count++;
            }

            JSONArray tiles = (JSONArray)checkedGet(puzzleData, "board");
            if (tiles != null) {
                for(Object obj : tiles) {
                    Tile t = new Tile();
                    JSONObject tile = (JSONObject)obj;

                    t.setRow(Integer.valueOf(String.valueOf(tile.get("row"))));
                    t.setCol(Integer.valueOf(String.valueOf(tile.get("column"))));

                    if(tile.containsKey("marker")) t.setMarker((String.valueOf(tile.get("marker"))));
                    if(tile.containsKey("value")) {
                        t.setCorrectValue((String)tile.get("value"));
                    } else if(tile.containsKey("letter")) {
                        t.setCorrectValue((String)tile.get("letter"));
                    }
                    b.setTile(t.getRow() % b.getSize(), t.getCol() % b.getSize(), t); //mod right now is because I messed up my outputting
                }
            }

            board = b;
        } catch(ParseException e) {
            System.out.println("Board failed to load due to JSON parse fail!");
            e.printStackTrace();
        } catch(IOException e) {
            System.out.println("Board failed to load due to file read exception!");
            e.printStackTrace();
        } catch(NullPointerException e) {
            System.out.println("Board failed to load due to NullPointerException");
            e.printStackTrace();
        }
    }



    private static void iterLoadPuzzle() {
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
                                    case "marker":
                                        c.setMarker((String.valueOf(clueValues.next())));
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

    private static void scrapePuzzle() {
        scrapePuzzle("");
    }

    private static void scrapePuzzle(String date) {
        try {
            Process scrape = Runtime.getRuntime().exec("/Users/zackamiton/Code/Crosswordizer/scraper/scraper.py " + date);
            scrape.waitFor(); //Make sure file exists and main thread halts to avoid accessing the file until then
        } catch(IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        scrapePuzzle();
        condLoadPuzzle(); //Load board
        for(int i = 0; i < board.getSize()*board.getSize(); i++) {
            System.out.println(board.getTile(i).getCorrectValue());
        }

//        PApplet.runSketch(new String[]{"Crossword"}, app);
    }
}
