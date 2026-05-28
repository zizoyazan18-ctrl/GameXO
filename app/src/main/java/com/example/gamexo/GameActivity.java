package com.example.gamexo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private TextView statusTv;
    private TextView playerScoreTv;
    private TextView computerScoreTv;
    private GridLayout gridLayout;
    private Button[][] buttons = new Button[3][3];
    private Button resetBtn, backBtn;
    private String difficulty;
    private boolean gameActive = true;
    private boolean playerTurn = true;
    private int moveCount = 0;
    private Random random = new Random();

    private int playerScore = 0;
    private int computerScore = 0;

    private static final String PREFS_NAME = "XOPrefs";
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_PLAYER_SCORE = "playerScore";
    private static final String KEY_COMPUTER_SCORE = "computerScore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        difficulty = prefs.getString(KEY_DIFFICULTY, "medium");

        String language = prefs.getString(KEY_LANGUAGE, "en");
        setLocale(language);

        playerScore = prefs.getInt(KEY_PLAYER_SCORE, 0);
        computerScore = prefs.getInt(KEY_COMPUTER_SCORE, 0);

        statusTv = findViewById(R.id.statusTv);
        playerScoreTv = findViewById(R.id.playerScoreTv);
        computerScoreTv = findViewById(R.id.computerScoreTv);
        gridLayout = findViewById(R.id.gridLayout);
        resetBtn = findViewById(R.id.resetBtn);
        backBtn = findViewById(R.id.backBtn);

        updateScoreDisplay();
        updateUITexts();
        createBoard();

        resetBtn.setOnClickListener(v -> resetGame());
        backBtn.setOnClickListener(v -> {
            saveScores();
            finish();
        });
        Button resetScoresBtn = findViewById(R.id.resetScoresBtn);
        resetScoresBtn.setOnClickListener(v -> resetScores());
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void updateUITexts() {
        if (!gameActive) return;
        if (playerTurn) {
            statusTv.setText(getString(R.string.your_turn));
        } else {
            statusTv.setText(getString(R.string.computer_thinking));
        }
        resetBtn.setText(getString(R.string.reset_game));
        backBtn.setText(getString(R.string.back_settings));
    }

    private void createBoard() {
        gridLayout.removeAllViews();
        gridLayout.setRowCount(3);
        gridLayout.setColumnCount(3);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Button btn = new Button(this);
                btn.setText("");
                btn.setTextSize(32);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.rowSpec = GridLayout.spec(i, 1f);
                params.columnSpec = GridLayout.spec(j, 1f);
                params.setMargins(8, 8, 8, 8);
                btn.setLayoutParams(params);

                int row = i, col = j;
                btn.setOnClickListener(v -> onPlayerMove(row, col));

                gridLayout.addView(btn);
                buttons[i][j] = btn;
            }
        }
    }

    private void onPlayerMove(int row, int col) {
        if (!gameActive || !playerTurn) return;
        if (!buttons[row][col].getText().toString().isEmpty()) return;

        buttons[row][col].setText("X");
        buttons[row][col].setTextColor(getColor(android.R.color.holo_blue_dark));
        moveCount++;

        if (checkWin(row, col, "X")) {
            playerScore++;
            updateScoreDisplay();
            saveScores();
            statusTv.setText(getString(R.string.you_win));
            gameActive = false;
            return;
        }

        if (moveCount == 9) {
            statusTv.setText(getString(R.string.draw));
            gameActive = false;
            return;
        }

        playerTurn = false;
        statusTv.setText(getString(R.string.computer_thinking));

        new Handler().postDelayed(this::computerMove, 500);
    }

    private void computerMove() {
        if (!gameActive || playerTurn) return;

        int[] move;

        switch (difficulty) {
            case "easy":
                move = getRandomMove();
                break;
            case "hard":
                move = getBestMove();
                break;
            default:
                if (random.nextInt(100) < 70) {
                    move = getBestMove();
                } else {
                    move = getRandomMove();
                }
                break;
        }

        if (move != null) {
            int row = move[0];
            int col = move[1];

            buttons[row][col].setText("O");
            buttons[row][col].setTextColor(getColor(android.R.color.holo_red_dark));
            moveCount++;

            if (checkWin(row, col, "O")) {
                computerScore++;
                updateScoreDisplay();
                saveScores();
                statusTv.setText(getString(R.string.computer_wins));
                gameActive = false;
                return;
            }

            if (moveCount == 9) {
                statusTv.setText(getString(R.string.draw));
                gameActive = false;
                return;
            }

            playerTurn = true;
            statusTv.setText(getString(R.string.your_turn));
        }
    }

    private int[] getRandomMove() {
        ArrayList<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().isEmpty()) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }
        if (emptyCells.isEmpty()) return null;
        return emptyCells.get(random.nextInt(emptyCells.size()));
    }

    private int[] getBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().isEmpty()) {
                    buttons[i][j].setText("O");
                    int score = minimax(false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    buttons[i][j].setText("");

                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = new int[]{i, j};
                    }
                }
            }
        }
        return bestMove != null ? bestMove : getRandomMove();
    }

    private int minimax(boolean isMaximizing, int alpha, int beta) {
        if (checkWinForMinimax("X")) return -10;
        if (checkWinForMinimax("O")) return 10;
        if (isBoardFull()) return 0;

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (buttons[i][j].getText().toString().isEmpty()) {
                        buttons[i][j].setText("O");
                        int score = minimax(false, alpha, beta);
                        buttons[i][j].setText("");
                        bestScore = Math.max(score, bestScore);
                        alpha = Math.max(alpha, bestScore);
                        if (beta <= alpha) break;
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (buttons[i][j].getText().toString().isEmpty()) {
                        buttons[i][j].setText("X");
                        int score = minimax(true, alpha, beta);
                        buttons[i][j].setText("");
                        bestScore = Math.min(score, bestScore);
                        beta = Math.min(beta, bestScore);
                        if (beta <= alpha) break;
                    }
                }
            }
            return bestScore;
        }
    }

    private boolean checkWinForMinimax(String symbol) {
        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().equals(symbol) &&
                    buttons[i][1].getText().equals(symbol) &&
                    buttons[i][2].getText().equals(symbol)) return true;
        }
        for (int j = 0; j < 3; j++) {
            if (buttons[0][j].getText().equals(symbol) &&
                    buttons[1][j].getText().equals(symbol) &&
                    buttons[2][j].getText().equals(symbol)) return true;
        }
        if (buttons[0][0].getText().equals(symbol) &&
                buttons[1][1].getText().equals(symbol) &&
                buttons[2][2].getText().equals(symbol)) return true;
        if (buttons[0][2].getText().equals(symbol) &&
                buttons[1][1].getText().equals(symbol) &&
                buttons[2][0].getText().equals(symbol)) return true;

        return false;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().isEmpty()) return false;
            }
        }
        return true;
    }

    private boolean checkWin(int row, int col, String symbol) {
        if (buttons[row][0].getText().equals(symbol) &&
                buttons[row][1].getText().equals(symbol) &&
                buttons[row][2].getText().equals(symbol)) return true;
        if (buttons[0][col].getText().equals(symbol) &&
                buttons[1][col].getText().equals(symbol) &&
                buttons[2][col].getText().equals(symbol)) return true;
        if (buttons[0][0].getText().equals(symbol) &&
                buttons[1][1].getText().equals(symbol) &&
                buttons[2][2].getText().equals(symbol)) return true;
        if (buttons[0][2].getText().equals(symbol) &&
                buttons[1][1].getText().equals(symbol) &&
                buttons[2][0].getText().equals(symbol)) return true;

        return false;
    }

    private void updateScoreDisplay() {
        playerScoreTv.setText(getString(R.string.you) + ": " + playerScore);
        computerScoreTv.setText(getString(R.string.computer) + ": " + computerScore);
    }

    private void saveScores() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_PLAYER_SCORE, playerScore);
        editor.putInt(KEY_COMPUTER_SCORE, computerScore);
        editor.apply();
    }

    private void resetGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
            }
        }
        gameActive = true;
        playerTurn = true;
        moveCount = 0;
        statusTv.setText(getString(R.string.your_turn));
    }
    private void resetScores() {
        playerScore = 0;
        computerScore = 0;
        updateScoreDisplay();
        saveScores();

        Toast.makeText(this, "Scores reset to zero!", Toast.LENGTH_SHORT).show();
    }
}