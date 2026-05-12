package game;

public class TicTacToeTestRunner {

    public static void main(String[] args) {
        TicTacToeTestRunner runner = new TicTacToeTestRunner();

        runner.testInitialState();
        runner.testValidMove();
        runner.testInvalidMove();
        runner.testRowWinner();
        runner.testColumnWinner();
        runner.testDiagonalWinner();
        runner.testDraw();
        runner.testGameStillRunning();
        runner.testCannotMoveAfterGameOver();
        runner.testReset();

        System.out.println("\n✅ ALL TESTS COMPLETED");
    }

    private void assertTrue(boolean condition, String message) {
        if (!condition) throw new RuntimeException("❌ FAIL: " + message);
        System.out.println("✔ PASS: " + message);
    }

    private void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            System.out.println("✔ PASS: " + message);
            return;
        }
        if (expected != null && expected.equals(actual)) {
            System.out.println("✔ PASS: " + message);
        } else {
            throw new RuntimeException("❌ FAIL: " + message +
                    " | expected=" + expected + ", actual=" + actual);
        }
    }

    // ---------------- TESTS ----------------

    void testInitialState() {
        System.out.println("\nRunning testInitialState");

        TicTacToe game = new TicTacToe();

        assertEquals(TicTacToe.Player.X, game.getCurrentPlayer(), "Initial player is X");
        assertFalse(game.isGameOver(), "Game is not over initially");

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                assertEquals(TicTacToe.Player.NONE, game.getCell(r, c),
                        "Cell should be empty at (" + r + "," + c + ")");
            }
        }
    }

    void testValidMove() {
        System.out.println("\nRunning testValidMove");

        TicTacToe game = new TicTacToe();

        assertTrue(game.makeMove(0, 0), "First move is valid");
        assertEquals(TicTacToe.Player.X, game.getCell(0, 0), "Cell updated with X");
        assertEquals(TicTacToe.Player.O, game.getCurrentPlayer(), "Turn switches to O");
    }

    void testInvalidMove() {
        System.out.println("\nRunning testInvalidMove");

        TicTacToe game = new TicTacToe();

        game.makeMove(0, 0);
        assertFalse(game.makeMove(0, 0), "Cannot move on occupied cell");
    }

    void testRowWinner() {
        System.out.println("\nRunning testRowWinner");

        TicTacToe game = new TicTacToe();

        game.makeMove(0, 0); // X
        game.makeMove(1, 0); // O
        game.makeMove(0, 1); // X
        game.makeMove(1, 1); // O
        game.makeMove(0, 2); // X

        assertEquals(TicTacToe.Player.X, game.checkWinner(), "X wins by row");
        assertTrue(game.isGameOver(), "Game is over");
    }

    void testColumnWinner() {
        System.out.println("\nRunning testColumnWinner");

        TicTacToe game = new TicTacToe();

        game.makeMove(0, 0);
        game.makeMove(0, 1);
        game.makeMove(1, 0);
        game.makeMove(1, 1);
        game.makeMove(2, 0);

        assertEquals(TicTacToe.Player.X, game.checkWinner(), "X wins by column");
        assertTrue(game.isGameOver(), "Game is over");
    }

    void testDiagonalWinner() {
        System.out.println("\nRunning testDiagonalWinner");

        TicTacToe game = new TicTacToe();

        game.makeMove(0, 0);
        game.makeMove(0, 1);
        game.makeMove(1, 1);
        game.makeMove(0, 2);
        game.makeMove(2, 2);

        assertEquals(TicTacToe.Player.X, game.checkWinner(), "X wins diagonally");
        assertTrue(game.isGameOver(), "Game is over");
    }

    void testDraw() {
        System.out.println("\nRunning testDraw");

        TicTacToe game = new TicTacToe();

        game.makeMove(0, 0);
        game.makeMove(0, 1);
        game.makeMove(0, 2);
        game.makeMove(1, 1);
        game.makeMove(1, 0);
        game.makeMove(1, 2);
        game.makeMove(2, 1);
        game.makeMove(2, 0);
        game.makeMove(2, 2);

        assertEquals(TicTacToe.Player.NONE, game.checkWinner(), "Game is draw");
        assertTrue(game.isGameOver(), "Game is over");
    }

    void testGameStillRunning() {
        System.out.println("\nRunning testGameStillRunning");

        TicTacToe game = new TicTacToe();

        game.makeMove(0, 0);
        game.makeMove(1, 1);

        assertEquals(null, game.checkWinner(), "No winner yet");
        assertFalse(game.isGameOver(), "Game still running");
    }

    void testCannotMoveAfterGameOver() {
        System.out.println("\nRunning testCannotMoveAfterGameOver");

        TicTacToe game = new TicTacToe();

        game.makeMove(0, 0);
        game.makeMove(1, 0);
        game.makeMove(0, 1);
        game.makeMove(1, 1);
        game.makeMove(0, 2);

        game.checkWinner();

        assertFalse(game.makeMove(2, 2), "Cannot move after game over");
    }

    void testReset() {
        System.out.println("\nRunning testReset");

        TicTacToe game = new TicTacToe();

        game.makeMove(0, 0);
        game.reset();

        assertEquals(TicTacToe.Player.X, game.getCurrentPlayer(), "Reset sets player to X");
        assertFalse(game.isGameOver(), "Game not over after reset");

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                assertEquals(TicTacToe.Player.NONE, game.getCell(r, c),
                        "Board cleared at (" + r + "," + c + ")");
            }
        }
    }
}