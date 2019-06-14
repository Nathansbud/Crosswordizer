public class Clue {
    public enum Direction {
        ACROSS(),
        DOWN()
    }

    private Direction direction;

    private String question;
    private String answer;
    private String marker;

    public Clue() {

    }
    public Clue(Direction _direction) {
        direction = _direction;
    }

    public String getMarker() {
        return marker;
    }
    public void setMarker(String _number) {
        marker = _number;
    }

    public String getQuestion() {
        return question;
    }
    public void setQuestion(String _question) {
        question = _question;
    }

    public String getAnswer() {
        return answer;
    }
    public void setAnswer(String _answer) {
        answer = _answer;
    }

    public Direction getDirection() {
        return direction;
    }
    public void setDirection(Direction _direction) {
        direction = _direction;
    }
}
