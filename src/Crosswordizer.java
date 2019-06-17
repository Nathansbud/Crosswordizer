import processing.core.PApplet;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.io.IOException;
import java.io.FileReader;
import java.io.File;

public class Crosswordizer extends PApplet {
    private static Crosswordizer app = new Crosswordizer();
    private static Board board;
    private static Scraper scraper = new Scraper();


    @Override
    public void settings() {
        fullScreen();
    }
    @Override
    public void setup() {
        Board.setStatics();
        Tile.setStatics();

        board.setHighlightMode(Board.HighlightMode.SINGLE);
        board.setup();
    }
    @Override
    public void draw() {
        background(PApplet.unhex(CColors.BACKGROUND_COLOR));
        board.draw();
    }

    @Override
    public void keyPressed() {
        System.out.println(keyCode);
        if(Tile.hasSelected()) {
            switch (keyCode) { //Backspace
                case 8:
                    Tile.getSelected().setCurrentValue("");
                    if(board.getHighlightMode() == Board.HighlightMode.SINGLE) {
                        if(board.isHighlightAcross()) {
                            board.shiftDirection(Board.ShiftDirection.LEFT, true);
                        } else if(board.isHighlightDown()) {
                            board.shiftDirection(Board.ShiftDirection.UP, true);
                        }
                    }
                    break;
                case 16:
                    Tile.getSelected().setGuess(!Tile.getSelected().isGuess());
                    break;
                case 17:
                    Tile.getSelected().swapValues();
                    break;
                case 37: //Left Arrow Key
                    if(board.isHighlightAcross()) {
                        board.shiftDirection(Board.ShiftDirection.LEFT, false);
                    } else if(board.getHighlightMode() == Board.HighlightMode.SINGLE) {
                        board.swapHighlights(Board.ShiftDirection.LEFT);
                    }
                    break;
                case 38: //Up Arrow Key
                    if(board.isHighlightDown()) {
                        board.shiftDirection(Board.ShiftDirection.UP, false);
                    } else if(board.getHighlightMode() == Board.HighlightMode.SINGLE){
                        board.swapHighlights(Board.ShiftDirection.UP);
                    }
                    break;
                case 39: //Right Arrow Key
                    if(board.isHighlightAcross()) {
                        board.shiftDirection(Board.ShiftDirection.RIGHT, false);
                    } else if(board.getHighlightMode() == Board.HighlightMode.SINGLE){
                        board.swapHighlights(Board.ShiftDirection.RIGHT);
                    }
                    break;
                case 40: //Down Arrow Key
                    if(board.isHighlightDown()) {
                        board.shiftDirection(Board.ShiftDirection.DOWN, false);
                    } else if(board.getHighlightMode() == Board.HighlightMode.SINGLE) {
                        board.swapHighlights(Board.ShiftDirection.DOWN);
                    }
                    break;
                default:
                    if(Character.getType(keyCode) != 15) {
                        if(board.hasRebus()) {
                            Tile.getSelected().setCurrentValue(Tile.getSelected().getCurrentValue() + Character.toUpperCase(key));
                        } else {
                            Tile.getSelected().setCurrentValue(String.valueOf(Character.toUpperCase(key)));
                        }
                    }

                    if(Character.getType(keyCode) != 15 || keyCode == 9 || keyCode == 10) {
                        if(board.getHighlightMode() == Board.HighlightMode.SINGLE) {
                            if(board.isHighlightAcross()) {
                                board.shiftDirection(Board.ShiftDirection.RIGHT, true);
                            } else if(board.isHighlightDown()) {
                                board.shiftDirection(Board.ShiftDirection.DOWN, true);
                            }
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void mouseClicked() {
        for(int i = 0; i < board.getSize()*board.getSize(); i++) {
            if(!board.getTile(i).isWall() && mouseX > board.getTile(i).getX() && mouseX < board.getTile(i).getX() + Tile.getSideLength() && mouseY > board.getTile(i).getY() && mouseY < board.getTile(i).getY() + Tile.getSideLength()) {
                Tile.setSelected(board.getTile(i));
                board.setCrosses();
            }
        }
    }

    private static Object checkedGet(JSONObject jsonObj, String key) {
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
            b.setTitle((String)checkedGet(puzzleData, "title"));

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
                        if (tile.containsKey("value")) t.setCorrectValue((String) tile.get("value"));
                        if (tile.containsKey("rebus")) {
                            t.setRebus(true);
                            b.setRebus(true);
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
    public static Crosswordizer getApp() {
        return app;
    }

    public static void main(String[] args) {
//        scraper.scrapeWashingtonPost("6/11/2019");
        board = scraper.scrapePuzzle();
        PApplet.runSketch(new String[]{"Crossword"}, app);
    }
}
