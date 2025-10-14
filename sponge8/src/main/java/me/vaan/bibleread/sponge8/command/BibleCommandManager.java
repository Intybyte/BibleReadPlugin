package me.vaan.bibleread.sponge8.command;

import co.aikar.commands.CommandCompletions;
import co.aikar.commands.SpongeCommandCompletionContext;
import co.aikar.commands.SpongeCommandManager;
import me.vaan.bibleread.api.access.AccessManager;
import me.vaan.bibleread.api.data.access.PlayerDataManager;
import me.vaan.bibleread.api.data.access.TranslationBookPair;
import me.vaan.bibleread.api.data.book.TranslationBooks;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.plugin.PluginContainer;

import java.util.Collections;
import java.util.Optional;

public class BibleCommandManager extends SpongeCommandManager {
    public BibleCommandManager(PluginContainer plugin) {
        super(plugin);
        AccessManager accessManager = AccessManager.getInstance();

        CommandCompletions<SpongeCommandCompletionContext> commandCompletions = this.getCommandCompletions();
        commandCompletions.registerAsyncCompletion(
            "translations",
            context -> accessManager.getTranslations().getTranslationIds()
        );

        commandCompletions.registerAsyncCompletion(
            "books",
            context -> {
                Player player = context.getPlayer();
                if (player == null) return Collections.emptyList();

                TranslationBookPair pair = PlayerDataManager.getData(player.uniqueId());
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
