import processing.core.PApplet;

import java.util.ArrayList;

public class Board {
    private static PApplet gui;

    private int size;

    private String date;
    private String weekday;

    private Tile[][] tiles;

    private ArrayList<Clue> downClues = new ArrayList<Clue>();
    private ArrayList<Clue> acrossClues = new ArrayList<Clue>(); //Should these be the same arraylist?

    public Board() {

    }

    public static void setStatics() {
        setGui(Crosswordizer.getApp());
    }

    public void setup() {
        for(Tile[] tls : tiles) {
            for(Tile t : tls) {
                t.setup();
            }
        }

    }

    public void draw() {
        for(Tile[] tls : tiles) {
            for(Tile t : tls) {
                t.draw();
            }
        }
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
        tiles = new Tile[size][size];
    }

    public Tile[][] getTiles() {
        return tiles;
    }
    public void setTiles(Tile[][] _tiles) {
        tiles = _tiles;
    }

    public Tile getTile(int x, int y) {
        return tiles[x][y];
    }
    public void setTile(int x, int y, Tile t) {
        tiles[x][y] = t;
    }

    public Tile getTile(int index) {
        return tiles[index / size][index % size];
    }
    public void setTile(int index, Tile t) {
        tiles[index / size][index % size] = t;
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

    public static PApplet getGui() {
        return gui;
    }
    public static void setGui(PApplet _gui) {
        gui = _gui;
    }
}
