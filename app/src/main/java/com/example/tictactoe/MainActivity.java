package com.example.tictactoe;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private Button[] cells = new Button[9];
    private TextView status;
    private Game game;
    private SharedPreferences prefs;
    private static final String PREFS = "tictactoe_prefs";
    private static final String KEY_X_WINS = "x_wins";
    private static final String KEY_O_WINS = "o_wins";
    private static final String KEY_DRAWS = "draws";
    private static final String KEY_ROUND_OFFSET = "round_offset"; // new: global per-current-user offset stored in prefs

    private boolean gameOver = false;
    private boolean playerVsComputer = false;
    private Handler handler = new Handler();
    private Random random = new Random();

    private RadioGroup modeGroup;
    private RadioButton radioPvp;
    private RadioButton radioPvc;

    private GameDatabaseHelper dbHelper;
    private String currentUsername;

    private TextView scoreX, scoreO, scoreDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        status = findViewById(R.id.statusText);

        modeGroup = findViewById(R.id.modeGroup);
        radioPvp = findViewById(R.id.radioPvp);
        radioPvc = findViewById(R.id.radioPvc);

        for (int i = 0; i < 9; i++) {
            int id = getResources().getIdentifier("cell" + i, "id", getPackageName());
            cells[i] = findViewById(id);
            final int idx = i;
            cells[i].setOnClickListener(v -> onCellClicked(idx));
        }

        Button resetBtn = findViewById(R.id.resetButton);
        resetBtn.setOnClickListener(v -> resetBoard());

        Button clearScore = findViewById(R.id.clearScoreButton);

        scoreX = findViewById(R.id.scoreX);
        scoreO = findViewById(R.id.scoreO);
        scoreDraw = findViewById(R.id.scoreDraw);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        currentUsername = prefs.getString("pref_user", "admin");

        dbHelper = GameDatabaseHelper.getInstance(this);
        dbHelper.ensureOpen();

        game = new Game();
        gameOver = false;

        modeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            playerVsComputer = (checkedId == R.id.radioPvc);
            resetBoard();
        });

        playerVsComputer = radioPvc.isChecked();

        // Clear Score: keep DB history, but set an offset so new rounds start at 1
        clearScore.setOnClickListener(v -> {
            try {
                int maxRound = dbHelper.getMaxRound(currentUsername); // keep history
                prefs.edit().putInt(KEY_ROUND_OFFSET, maxRound).apply(); // set offset
            } catch (Exception e) {
                e.printStackTrace();
                prefs.edit().putInt(KEY_ROUND_OFFSET, 0).apply();
            }
            prefs.edit().putInt(KEY_X_WINS, 0).putInt(KEY_O_WINS, 0).putInt(KEY_DRAWS, 0).apply();
            updateScoreText();
        });

        updateUI();
        updateScoreText();

        // show starting status depending on Game reset (if Game starts with O)
        status.setText(game.getCurrentTurn() == Game.PLAYER_O ? "O's turn" : "X's turn");
    }

    private void onCellClicked(int index) {
        if (gameOver) return;
        if (playerVsComputer) {
            // human plays O; CPU plays X (we assume human is O)
            if (game.getCurrentTurn() == Game.PLAYER_X) return;
        }

        if (game.play(index)) {
            updateUI();
            int winner = game.checkWinner();
            if (winner != Game.NO_WINNER) {
                handleGameEnd(winner);
                return;
            } else {
                status.setText(game.getCurrentTurn() == Game.PLAYER_X ? "X's turn" : "O's turn");
            }

            if (playerVsComputer && game.getCurrentTurn() == Game.PLAYER_X) {
                handler.postDelayed(this::makeCpuMove, 350);
            }
        }
    }

    private void makeCpuMove() {
        if (gameOver) return;
        int cpuMove = pickCpuMove();
        if (cpuMove >= 0) {
            game.play(cpuMove);
            updateUI();
            int winner = game.checkWinner();
            if (winner != Game.NO_WINNER) {
                handleGameEnd(winner);
                return;
            } else {
                status.setText(game.getCurrentTurn() == Game.PLAYER_X ? "X's turn" : "O's turn");
            }
        }
    }

    private int pickCpuMove() {
        List<Integer> avail = game.availableMoves();
        if (avail.isEmpty()) return -1;

        // CPU is X in PvC
        for (int m : avail) {
            Game copy = game.copy();
            copy.setAt(m, Game.PLAYER_X);
            if (copy.checkWinner() == Game.PLAYER_X) return m;
        }

        for (int m : avail) {
            Game copy = game.copy();
            copy.setAt(m, Game.PLAYER_O);
            if (copy.checkWinner() == Game.PLAYER_O) return m;
        }

        if (avail.contains(4)) return 4;
        int[] corners = {0,2,6,8};
        for (int c : corners) if (avail.contains(c)) return c;
        return avail.get(random.nextInt(avail.size()));
    }

    private void handleGameEnd(int winner) {
        gameOver = true;
        if (winner == Game.PLAYER_X) {
            status.setText("X wins!");
            prefs.edit().putInt(KEY_X_WINS, prefs.getInt(KEY_X_WINS,0)+1).apply();
        } else if (winner == Game.PLAYER_O) {
            status.setText("O wins!");
            prefs.edit().putInt(KEY_O_WINS, prefs.getInt(KEY_O_WINS,0)+1).apply();
        } else if (winner == Game.DRAW) {
            status.setText("Draw");
            prefs.edit().putInt(KEY_DRAWS, prefs.getInt(KEY_DRAWS,0)+1).apply();
        } else {
            status.setText("Game over");
        }
        disableAllCells();
        updateScoreText();

        // compute adjusted round using stored offset
        int offset = prefs.getInt(KEY_ROUND_OFFSET, 0); // default 0 (no offset)
        int dbMax = dbHelper.getMaxRound(currentUsername); // existing max in DB (0 if none)
        int adjustedRound = Math.max(1, dbMax - offset + 1);

        String winnerStr = (winner == Game.PLAYER_X) ? "X" :
                (winner == Game.PLAYER_O) ? "O" : "Draw";
        String modeStr = playerVsComputer ? "PvC" : "PvP";
        String timestamp = DateFormat.getDateTimeInstance().format(new Date());
        dbHelper.insertGame(currentUsername, adjustedRound, winnerStr, modeStr, timestamp);
    }

    private void updateUI() {
        int colorX = ContextCompat.getColor(this, R.color.x_red);
        int colorO = ContextCompat.getColor(this, R.color.o_blue);
        int colorEmpty = ContextCompat.getColor(this, R.color.white);

        for (int i = 0; i < 9; i++) {
            int cell = game.getBoard()[i];
            if (cell == Game.PLAYER_X) {
                cells[i].setText("X");
                cells[i].setTextColor(colorX);
                cells[i].setEnabled(false);
            } else if (cell == Game.PLAYER_O) {
                cells[i].setText("O");
                cells[i].setTextColor(colorO);
                cells[i].setEnabled(false);
            } else {
                cells[i].setText("");
                cells[i].setTextColor(colorEmpty);
                boolean enable = !gameOver && !(playerVsComputer && game.getCurrentTurn() == Game.PLAYER_X);
                cells[i].setEnabled(enable);
            }
        }
        if (gameOver) disableAllCells();
    }

    private void resetBoard() {
        game.reset();
        gameOver = false;
        updateUI();
        status.setText(game.getCurrentTurn() == Game.PLAYER_O ? "O's turn" : "X's turn");
    }

    private void updateScoreText() {
        int o = prefs.getInt(KEY_O_WINS, 0);
        int x = prefs.getInt(KEY_X_WINS, 0);
        int d = prefs.getInt(KEY_DRAWS, 0);
        scoreO.setText(String.valueOf(o));
        scoreX.setText(String.valueOf(x));
        scoreDraw.setText(String.valueOf(d));
    }

    private void disableAllCells() {
        for (Button b : cells) b.setEnabled(false);
    }
}
