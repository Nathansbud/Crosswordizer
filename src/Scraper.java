import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;



public class Scraper {
    public Scraper() {

    }


    public boolean isBeforeShortz(String date) {
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

    public Board scrapePuzzle() { //Use today's date if no date specified
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date date = new Date();
        String todayDate = sdf.format(date);

        return scrapePuzzle((todayDate.startsWith("0")) ? todayDate.substring(1) : todayDate);
    }
    public Board scrapePuzzle(String date) {
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

    public Board scrapeWashingtonPost(String date) {
        String[] dateParts = date.split("/");
        for(int i = 0; i < 2; i++) {
            if(Integer.parseInt(dateParts[i]) < 10) {
                dateParts[i] = "0" + dateParts[i];
            }
        }
        dateParts[2] = dateParts[2].substring(2);
        date = dateParts[2] + dateParts[0] + dateParts[1];

        if(Integer.parseInt(date) < 171130) {
            System.out.println("No archived puzzles before November 30th, 2017...");
            System.out.println("Setting puzzle to most recent: ");
            date = "171130";
        }

        try {
            WebClient client = new WebClient(); //Use HtmlUnit because page loading is dynamic, not static :(((
            client.setCssErrorHandler(new SilentCssErrorHandler());

            HtmlPage page = client.getPage("https://cdn1.amuselabs.com/wapo/crossword?id=tca" + date + "&set=wapo-daily");
//            (page.getElementById("footer-btn")).click();
//            client.waitForBackgroundJavaScript(1000);
//            (page.getElementById("answers-button")).click();
//            client.waitForBackgroundJavaScript(1000);
            ((HtmlButton)page.getByXPath("//button[@class='btn confirm-yes']").get(0)).click();
//            client.waitForBackgroundJavaScript(1000);

//            String cookie = "AL_PM_wapo-daily_tca" + date; //Uses to gauge if should load filled model
//            page.executeJavaScript("window.localStorage.setItem('" + cookie + "', ' ');");
//            page.refresh();
//            System.out.println(page.asText());
        } catch(IOException e) {
            System.out.println("Doc download failed!");
        }

        return new Board();
    }
}
