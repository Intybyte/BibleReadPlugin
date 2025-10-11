package me.vaan.bibleread.bukkit;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.vaan.bibleread.api.file.FileManager;
import me.vaan.bibleread.bukkit.data.TranslationBookPair;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private static Map<UUID, TranslationBookPair> playerMap = new HashMap<>();
    private static final TypeToken<Map<UUID, TranslationBookPair>> type = new TypeToken<Map<UUID, TranslationBookPair>>(){};

    public static void setTranslationBook(OfflinePlayer player, String translation) {
        UUID uuid = player.getUniqueId();
        TranslationBookPair pair = playerMap.get(uuid);
        if (pair == null) {
            playerMap.put(uuid, new TranslationBookPair(translation, null));
            return;
        }

        pair.setTranslationId(translation);
    }

    public static void setBookId(OfflinePlayer player, String bookId) {
        UUID uuid = player.getUniqueId();
        TranslationBookPair pair = playerMap.get(uuid);
        if (pair == null) {
            playerMap.put(uuid, new TranslationBookPair(null, bookId));
            return;
        }

        pair.setBookId(bookId);
    }

    public static TranslationBookPair getData(OfflinePlayer player) {
        playerMap.putIfAbsent(player.getUniqueId(), new TranslationBookPair(null, null));
        return playerMap.get(player.getUniqueId());
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
