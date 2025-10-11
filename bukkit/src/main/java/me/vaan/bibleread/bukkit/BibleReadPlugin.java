package me.vaan.bibleread.bukkit;

import co.aikar.commands.BukkitCommandManager;
import lombok.Getter;
import me.vaan.bibleread.api.access.AccessManager;
import me.vaan.bibleread.api.connection.ConnectionHandler;
import me.vaan.bibleread.api.data.book.TranslationBooks;
import me.vaan.bibleread.api.file.FileManager;
import me.vaan.bibleread.bukkit.command.MainCommand;
import me.vaan.bibleread.bukkit.data.TranslationBookPair;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Optional;

public class BibleReadPlugin extends JavaPlugin {
    @Getter
    private static BibleReadPlugin instance;

    @Override
    public void onLoad() {
        instance = this;
        FileManager.initialize(getDataFolder());
        ConnectionHandler.initialize();
        AccessManager.initialize();
        PlayerDataManager.load();
    }

    @Override
    public void onEnable() {
        BukkitCommandManager bcm = new BukkitCommandManager(this);
        bcm.getCommandCompletions().registerAsyncCompletion(
            "translations",
            context -> AccessManager.getInstance().getTranslations().getTranslationIds()
        );

        bcm.getCommandCompletions().registerAsyncCompletion(
            "books",
            context -> {
                Player player = context.getPlayer();
                if (player == null) return Collections.emptyList();

                TranslationBookPair pair = PlayerDataManager.getData(player);
                String translation = pair.getTranslationId();
                if (translation == null) return Collections.emptyList();

                Optional<TranslationBooks> translationBooksOptional = AccessManager.getInstance().getTranslationBooks(translation).join();
                if(!translationBooksOptional.isPresent()) return Collections.emptyList();

                return translationBooksOptional.get().getBookMap().keySet();
            }
        );

        bcm.registerCommand(new MainCommand());
    }

    @Override
    public void onDisable() {
        PlayerDataManager.save();
    }
}
