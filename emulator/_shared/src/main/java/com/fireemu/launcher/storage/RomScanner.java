package com.fireemu.launcher.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RomScanner {
    public static final String CONSOLE_NES = "nes";
    public static final String CONSOLE_GAMEBOY = "gameboy";
    public static final String CONSOLE_GBA = "gba";
    public static final String CONSOLE_PS1 = "ps1";
    public static final String CONSOLE_PSP = "psp";

    public static class GameItem {
        public final String title;
        public final String path;
        public final String console;
        public final long sizeBytes;
        public boolean isFavorite;

        public GameItem(String title, String path, String console, long sizeBytes, boolean isFavorite) {
            this.title = title;
            this.path = path;
            this.console = console;
            this.sizeBytes = sizeBytes;
            this.isFavorite = isFavorite;
        }
    }

    private final Context context;
    private final SharedPreferences prefs;

    public RomScanner(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("fireemu_roms", Context.MODE_PRIVATE);
    }

    public static List<String> getExtensionsForConsole(String console) {
        switch (console) {
            case CONSOLE_NES:
                return Arrays.asList(".nes", ".fds");
            case CONSOLE_GAMEBOY:
                return Arrays.asList(".gb", ".gbc");
            case CONSOLE_GBA:
                return Collections.singletonList(".gba");
            case CONSOLE_PS1:
                return Arrays.asList(".cue", ".bin", ".chd", ".pbp", ".iso");
            case CONSOLE_PSP:
                return Arrays.asList(".iso", ".cso", ".pbp");
            default:
                return Collections.emptyList();
        }
    }

    public List<GameItem> scanConsole(String console) {
        List<GameItem> results = new ArrayList<>();
        List<String> validExtensions = getExtensionsForConsole(console);
        Set<String> favorites = prefs.getStringSet("fav_" + console, new HashSet<String>());

        // 1. Primary storage path: /sdcard/ROMs/<console>/
        File defaultDir = new File(Environment.getExternalStorageDirectory(), "ROMs/" + console);
        if (!defaultDir.exists()) {
            defaultDir.mkdirs();
        }
        scanDirectory(defaultDir, console, validExtensions, favorites, results);

        // 2. Scan external / OTG USB mounts: /storage/...
        File storageRoot = new File("/storage");
        if (storageRoot.exists() && storageRoot.isDirectory()) {
            File[] mounts = storageRoot.listFiles();
            if (mounts != null) {
                for (File mount : mounts) {
                    if (mount.isDirectory() && !mount.getName().equalsIgnoreCase("emulated") && !mount.getName().equalsIgnoreCase("self")) {
                        File usbRomDir = new File(mount, "ROMs/" + console);
                        if (usbRomDir.exists() && usbRomDir.isDirectory()) {
                            scanDirectory(usbRomDir, console, validExtensions, favorites, results);
                        }
                    }
                }
            }
        }

        // Sort alphabetically by title
        Collections.sort(results, (a, b) -> a.title.compareToIgnoreCase(b.title));
        return results;
    }

    private void scanDirectory(File dir, String console, List<String> exts, Set<String> favs, List<GameItem> out) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, console, exts, favs, out);
            } else {
                String name = file.getName();
                String lower = name.toLowerCase();
                for (String ext : exts) {
                    if (lower.endsWith(ext)) {
                        String cleanTitle = name.substring(0, name.lastIndexOf('.'));
                        boolean isFav = favs.contains(file.getAbsolutePath());
                        out.add(new GameItem(cleanTitle, file.getAbsolutePath(), console, file.length(), isFav));
                        break;
                    }
                }
            }
        }
    }

    public void toggleFavorite(String console, String path) {
        Set<String> favorites = new HashSet<>(prefs.getStringSet("fav_" + console, new HashSet<String>()));
        if (favorites.contains(path)) {
            favorites.remove(path);
        } else {
            favorites.add(path);
        }
        prefs.edit().putStringSet("fav_" + console, favorites).apply();
    }

    public void addRecentlyPlayed(String path) {
        List<String> recents = getRecentlyPlayed();
        recents.remove(path);
        recents.add(0, path);
        if (recents.size() > 20) {
            recents = recents.subList(0, 20);
        }
        prefs.edit().putString("recently_played", String.join(";;", recents)).apply();
    }

    public List<String> getRecentlyPlayed() {
        String raw = prefs.getString("recently_played", "");
        if (raw.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(";;")));
    }

    public static String getBiosDirectory() {
        File biosDir = new File(Environment.getExternalStorageDirectory(), "ROMs/bios");
        if (!biosDir.exists()) biosDir.mkdirs();
        return biosDir.getAbsolutePath();
    }
}
