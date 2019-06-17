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

        board.setup();
    }
    @Override
    public void draw() {
        background(PApplet.unhex(CConstants.BACKGROUND_COLOR));
        board.draw();
    }

    @Override
    public void keyPressed() {
        if(Tile.hasSelected()) {
            int currentIndex;
            int changeIndex;

            switch (keyCode) {
                case 8:
                    Tile.getSelected().setCurrentValue("");
                    break;
                case 37: //Left Arrow Key
                    currentIndex = (Tile.getSelected().getCol() == 0) ? board.getSize() : Tile.getSelected().getCol();
                    changeIndex = currentIndex;

                    while (currentIndex > 0) {
                        if (!board.getTile(Tile.getSelected().getRow(), currentIndex - 1).isWall()) {
                            changeIndex = currentIndex - 1;
                            break;
                        } else {
                            currentIndex -= 1;
                            if(currentIndex == 0) {
                                currentIndex = board.getSize();
                            }
                        }
                    }
                    Tile.setSelected(board.getTile(Tile.getSelected().getRow(), changeIndex));
                    board.setCrosses();
                    break;
                case 38: //Up Arrow Key
                    currentIndex = (Tile.getSelected().getRow() == 0) ? board.getSize() : Tile.getSelected().getRow();
                    changeIndex = currentIndex;

                    while (currentIndex > 0) {
                        if (!board.getTile(currentIndex - 1, Tile.getSelected().getCol()).isWall()) {
                            changeIndex = currentIndex - 1;
                            break;
                        } else {
                            currentIndex -= 1;
                            if(currentIndex == 0) {
                                currentIndex = board.getSize();
                            }
                        }
                    }
                    Tile.setSelected(board.getTile(changeIndex, Tile.getSelected().getCol()));
                    board.setCrosses();
                    break;
                case 9: //Tab
                case 39: //Right Arrow Key
                    currentIndex = (Tile.getSelected().getCol() == board.getSize() - 1) ? -1 : Tile.getSelected().getCol();
                    changeIndex = currentIndex;

                    while (currentIndex < board.getSize() - 1) {
                        if (!board.getTile(Tile.getSelected().getRow(), currentIndex + 1).isWall()) {
                            changeIndex = currentIndex + 1;
                            break;
                        } else {
                            currentIndex += 1;
                            if(currentIndex == board.getSize() - 1) {
                                currentIndex = -1;
                            }
                        }
                    }
                    Tile.setSelected(board.getTile(Tile.getSelected().getRow(), changeIndex));
                    board.setCrosses();
                    break;
                case 10: //Enter Key
                case 40: //Down Arrow Key
                    currentIndex = (Tile.getSelected().getRow() == board.getSize() - 1) ? -1 : Tile.getSelected().getRow();
                    changeIndex = currentIndex;

                    while (currentIndex < board.getSize() - 1) {
                        if (!board.getTile(currentIndex + 1, Tile.getSelected().getCol()).isWall()) {
                            changeIndex = currentIndex + 1;
                            break;
                        } else {
                            currentIndex += 1;
                            if(currentIndex == board.getSize() - 1) {
                                currentIndex = -1;
                            }
                        }
                    }
                    Tile.setSelected(board.getTile(changeIndex, Tile.getSelected().getCol()));
                    board.setCrosses();
                    break;
                default:
                    if(Character.getType(keyCode) != 15) {
                        if(board.hasRebus()) {
                            Tile.getSelected().setCurrentValue(Tile.getSelected().getCurrentValue() + Character.toUpperCase(key));
                        } else {
                            Tile.getSelected().setCurrentValue(String.valueOf(Character.toUpperCase(key)));
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
