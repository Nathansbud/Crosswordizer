public class Tile {
    public static Tile selected = null;

    private int clue;
    private boolean special = false; //Handle weird crossword puzzle tiles ("oo") for themed puzzles

    private String correctValue;
    private String currentValue;

    public Tile() {

    }
}
