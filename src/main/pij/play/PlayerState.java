package pij.play;

import pij.tiles.Rack;

public final class PlayerState {
    private final String name;
    private final Rack rack;
    private int score;

    public PlayerState(String name) {
        this.name = name;
        this.rack = new Rack();
        this.score = 0;
    }

    public String name() { return name; }
    public Rack rack() { return rack; }
    public int score() { return score; }

    public void addScore(int delta) { score += delta; }
    public void addPenalty(int delta) { score -= delta; } // delta should be positive
}
