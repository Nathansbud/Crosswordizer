import processing.core.PApplet;

import java.util.ArrayList;

public class Board {
    public enum HighlightMode {
        WING(),
        SINGLE(),
        NONE(),
    }

    public enum ShiftDirection {
        UP(),
        DOWN(),
        LEFT(),
        RIGHT()
    }

    private static PApplet gui;

    private int size;

    private String date;
    private String weekday;
    private String title;

    private Tile[][] tiles;

    private ArrayList<Clue> downClues = new ArrayList<Clue>();
    private ArrayList<Clue> acrossClues = new ArrayList<Clue>(); //Should these be the same arraylist?

    private HighlightMode highlightMode;

    private boolean highlightAcross;
    private boolean highlightDown;

    private boolean rebus = false;

//    private ShiftDirection lastMove; //Wings variable to check direction that shifts should move

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
        setHighlightMode(HighlightMode.SINGLE);
    }

    public void draw() {
        for (Tile[] tls : tiles) {
            for (Tile t : tls) {
                t.draw();
            }
        }
        if (Tile.hasSelected()) {
            Tile.getSelected().draw();
        }
        gui.fill(0);
        gui.fill(16);

        if(hasTitle()) gui.text(title, (size)*Tile.getSideLength()/2.0f - 0.5f*gui.textWidth(title), (size+1)*Tile.getSideLength());
        gui.text(weekday + ", " + date, (size)*Tile.getSideLength()/2.0f - 0.5f*gui.textWidth(weekday + ", " + date), (size+1.5f)*Tile.getSideLength());

        gui.textSize(12);

        int count = 1;
        for (Clue c : acrossClues) {
            if(Tile.hasAcrossRoot()) {
                if(c.getMarker().equals(Tile.getAcrossRoot().getMarker())) {
                    gui.fill(PApplet.unhex(CConstants.ACROSS_COLOR));
                    gui.text(c.getMarker() + ") " + c.getQuestion(), Tile.getSideLength(), Tile.getSideLength() * (size + 2.5f));
                } else {
                    gui.fill(0);
                }
            }
            gui.text(c.getMarker() + ") " + c.getQuestion(), Tile.getSideLength() * (size + 2), gui.height / 75.0f * count, Tile.getSideLength() * 12, gui.height / 37.5f);
            count++;
        }

        gui.fill(0);

        count = 1;

        for (Clue c : downClues) {
            if(Tile.hasDownRoot()) {
                if(c.getMarker().equals(Tile.getDownRoot().getMarker())) {
                    gui.fill(PApplet.unhex(CConstants.DOWN_COLOR));
                    gui.text(c.getMarker() + ") " + c.getQuestion(), Tile.getSideLength(), Tile.getSideLength() * (size + 3));
                } else {
                    gui.fill(0);
                }
            }
            gui.text(c.getMarker() + ") " + c.getQuestion(), Tile.getSideLength() * (size + 2) + Tile.getSideLength() * 13, gui.height / 75.0f * count, Tile.getSideLength() * 12, gui.height / 37.5f);
            count++;
        }

        gui.fill(255, 0, 0);
        gui.text("Across", Tile.getSideLength() * (size + 2), 1, Tile.getSideLength() * 12, gui.height/37.5f);
        gui.text("Down", Tile.getSideLength() * (size + 2) + Tile.getSideLength() * 13, 1, Tile.getSideLength() * 12, gui.height/37.5f);
    }

    public void swapHighlights(ShiftDirection d) {
        switch(d) {
            case UP:
            case DOWN:
                if(highlightAcross) {
                    highlightAcross = false;
                    highlightDown = true;
                }
                break;
            case LEFT:
            case RIGHT:
                if(highlightDown) {
                    highlightAcross = true;
                    highlightDown = false;
                }
                break;
        }
        setCrosses();
    }

    public void shiftDirection(ShiftDirection d, boolean stopAtWalls) {
        int currentIndex;
        int changeIndex;

        switch(d) {
            case LEFT:
                if(!stopAtWalls) {
                    currentIndex = (Tile.getSelected().getCol() == 0) ? size : Tile.getSelected().getCol();
                    changeIndex = currentIndex;

                    while (currentIndex > 0) {
                        if (!getTile(Tile.getSelected().getRow(), currentIndex - 1).isWall()) {
                            changeIndex = currentIndex - 1;
                            break;
                        } else {
                            currentIndex -= 1;
                            if (currentIndex == 0) {
                                currentIndex = size;
                            }
                        }
                    }

                    Tile.setSelected(getTile(Tile.getSelected().getRow(), changeIndex));
                } else {
                    if(Tile.getSelected().getCol() != 0 && !getTile(Tile.getSelected().getRow(), Tile.getSelected().getCol() - 1).isWall()) {
                        Tile.setSelected(getTile(Tile.getSelected().getRow(), Tile.getSelected().getCol() - 1));
                    }
                }
                break;
            case RIGHT:
                if(!stopAtWalls) {
                    currentIndex = (Tile.getSelected().getCol() == size - 1) ? -1 : Tile.getSelected().getCol();

                    changeIndex = currentIndex;

                    while (currentIndex < size - 1) {
                        if (!getTile(Tile.getSelected().getRow(), currentIndex + 1).isWall()) {
                            changeIndex = currentIndex + 1;
                            break;
                        } else {
                            currentIndex += 1;
                            if (currentIndex == size - 1) {
                                currentIndex = -1;
                            }
                        }
                    }
                    Tile.setSelected(getTile(Tile.getSelected().getRow(), changeIndex));
                } else {
                    if(Tile.getSelected().getCol() != size - 1 && !getTile(Tile.getSelected().getRow(), Tile.getSelected().getCol() + 1).isWall()) {
                        Tile.setSelected(getTile(Tile.getSelected().getRow(), Tile.getSelected().getCol() + 1));
                    }
                }
                break;
            case UP:
                if(!stopAtWalls) {
                    currentIndex = (Tile.getSelected().getRow() == 0) ? size : Tile.getSelected().getRow();
                    changeIndex = currentIndex;

                    while (currentIndex > 0) {
                        if (!getTile(currentIndex - 1, Tile.getSelected().getCol()).isWall()) {
                            changeIndex = currentIndex - 1;
                            break;
                        } else {
                            currentIndex -= 1;
                            if (currentIndex == 0) {
                                currentIndex = size;
                            }
                        }
                    }
                    Tile.setSelected(getTile(changeIndex, Tile.getSelected().getCol()));
                } else {
                    if(Tile.getSelected().getRow() != 0 && !getTile(Tile.getSelected().getRow() - 1, Tile.getSelected().getCol()).isWall()) {
                        Tile.setSelected(getTile(Tile.getSelected().getRow() - 1, Tile.getSelected().getCol()));
                    }
                }
                break;
            case DOWN:
                if(!stopAtWalls) {
                    currentIndex = (Tile.getSelected().getRow() == size - 1) ? -1 : Tile.getSelected().getRow();
                    changeIndex = currentIndex;

                    while (currentIndex < size - 1) {
                        if (!getTile(currentIndex + 1, Tile.getSelected().getCol()).isWall()) {
                            changeIndex = currentIndex + 1;
                            break;
                        } else {
                            currentIndex += 1;
                            if (currentIndex == size - 1) {
                                currentIndex = -1;
                            }
                        }
                    }
                    Tile.setSelected(getTile(changeIndex, Tile.getSelected().getCol()));
                    break;
                } else {
                    if(Tile.getSelected().getRow() != size - 1 && !getTile(Tile.getSelected().getRow() + 1, Tile.getSelected().getCol()).isWall()) {
                        Tile.setSelected(getTile(Tile.getSelected().getRow() + 1, Tile.getSelected().getCol()));
                    }
                }
        }
        setCrosses();
    }

    public void setCrosses() {
        for(int i = 0; i < size*size; i++) {
            getTile(i).setHorizontal(false);
            getTile(i).setVertical(false);
        }
        Tile.setDownRoot(null);
        Tile.setAcrossRoot(null);

        if(Tile.hasSelected() && highlightMode != HighlightMode.NONE) {
            int across = Tile.getSelected().getCol();
            int down = Tile.getSelected().getRow();

            Tile acrossRoot = null;
            Tile downRoot = null;

            if(highlightAcross) {
                for (int i = across - 1; i >= 0; i--) {
                    if (getTile(down, i).isWall()) {
                        acrossRoot = getTile(down, i + 1);
                        break;
                    } else {
                        getTile(down, i).setHorizontal(true);
                    }
                }

                for (int i = across + 1; i < size; i++) {
                    if (getTile(down, i).isWall()) {
                        break;
                    } else {
                        getTile(down, i).setHorizontal(true);
                    }
                }

                if (acrossRoot != null) {
                    Tile.setAcrossRoot(acrossRoot);
                } else {
                    Tile.setAcrossRoot(getTile(down, 0));
                }

            }

            if(highlightDown) {
                for (int i = down - 1; i >= 0; i--) {
                    if (getTile(i, across).isWall()) {
                        downRoot = getTile(i + 1, across);
                        break;
                    } else {
                        getTile(i, across).setVertical(true);
                    }
                }

                for (int i = down + 1; i < size; i++) {
                    if (getTile(i, across).isWall()) {
                        break;
                    } else {
                        getTile(i, across).setVertical(true);
                    }
                }

                if (downRoot != null) {
                    Tile.setDownRoot(downRoot);
                } else {
                    Tile.setDownRoot(getTile(0, across));
                }
            }
        }
    }

    public String getDate() {
        return date;
    }
    public boolean hasDate() {
        return date != null && date.length() > 0;
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

    public String getTitle() {
        return title;
    }
    public boolean hasTitle() {
        return title != null && date.length() > 0;
    }
    public void setTitle(String _title) {
        title = _title;
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

    public boolean hasRebus() {
        return rebus;
    }
    public void setRebus(boolean _rebus) {
        rebus = _rebus;
    }

    public HighlightMode getHighlightMode() {
        return highlightMode;
    }
    public void setHighlightMode(HighlightMode _highlightMode) {
        highlightMode = _highlightMode;
        switch(_highlightMode) {
            case NONE:
                highlightDown = false;
                highlightAcross = false;
                break;
            case SINGLE:
                highlightAcross = true;
                highlightDown = false;
                break;
            case WING:
                highlightAcross = true;
                highlightDown = true;
                break;
        }
    }

    public boolean isHighlightAcross() {
        return highlightAcross;
    }
    public void setHighlightAcross(boolean _highlightAcross) {
        highlightAcross = _highlightAcross;
    }

    public boolean isHighlightDown() {
        return highlightDown;
    }

    public void setHighlightDown(boolean _highlightDown) {
        highlightDown = _highlightDown;
    }

    public boolean isRebus() {
        return rebus;
    }
}
