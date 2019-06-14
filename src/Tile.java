public class Tile {
    public static Tile selected = null;

    private String marker;

    private int row;
    private int col;

    private boolean special = false; //Handle weird crossword puzzle tiles ("oo") for themed puzzles

    private String correctValue;
    private String currentValue;

    public Tile() {

    }

    public static Tile getSelected() {
        return selected;
    }
    public boolean isSelected() {
        return selected != null && selected.equals(this);
    }
    public static void setSelected(Tile _selected) {
        selected = _selected;
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
}
