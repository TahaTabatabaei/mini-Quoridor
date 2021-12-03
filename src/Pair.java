public class Pair {
    double value;
    String move;

    public Pair(){}

    public Pair(double value) {
        this.value = value;
    }

    public Pair(double value, String move) {
        this.value = value;
        this.move = move;
    }

    public Pair(String move) {
        this.move = move;
    }
}
