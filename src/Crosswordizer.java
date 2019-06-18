import processing.core.PApplet;

public class Crosswordizer extends PApplet {
    private static Crosswordizer app = new Crosswordizer();
    private static Board board;

    private boolean shiftOnEnter = false;

    @Override
    public void settings() {
        fullScreen();
    }
    @Override
    public void setup() {
        Board.setStatics();
        Tile.setStatics();

        board.setup();
        board.setHighlightMode(Board.HighlightMode.WING);
    }
    @Override
    public void draw() {
        background(PApplet.unhex(CColors.BACKGROUND_COLOR));
        board.draw();
    }

    @Override
    public void keyPressed() {
        if(Tile.hasSelected()) {
            switch (keyCode) { //Backspace
                case 8:
                    Tile.getSelected().setCurrentValue("");
                    if(shiftOnEnter && board.getHighlightMode() == Board.HighlightMode.SINGLE) {
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

                    if(shiftOnEnter && ((Character.getType(keyCode) != 15 && !board.isRebus()) || keyCode == 9 || keyCode == 10)) {
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

    public static Crosswordizer getApp() {
        return app;
    }

    public static void main(String[] args) {
//        scraper.scrapeWashingtonPost("6/11/2019");
        board = Scraper.scrapePuzzle("6/08/2019");

//        board = Scraper.loadPuzzleFromJSON("/Users/zackamiton/Desktop/puzzle.json");
        PApplet.runSketch(new String[]{"Crossword"}, app);
    }
}
