package pij.main;

import pij.ai.PlayerController;
import pij.ai.SimpleComputer;
import pij.board.Board;
import pij.board.BoardLoader;
import pij.board.InvalidBoardFileException;
import pij.dict.WordList;
import pij.game.IllegalMoveException;
import pij.game.MoveApplier;
import pij.game.MoveValidator;
import pij.game.ValidatedMove;
import pij.play.BoardPrinter;
import pij.play.PlayerState;
import pij.move.Move;
import pij.move.MoveFormatException;
import pij.move.MoveParser;
import pij.score.ScoreBreakdown;
import pij.score.Scorer;
import pij.tiles.TileBag;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        new Main().run();
    }

    private final Scanner in = new Scanner(System.in);
    private final BoardPrinter printer = new BoardPrinter();

    private final MoveParser moveParser = new MoveParser();
    private final MoveValidator validator = new MoveValidator();
    private final MoveApplier applier = new MoveApplier();
    private final Scorer scorer = new Scorer();

    private void run() throws IOException {
        System.out.println("============                     ============");
        System.out.println("============ S c r a e B B K l e ============");
        System.out.println("============                     ============");

        Board board = chooseBoard();
        boolean openGame = chooseOpenClosed();

        WordList dict = WordList.fromResource("/wordlist.txt");

        // Create player states
        PlayerState p1 = new PlayerState("Player 1");
        PlayerState p2 = new PlayerState("Player 2");

        // Choose controllers (Human vs Computer)
        PlayerController p1Controller = chooseControllerFor("Player 1", dict);
        PlayerController p2Controller = chooseControllerFor("Player 2", dict);

        TileBag bag = new TileBag(TileBag.defaultTiles());

        // initial racks: draw up to 7
        bag.refillRack(p1.rack(), 7);
        bag.refillRack(p2.rack(), 7);

        boolean firstMove = true;
        int consecutivePasses = 0;

        PlayerState current = p1;
        PlayerState other = p2;

        PlayerController currentController = p1Controller;
        PlayerController otherController = p2Controller;

        while (true) {
            System.out.println();
            System.out.println(p1.name() + ": " + p1.score() + "    " + p2.name() + ": " + p2.score());
            printer.print(board);

            if (openGame) {
                System.out.println("OPEN GAME: " + other.name() + "'s tiles:");
                System.out.println(rackToString(other));
            }

            System.out.println("It's your turn, " + current.name() + "! Your tiles:");
            System.out.println(rackToString(current));

            Move move = getMoveFromController(board, current, currentController, firstMove);

            if (move.isPass()) {
                consecutivePasses++;
                System.out.println(current.name() + " passes.");
            } else {
                try {
                    ValidatedMove vm = validator.validate(board, current.rack(), move, firstMove, dict);

                    applier.apply(board, current.rack(), vm);
                    ScoreBreakdown sb = scorer.scoreMove(board, vm);

                    current.addScore(sb.total());
                    System.out.println(current.name() + " plays " + vm.mainWord() + " for " + sb.total() + " points.");

                    bag.refillRack(current.rack(), 7);

                    firstMove = false;
                    consecutivePasses = 0;
                } catch (IllegalMoveException e) {
                    System.out.println("Illegal move");
                    // Human: retry; Computer: pass to avoid infinite loops
                    if (isHuman(currentController)) {
                        continue;
                    } else {
                        consecutivePasses++;
                        System.out.println(current.name() + " falls back to pass.");
                    }
                }
            }

            if (isGameOver(bag, p1, p2, consecutivePasses)) {
                break;
            }

            // swap players and controllers
            PlayerState tmp = current;
            current = other;
            other = tmp;

            PlayerController tmpC = currentController;
            currentController = otherController;
            otherController = tmpC;
        }

        // final scoring (end-game penalty)
        int p1Penalty = p1.rack().totalValue();
        int p2Penalty = p2.rack().totalValue();
        p1.addPenalty(p1Penalty);
        p2.addPenalty(p2Penalty);

        System.out.println();
        System.out.println("Game over.");
        System.out.println(p1.name() + " penalty: -" + p1Penalty);
        System.out.println(p2.name() + " penalty: -" + p2Penalty);
        System.out.println("Final scores:");
        System.out.println(p1.name() + ": " + p1.score());
        System.out.println(p2.name() + ": " + p2.score());

        if (p1.score() > p2.score()) System.out.println(p1.name() + " wins!");
        else if (p2.score() > p1.score()) System.out.println(p2.name() + " wins!");
        else System.out.println("Draw!");
    }

    private PlayerController chooseControllerFor(String playerName, WordList dict) {
        while (true) {
            System.out.print(playerName + " is Human (h) or Computer (c)? ");
            String s = in.nextLine().trim().toLowerCase();
            if (s.equals("h")) {
                return new HumanController();
            }
            if (s.equals("c")) {
                return new SimpleComputer(validator, dict);
            }
            System.out.println("Please enter h or c.");
        }
    }

    private Move getMoveFromController(Board board, PlayerState player, PlayerController controller, boolean firstMove) {
        if (isHuman(controller)) {
            return readMove(board);
        } else {
            Move m = controller.chooseMove(board, player.rack(), firstMove);
            System.out.println(player.name() + " chooses: " + moveToString(m));
            return m;
        }
    }

    private boolean isHuman(PlayerController controller) {
        return controller instanceof HumanController;
    }

    /**
     * Human controller is just a marker; the actual input is handled by Main.readMove().
     */
    private static final class HumanController implements PlayerController {
        @Override
        public Move chooseMove(Board board, pij.tiles.Rack rack, boolean firstMove) {
            throw new UnsupportedOperationException("Human moves are read from console input");
        }
    }

    private Board chooseBoard() throws IOException {
        while (true) {
            System.out.print("Would you like to load a board (l) or use the default board (d) ?");
            String s = in.nextLine().trim().toLowerCase();
            if (s.equals("d")) return defaultBoard15x15();
            if (s.equals("l")) {
                System.out.print("Enter board file path: ");
                String path = in.nextLine().trim();
                try {
                    return new BoardLoader().load(Path.of(path));
                } catch (InvalidBoardFileException e) {
                    System.out.println("Invalid board file");
                }
                continue;
            }
            System.out.println("Please enter d or l.");
        }
    }

    private Board defaultBoard15x15() {
        int m = 15, n = 15;
        pij.board.Cell[][] cells = new pij.board.Cell[n][m];
        for (int r = 0; r < n; r++) for (int c = 0; c < m; c++) cells[r][c] = pij.board.Cell.normal();
        return new Board(m, n, new pij.board.Square(7, 7), cells); // h8
    }

    private boolean chooseOpenClosed() {
        while (true) {
            System.out.print("Open game (o) or closed game (c)? ");
            String s = in.nextLine().trim().toLowerCase();
            if (s.equals("o")) return true;
            if (s.equals("c")) return false;
            System.out.println("Please enter o or c.");
        }
    }

    private Move readMove(Board board) {
        while (true) {
            System.out.print("Enter move (word,square) or ',' to pass: ");
            String line = in.nextLine();
            try {
                return moveParser.parse(line, board.cols(), board.rows());
            } catch (MoveFormatException e) {
                System.out.println("Illegal move format");
            }
        }
    }

    private boolean isGameOver(TileBag bag, PlayerState p1, PlayerState p2, int consecutivePasses) {
        if (bag.isEmpty() && (p1.rack().isEmpty() || p2.rack().isEmpty())) return true;
        return consecutivePasses >= 4;
    }

    private String rackToString(PlayerState p) {
        StringBuilder sb = new StringBuilder();
        for (pij.tiles.Tile t : p.rack().tilesView()) {
            if (t.isWildcard()) sb.append("[_8]");
            else sb.append(t.letter());
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    private String moveToString(Move m) {
        if (m.isPass()) return ",";
        return m.wordRaw() + "," + m.start().toString() + " (" + m.direction() + ")";
    }
}