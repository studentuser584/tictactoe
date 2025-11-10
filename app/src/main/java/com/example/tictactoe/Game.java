package com.example.tictactoe;

import java.util.ArrayList;
import java.util.List;

public class Game {
    public static final int NO_WINNER = 0;
    public static final int PLAYER_X = 1;
    public static final int PLAYER_O = 2;
    public static final int DRAW = -1;

    private int[] board = new int[9];
    // Start with O by default
    private int currentTurn = PLAYER_O;

    public Game() { reset(); }

    public int[] getBoard() { return board; }
    public int getCurrentTurn() { return currentTurn; }

    public boolean play(int index) {
        if (index < 0 || index >= 9) return false;
        if (board[index] != NO_WINNER) return false;
        board[index] = currentTurn;
        currentTurn = (currentTurn == PLAYER_X) ? PLAYER_O : PLAYER_X;
        return true;
    }

    public void setAt(int index, int player) {
        if (index < 0 || index >= 9) return;
        board[index] = player;
    }

    public void reset() {
        for (int i = 0; i < 9; i++) board[i] = NO_WINNER;
        // ensure O starts after reset
        currentTurn = PLAYER_O;
    }

    public boolean isFull() {
        for (int v : board) if (v == NO_WINNER) return false;
        return true;
    }

    public int checkWinner() {
        int[][] lines = {
                {0,1,2},{3,4,5},{6,7,8},
                {0,3,6},{1,4,7},{2,5,8},
                {0,4,8},{2,4,6}
        };
        for (int[] l : lines) {
            if (board[l[0]] != NO_WINNER &&
                    board[l[0]] == board[l[1]] &&
                    board[l[1]] == board[l[2]]) {
                return board[l[0]];
            }
        }
        if (isFull()) return DRAW;
        return NO_WINNER;
    }

    public List<Integer> availableMoves() {
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < 9; i++) if (board[i] == NO_WINNER) res.add(i);
        return res;
    }

    public Game copy() {
        Game g = new Game();
        for (int i = 0; i < 9; i++) g.board[i] = this.board[i];
        g.currentTurn = this.currentTurn;
        return g;
    }
}
