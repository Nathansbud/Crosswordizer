import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileReader;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;

public class Scraper {
    public static boolean isBeforeShortz(String date) {
        String[] s = date.split("/");
        Calendar checkDate = Calendar.getInstance();

        checkDate.set(Calendar.MONTH, Integer.parseInt(s[0]));
        checkDate.set(Calendar.DATE, Integer.parseInt(s[1]));
        checkDate.set(Calendar.YEAR, Integer.parseInt(s[2]));

        Calendar shortzDate = Calendar.getInstance();
        shortzDate.set(Calendar.MONTH, 11);
        shortzDate.set(Calendar.DATE, 21);
        shortzDate.set(Calendar.YEAR, 1993);

        return checkDate.before(shortzDate);
    }

    public static Board scrapePuzzle() { //Use today's date if no date specified
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date date = new Date();
        String todayDate = sdf.format(date);

        return scrapePuzzle((todayDate.startsWith("0")) ? todayDate.substring(1) : todayDate);
    }
    public static Board scrapePuzzle(String date) {
        Board b = new Board();

        try {
            Document doc = Jsoup.connect((isBeforeShortz(date) ? ("http://xwordinfo.com/PS?date=") : ("http://xwordinfo.com/Crossword?date="))+date).get();
            Element grid = doc.getElementById("PuzTable");

            b.setSize(grid.child(0).children().size());

            int column = 0;
            int row = 0;

            for (Element gridRow : grid.child(0).children()) {
                column = 0;
                for (int i = 0; i < gridRow.children().size(); i++) {
                    Element currentCell = gridRow.children().get(i);

                    if (currentCell.hasClass("black")) {
                        b.setTile(row, column, new Tile(row, column, true));
                    } else {
                        Elements cellInformation = currentCell.children();

                        Tile t = new Tile(row, column, false);
                        if (currentCell.hasClass("bigcircle")) t.setCircled(true);
                        if (currentCell.hasClass("bigshade")) t.setShaded(true);

                        for (Element e : cellInformation) {
                            if (e.hasClass("num")) t.setMarker(e.text());
                            if (e.hasClass("letter")) {
                                t.setCorrectValue(e.text());
                            } else if (e.hasClass("subst")) {
                                t.setCorrectValue(e.text());
                                t.setRebus(true);
                                b.setRebus(true);
                            }
                        }

                        b.setTile(row, column, t);
                    }
                    column++;
                }
                row++;
            }

            Elements clues = doc.getElementsByClass("numclue");

            String clueMarker = "";
            String[] clueBody;

            for (int i = 0; i < clues.size(); i++) {
                for (int j = 0; j < clues.get(i).children().size(); j++) {
                    if (j % 2 == 0) {
                        clueMarker = clues.get(i).children().get(j).text();
                    } else {
                        clueBody = clues.get(i).children().get(j).text().split(" : ");
                        Clue c = new Clue(clueMarker, clueBody[0], clueBody[1], (i % 2 == 0) ? (Clue.Direction.ACROSS) : (Clue.Direction.DOWN));
                        if (i % 2 == 0) {
                            b.addAcrossClue(c);
                        } else {
                            b.addDownClue(c);
                        }
                    }
                }
            }


            String pageTitle = doc.getElementById("PuzTitle").text();
            Element pageSubtitle = doc.getElementById("CPHContent_SubTitle");

            String[] puzzleDate;

            if (pageSubtitle != null) {
                b.setTitle(pageTitle);
                puzzleDate = pageSubtitle.text().split(", ");
            } else {
                b.setTitle(null);
                puzzleDate = pageTitle.split(", ");
            }
            b.setWeekday(puzzleDate[1]);
            b.setDate(Month.valueOf(puzzleDate[2].split(" ")[0].toUpperCase()).getValue() + "/" + puzzleDate[2].split(" ")[1] + "/" + puzzleDate[3]);
        } catch (IOException e) {
            b = null;
            e.printStackTrace();
            System.out.println("Board load failed!");
        }

        return b;
    }

    public static Object checkedGet(JSONObject jsonObj, String key) {
        return (jsonObj.containsKey(key)) ? (jsonObj.get(key)) : null;
    }
    public static Board loadPuzzleFromJSON(String path) {
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader f = new FileReader(path);
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

                    if(tile.containsKey("wall") || !(tile.containsKey("value") || tile.containsKey("letter"))){
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

            return b;
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
        return new Board();
    }
}
