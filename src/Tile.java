import processing.core.PApplet;

public class Tile {
    public static PApplet gui;
    public static Tile selected = null;

    public static float sideLength;

    private String marker = "";
    private String correctValue = "";
    private String currentValue = "";

    private int row;
    private int col;

    public float x;
    public float y;

    private boolean special = false;

    private static int markerSize = 12;
    private static int valueSize = 15;

    private boolean wall;
    private boolean circled;
    private boolean shaded;
    private boolean rebus; //Handle weird crossword puzzle tiles ("oo") for themed puzzles

    private boolean horizontal;
    private boolean vertical;

    public Tile() {

    }

    public static void setStatics() {
        setGui(Crosswordizer.getApp());
        setSideLength(gui.width/48.0f);
    }

    public void setup() {
        x = col*sideLength;
        y = row*sideLength;
    }

    public void draw() {
        if(wall) {
            gui.fill(0);
        } else if(!shaded) {
            gui.fill(255); //normal
        } else {
            gui.fill(180); //shaded cells
        }

        if(!isSelected()) {
            gui.strokeWeight(1);
            gui.stroke(0);
        } else if(horizontal) {
            gui.strokeWeight(2);
            gui.stroke(255, 0, 0);
        } else if(vertical) {
            gui.strokeWeight(2);
            gui.stroke(0, 255, 0);
        } else {
            gui.strokeWeight(2);
            gui.stroke(0, 0, 255);
        }
        gui.rect(x, y, sideLength, sideLength);

        gui.strokeWeight(1);
        gui.stroke(0);


        if(circled) {
            gui.fill(PApplet.unhex("ffffcd20"));
            gui.ellipse((2*x + sideLength)/2.0f, (2*y + sideLength)/(2.0f) + 0.2f*valueSize, sideLength/1.5f, sideLength/1.5f);
        }

        if(hasMarker()) {
            gui.fill(0);
            gui.textSize(markerSize);
            gui.text(marker, x, y, x + sideLength, y + sideLength);
        }

        if(hasCurrentValue()) {
            if(isCorrect() && isRebus()) {
                gui.fill(255, 0, 0);
            } else {
                gui.fill(0);
            }

            gui.textSize(valueSize);
            gui.text(currentValue, (2*x + sideLength)/2.0f - 0.5f*gui.textWidth(currentValue), y + 0.3f*sideLength, x + sideLength, y + sideLength);
        }

        gui.textSize(12);
    }

    public static Tile getSelected() {
        return selected;
    }

    public static boolean hasSelected() {
        return selected != null;
    }
    public static boolean isSelected(Tile t) {
        return selected != null && selected.equals(t);
    }
    public boolean isSelected() {
        return selected != null && selected.equals(this);
    }

    public static void setSelected(Tile _selected) {
        selected = _selected;
    }

    public boolean hasCorrectValue() {
        return correctValue != null && correctValue.length() > 0;
    }
    public boolean hasMarker() {
        return marker != null && marker.length() > 0;
    }
    public boolean hasCurrentValue() {
        return currentValue != null && currentValue.length() > 0;
    }

    public boolean isCorrect() {
        return correctValue.equals(currentValue);
    }

    public String getMarker() {
        return marker;
    }
    public void setMarker(String _marker) {
        marker = _marker;
    }

    public int getRow() {
        return row;
    }
    public void setRow(int _row) {
        row = _row;
    }

    public int getCol() {
        return col;
    }
    public void setCol(int _col) {
        col = _col;
    }

    public boolean isSpecial() {
        return special;
    }
    public void setSpecial(boolean _special) {
        special = _special;
    }

    public String getCorrectValue() {
        return correctValue;
    }
    public void setCorrectValue(String _correctValue) {
        correctValue = _correctValue;
    }

    public String getCurrentValue() {
        return currentValue;
    }
    public void setCurrentValue(String _currentValue) {
        currentValue = _currentValue;
    }

    public static PApplet getGui() {
        return gui;
    }
    public static void setGui(PApplet _gui) {
        gui = _gui;
    }

    public static float getSideLength() {
        return sideLength;
    }
    public static void setSideLength(float _sideLength) {
        sideLength = _sideLength;
    }

    public boolean isCircled() {
        return circled;
    }
    public void setCircled(boolean _circled) {
        circled = _circled;
    }

    public boolean isShaded() {
        return shaded;
    }
    public void setShaded(boolean _shaded) {
        shaded = _shaded;
    }

    public boolean isWall() {
        return wall;
    }
    public void setWall(boolean _wall) {
        wall = _wall;
    }

    public boolean isRebus() {
        return rebus;
    }
    public void setRebus(boolean _rebus) {
        rebus = _rebus;
    }

    public float getX() {
        return x;
    }
    public void setX(float _x) {
        x = _x;
    }

    public float getY() {
        return y;
    }
    public void setY(float _y) {
        y = _y;
    }

    public static int getMarkerSize() {
        return markerSize;
    }
    public static void setMarkerSize(int _markerSize) {
        markerSize = _markerSize;
    }

    public static int getValueSize() {
        return valueSize;
    }
    public static void setValueSize(int _valueSize) {
        valueSize = _valueSize;
    }


}

