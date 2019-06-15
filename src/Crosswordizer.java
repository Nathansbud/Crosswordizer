import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import processing.core.PApplet;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.io.IOException;
import java.io.FileReader;
import java.io.File;

public class Crosswordizer extends PApplet {
    private static Crosswordizer app = new Crosswordizer();
    private static Board board;

    @Override
    public void settings() {
        fullScreen();
    }
    @Override
    public void setup() {
        Board.setStatics();
        Tile.setStatics();

        board.setup();
    }

    @Override
    public void draw() {
        background(PApplet.unhex("fff2eecb"));
        board.draw();
    }

    public static Object checkedGet(JSONObject jsonObj, String key) {
        return (jsonObj.containsKey(key)) ? (jsonObj.get(key)) : null;
    }

    private static void loadPuzzle() {
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
                    if(tile.containsKey("wall")){
                        t.setWall(true);
                    } else {
                        if (tile.containsKey("marker")) t.setMarker((String.valueOf(tile.get("marker"))));
                        if (tile.containsKey("value")) {
                            t.setCorrectValue((String) tile.get("value"));
                            t.setRebus(true);
                        } else if (tile.containsKey("letter")) {
                            t.setCorrectValue((String) tile.get("letter"));
                        }
                        if (tile.containsKey("shaded")) t.setShaded(true);
                        if (tile.containsKey("circled")) t.setCircled(true);

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

    private static void scrapePuzzle() {
        scrapePuzzle("");
    }

    private static void scrapePuzzle(String date) { //date in M/D/Y, no 0s on single-digits
        try {
            Process scrape = Runtime.getRuntime().exec("/Users/zackamiton/Code/Crosswordizer/scraper/scraper.py " + date);
            scrape.waitFor(); //Make sure file exists and main thread halts to avoid accessing the file until then
        } catch(IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Crosswordizer getApp() {
        return app;
    }

    public static void main(String[] args) {
        scrapePuzzle("4/29/2019");
        loadPuzzle();

        PApplet.runSketch(new String[]{"Crossword"}, app);
    }
}
