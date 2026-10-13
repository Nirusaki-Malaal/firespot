package com.fireemu.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fireemu.launcher.settings.PerformanceManager;
import com.fireemu.launcher.storage.RomScanner;
import com.fireemu.launcher.ui.LicensesActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private RomScanner romScanner;
    private PerformanceManager perfManager;
    private String selectedConsole = RomScanner.CONSOLE_NES;
    private List<RomScanner.GameItem> allConsoleGames = new ArrayList<>();

    private LinearLayout layoutConsoleTiles;
    private LinearLayout layoutGamesContainer;
    private TextView tvActiveConsoleLabel;
    private TextView tvGameCount;
    private EditText etSearch;
    private Button btnSettings;

    private final String[] CONSOLES = {
        RomScanner.CONSOLE_NES,
        RomScanner.CONSOLE_GAMEBOY,
        RomScanner.CONSOLE_GBA,
        RomScanner.CONSOLE_PS1,
        RomScanner.CONSOLE_PSP
    };

    private final String[] CONSOLE_LABELS = {
        "NES (Nintendo)",
        "Game Boy / Color",
        "Game Boy Advance",
        "PlayStation 1",
        "PSP (PlayStation Portable)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        romScanner = new RomScanner(this);
        perfManager = new PerformanceManager(this);

        layoutConsoleTiles = findViewById(R.id.layout_console_tiles);
        layoutGamesContainer = findViewById(R.id.layout_games_container);
        tvActiveConsoleLabel = findViewById(R.id.tv_active_console_label);
        tvGameCount = findViewById(R.id.tv_game_count);
        etSearch = findViewById(R.id.et_search);
        btnSettings = findViewById(R.id.btn_settings);

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LicensesActivity.class);
            startActivity(intent);
        });

        setupConsoleTiles();
        setupSearch();
        loadGamesForConsole(selectedConsole);
    }

    private void setupConsoleTiles() {
        layoutConsoleTiles.removeAllViews();
        for (int i = 0; i < CONSOLES.length; i++) {
            final String consoleId = CONSOLES[i];
            final String label = CONSOLE_LABELS[i];

            Button tile = new Button(this);
            tile.setText(label);
            tile.setTextColor(Color.WHITE);
            tile.setTextSize(14f);
            tile.setBackgroundResource(R.drawable.card_focus_selector);
            tile.setFocusable(true);
            tile.setFocusableInTouchMode(false);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                110
            );
            lp.setMargins(0, 0, 16, 0);
            tile.setLayoutParams(lp);
            tile.setPadding(24, 8, 24, 8);

            tile.setOnClickListener(v -> {
                selectedConsole = consoleId;
                loadGamesForConsole(selectedConsole);
            });

            layoutConsoleTiles.addView(tile);
            if (i == 0) {
                tile.requestFocus();
            }
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterGames(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadGamesForConsole(String console) {
        tvActiveConsoleLabel.setText(getConsoleDisplayName(console));
        allConsoleGames = romScanner.scanConsole(console);
        renderGameCards(allConsoleGames);
    }

    private void filterGames(String query) {
        if (query.trim().isEmpty()) {
            renderGameCards(allConsoleGames);
            return;
        }
        List<RomScanner.GameItem> filtered = new ArrayList<>();
        String lower = query.toLowerCase();
        for (RomScanner.GameItem game : allConsoleGames) {
            if (game.title.toLowerCase().contains(lower)) {
                filtered.add(game);
            }
        }
        renderGameCards(filtered);
    }

    private void renderGameCards(List<RomScanner.GameItem> games) {
        layoutGamesContainer.removeAllViews();
        tvGameCount.setText(games.size() + " Titles Found");

        if (games.isEmpty()) {
            TextView emptyTv = new TextView(this);
            emptyTv.setText("No ROMs found in /sdcard/ROMs/" + selectedConsole + "/\nConnect USB drive or copy files via ADB.");
            emptyTv.setTextColor(Color.parseColor("#9DA7B8"));
            emptyTv.setTextSize(16f);
            emptyTv.setPadding(16, 32, 16, 16);
            layoutGamesContainer.addView(emptyTv);
            return;
        }

        // Render Recently Played row if available
        List<String> recents = romScanner.getRecentlyPlayed();
        if (!recents.isEmpty()) {
            TextView recentsTitle = new TextView(this);
            recentsTitle.setText("RECENTLY PLAYED");
            recentsTitle.setTextColor(Color.parseColor("#FFC107"));
            recentsTitle.setTextSize(14f);
            recentsTitle.setPadding(0, 12, 0, 8);
            layoutGamesContainer.addView(recentsTitle);

            LinearLayout recentsRow = new LinearLayout(this);
            recentsRow.setOrientation(LinearLayout.HORIZONTAL);
            recentsRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            for (String rPath : recents) {
                File f = new File(rPath);
                if (f.exists()) {
                    Button rBtn = createGameCard(new RomScanner.GameItem(f.getName(), rPath, selectedConsole, f.length(), false));
                    recentsRow.addView(rBtn);
                    if (recentsRow.getChildCount() >= 4) break;
                }
            }
            layoutGamesContainer.addView(recentsRow);
        }

        // Render All Games
        TextView allTitle = new TextView(this);
        allTitle.setText("ALL GAMES");
        allTitle.setTextColor(Color.parseColor("#9DA7B8"));
        allTitle.setTextSize(14f);
        allTitle.setPadding(0, 20, 0, 8);
        layoutGamesContainer.addView(allTitle);

        LinearLayout row = null;
        for (int i = 0; i < games.size(); i++) {
            if (i % 3 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                row.setPadding(0, 0, 0, 16);
                layoutGamesContainer.addView(row);
            }
            Button card = createGameCard(games.get(i));
            if (row != null) {
                row.addView(card);
            }
        }
    }

    private Button createGameCard(RomScanner.GameItem game) {
        Button card = new Button(this);
        card.setText(game.title);
        card.setTextColor(Color.WHITE);
        card.setTextSize(14f);
        card.setGravity(Gravity.CENTER);
        card.setBackgroundResource(R.drawable.card_focus_selector);
        card.setFocusable(true);
        card.setFocusableInTouchMode(false);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            340, 160
        );
        lp.setMargins(0, 0, 16, 0);
        card.setLayoutParams(lp);

        card.setOnClickListener(v -> launchGame(game));

        card.setOnLongClickListener(v -> {
            romScanner.toggleFavorite(game.console, game.path);
            Toast.makeText(MainActivity.this, "Toggled Favorite: " + game.title, Toast.LENGTH_SHORT).show();
            loadGamesForConsole(selectedConsole);
            return true;
        });

        return card;
    }

    private void launchGame(RomScanner.GameItem game) {
        romScanner.addRecentlyPlayed(game.path);
        Intent intent = new Intent(MainActivity.this, EmulatorActivity.class);
        intent.putExtra("EXTRA_CONSOLE", game.console);
        intent.putExtra("EXTRA_ROM_PATH", game.path);
        startActivity(intent);
    }

    private String getConsoleDisplayName(String console) {
        for (int i = 0; i < CONSOLES.length; i++) {
            if (CONSOLES[i].equals(console)) return CONSOLE_LABELS[i];
        }
        return console.toUpperCase();
    }
}
