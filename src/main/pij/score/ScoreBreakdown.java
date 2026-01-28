package pij.score;

public record ScoreBreakdown(int baseBeforeWordMultiplier,
                             int wordMultiplier,
                             int bingoBonus,
                             int total) {}
