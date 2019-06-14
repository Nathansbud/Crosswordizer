import java.lang.reflect.Array;
import java.util.ArrayList;

public class Board {
    private int size;

    private String date;
    private String weekday;

    private Tile[][] tiles;

    private ArrayList<Clue> downClues = new ArrayList<Clue>();
    private ArrayList<Clue> acrossClues = new ArrayList<Clue>(); //Should these be the same arraylist?

    public Board() {

    }

    public String getDate() {
        return date;
    }
    public void setDate(String _date) {
        date = _date;
    }

    public String getWeekday() {
        return weekday;
    }
    public void setWeekday(String _weekday) {
        weekday = _weekday;
    }

    public int getSize() {
        return size;
    }
    public void setSize(int _size) {
        size = _size;
    }

    public ArrayList<Clue> getDownClues() {
        return downClues;
    }
    public Clue getDownClue(int index) {
        return downClues.get(index);
    }
    public void addDownClue(Clue downClue) {
        downClues.add(downClue);
    }
    public void setDownClues(ArrayList<Clue> _downClues) {
        downClues = _downClues;
    }

    public ArrayList<Clue> getAcrossClues() {
        return acrossClues;
    }
    public Clue getAcrossClue(int index) {
        return acrossClues.get(index);
    }
    public void addAcrossClue(Clue acrossClue) {
        acrossClues.add(acrossClue);
    }
    public void setAcrossClues(ArrayList<Clue> _acrossClues) {
        acrossClues = _acrossClues;
    }
}
