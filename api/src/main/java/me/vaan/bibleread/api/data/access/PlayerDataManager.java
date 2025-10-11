package me.vaan.bibleread.api.data.access;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.vaan.bibleread.api.file.FileManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private static Map<UUID, TranslationBookPair> playerMap = new HashMap<>();
    private static final TypeToken<Map<UUID, TranslationBookPair>> type = new TypeToken<Map<UUID, TranslationBookPair>>(){};

    public static void setTranslationBook(UUID player, String translation) {
        TranslationBookPair pair = playerMap.get(player);
        if (pair == null) {
            playerMap.put(player, new TranslationBookPair(translation, null));
            return;
        }

        pair.setTranslationId(translation);
    }

    public static void setBookId(UUID player, String bookId) {
        TranslationBookPair pair = playerMap.get(player);
        if (pair == null) {
            playerMap.put(player, new TranslationBookPair(null, bookId));
            return;
        }

        pair.setBookId(bookId);
    }

    public static TranslationBookPair getData(UUID player) {
        playerMap.putIfAbsent(player, new TranslationBookPair(null, null));
        return playerMap.get(player);
    }

    public static void load() {

        Gson gson = new Gson();
        File file = FileManager.getInstance().getFile("player_selection.json");
        if (!file.exists()) return;

        try {
            try (FileReader reader = new FileReader(file)) {
                playerMap = gson.fromJson(reader, type.getType());
            }
        } catch (IOException ignored) {}
    }

    public static void save() {
        Gson gson = new Gson();
        File file = FileManager.getInstance().getFile("player_selection.json");

        try {
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(playerMap, type.getType(), writer);
            }
        } catch (Exception ignored) {}
    }
}
