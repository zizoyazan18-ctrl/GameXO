package com.example.gamexo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private RadioGroup difficultyRadioGroup;
    private Button startGameBtn;

    private static final String PREFS_NAME = "XOPrefs";
    private static final String KEY_DIFFICULTY = "difficulty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        difficultyRadioGroup = findViewById(R.id.difficultyRadioGroup);
        startGameBtn = findViewById(R.id.startGameBtn);

        loadPreferences();

        startGameBtn.setOnClickListener(v -> {
            savePreferences();
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });
    }

    private void loadPreferences() {
        String savedDifficulty = sharedPreferences.getString(KEY_DIFFICULTY, "medium");
        switch (savedDifficulty) {
            case "easy":
                ((RadioButton) findViewById(R.id.radioEasy)).setChecked(true);
                break;
            case "hard":
                ((RadioButton) findViewById(R.id.radioHard)).setChecked(true);
                break;
            default:
                ((RadioButton) findViewById(R.id.radioMedium)).setChecked(true);
                break;
        }
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int diffId = difficultyRadioGroup.getCheckedRadioButtonId();
        String difficulty;
        if (diffId == R.id.radioEasy) difficulty = "easy";
        else if (diffId == R.id.radioHard) difficulty = "hard";
        else difficulty = "medium";
        editor.putString(KEY_DIFFICULTY, difficulty);

        editor.apply();
        Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show();
    }
}