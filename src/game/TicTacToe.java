package game;

public final class TicTacToe {

    public enum Player { X, O, NONE }

    private final Player[][] board = new Player[3][3];
    private Player currentPlayer  = Player.X;
    private boolean gameOver      = false;

    public TicTacToe() {
        reset();
    }

    public void reset() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                board[r][c] = Player.NONE;
        currentPlayer = Player.X;
        gameOver      = false;
    }

    // returns true if move was valid
    public boolean makeMove(int row, int col) {
        if (gameOver) return false;
        if (board[row][col] != Player.NONE) return false;

        board[row][col] = currentPlayer;
        currentPlayer   = (currentPlayer == Player.X) ? Player.O : Player.X;
        return true;
    }

    public Player getCell(int row, int col) { return board[row][col]; }
    public Player getCurrentPlayer()        { return currentPlayer; }
    public boolean isGameOver()             { return gameOver; }

    // call after every move to check result
    public Player checkWinner() {
        // check rows
        for (int r = 0; r < 3; r++)
            if (check(board[r][0], board[r][1], board[r][2]))
                { gameOver = true; return board[r][0]; }

        // check columns
        for (int c = 0; c < 3; c++)
            if (check(board[0][c], board[1][c], board[2][c]))
                { gameOver = true; return board[0][c]; }

        // check diagonals
        if (check(board[0][0], board[1][1], board[2][2]))
            { gameOver = true; return board[0][0]; }
        if (check(board[0][2], board[1][1], board[2][0]))
            { gameOver = true; return board[0][2]; }

        // check draw
        if (isBoardFull()) { gameOver = true; return Player.NONE; }

        return null; // game still going
    }

    private boolean check(Player a, Player b, Player c) {
        return a != Player.NONE && a == b && b == c;
    }

    private boolean isBoardFull() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (board[r][c] == Player.NONE) return false;
        return true;
    }
}