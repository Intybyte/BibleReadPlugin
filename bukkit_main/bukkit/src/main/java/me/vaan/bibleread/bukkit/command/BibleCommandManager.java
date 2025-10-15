package me.vaan.bibleread.bukkit.command;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.CommandCompletions;
import lombok.Getter;
import me.vaan.bibleread.api.access.AccessManager;
import me.vaan.bibleread.api.data.access.PlayerDataManager;
import me.vaan.bibleread.api.data.access.TranslationBookPair;
import me.vaan.bibleread.api.data.book.TranslationBooks;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Optional;

@Getter
public class BibleCommandManager {
    private final BukkitCommandManager commandManager;

    public BibleCommandManager(BukkitCommandManager commandManager) {
        this.commandManager = commandManager;
        AccessManager accessManager = AccessManager.getInstance();

        CommandCompletions<BukkitCommandCompletionContext> commandCompletions = commandManager.getCommandCompletions();
        commandCompletions.registerAsyncCompletion(
            "translations",
            context -> accessManager.getTranslations().getTranslationIds()
        );

        commandCompletions.registerAsyncCompletion(
            "books",
            context -> {
                Player player = context.getPlayer();
                if (player == null) return Collections.emptyList();

                TranslationBookPair pair = PlayerDataManager.getData(player.getUniqueId());
                String translation = pair.getTranslationId();
                if (translation == null) return Collections.emptyList();

                Optional<TranslationBooks> translationBooksOptional = accessManager.getTranslationBooks(translation).join();
                if (!translationBooksOptional.isPresent()) return Collections.emptyList();

                return translationBooksOptional.get().getBookMap().keySet();
            }
        );

        commandCompletions.registerAsyncCompletion(
            "languages",
            context -> accessManager.getTranslations().getLanguageMap().keySet()
        );
    }
}
