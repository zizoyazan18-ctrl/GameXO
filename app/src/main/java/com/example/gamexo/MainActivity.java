package com.example.gamexo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private RadioGroup languageRadioGroup, difficultyRadioGroup;
    private Button startGameBtn;
    private RadioButton radioEnglish, radioArabic, radioEasy, radioMedium, radioHard;
    private TextView titleTv, languageLabel, difficultyLabel;

    private static final String PREFS_NAME = "XOPrefs";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_DIFFICULTY = "difficulty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        titleTv = findViewById(R.id.titleTv);
        languageLabel = findViewById(R.id.languageLabel);
        difficultyLabel = findViewById(R.id.difficultyLabel);
        languageRadioGroup = findViewById(R.id.languageRadioGroup);
        difficultyRadioGroup = findViewById(R.id.difficultyRadioGroup);
        startGameBtn = findViewById(R.id.startGameBtn);
        radioEnglish = findViewById(R.id.radioEnglish);
        radioArabic = findViewById(R.id.radioArabic);
        radioEasy = findViewById(R.id.radioEasy);
        radioMedium = findViewById(R.id.radioMedium);
        radioHard = findViewById(R.id.radioHard);

        loadPreferences();

        startGameBtn.setOnClickListener(v -> {
            savePreferences();
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });
    }

    private void loadPreferences() {
        String savedLanguage = sharedPreferences.getString(KEY_LANGUAGE, "en");
        if (savedLanguage.equals("ar")) {
            radioArabic.setChecked(true);
            radioEnglish.setChecked(false);
            setLocale("ar");
        } else {
            radioEnglish.setChecked(true);
            radioArabic.setChecked(false);
            setLocale("en");
        }

        String savedDifficulty = sharedPreferences.getString(KEY_DIFFICULTY, "medium");
        switch (savedDifficulty) {
            case "easy":
                radioEasy.setChecked(true);
                radioMedium.setChecked(false);
                radioHard.setChecked(false);
                break;
            case "hard":
                radioHard.setChecked(true);
                radioEasy.setChecked(false);
                radioMedium.setChecked(false);
                break;
            default:
                radioMedium.setChecked(true);
                radioEasy.setChecked(false);
                radioHard.setChecked(false);
                break;
        }

        updateUITexts();
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String language = radioArabic.isChecked() ? "ar" : "en";
        editor.putString(KEY_LANGUAGE, language);
        setLocale(language);

        String difficulty;
        if (radioEasy.isChecked()) difficulty = "easy";
        else if (radioHard.isChecked()) difficulty = "hard";
        else difficulty = "medium";
        editor.putString(KEY_DIFFICULTY, difficulty);

        editor.apply();
        updateUITexts();
        Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void updateUITexts() {
        titleTv.setText(getString(R.string.game_title));
        languageLabel.setText(getString(R.string.language_label));
        difficultyLabel.setText(getString(R.string.difficulty_label));
        radioEasy.setText(getString(R.string.easy));
        radioMedium.setText(getString(R.string.medium));
        radioHard.setText(getString(R.string.hard));
        radioEnglish.setText(getString(R.string.english));
        radioArabic.setText(getString(R.string.arabic));
        startGameBtn.setText(getString(R.string.start_game));
    }
}