package me.vaan.bibleread.paper.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.vaan.bibleread.api.access.AccessManager;
import me.vaan.bibleread.api.connection.ConnectionHandler;
import me.vaan.bibleread.api.data.access.ChapterPointer;
import me.vaan.bibleread.api.data.access.PlayerDataManager;
import me.vaan.bibleread.api.data.access.TranslationBookPair;
import me.vaan.bibleread.api.data.chapter.TranslationBookChapter;
import me.vaan.bibleread.api.file.FileManager;
import me.vaan.bibleread.api.file.translation.LocaleHolder;
import me.vaan.bibleread.bukkit.PluginHolder;
import me.vaan.bibleread.bukkit.parser.ChapterContentParser;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
@CommandAlias("bibleread")
@Description("Main bibleread command")
public class PaperCommand extends BaseCommand {

    @Subcommand("chapter")
    public class ChapterCommand extends BaseCommand {

        @Subcommand("openDialog")
        public void openDialog(Player player, int chapterId) {
            LocaleHolder holder = new LocaleHolder(player.getLocale(), player::sendMessage);
            TranslationBookPair pair = PlayerDataManager.getData(player.getUniqueId());
            if (!pair.isValid()) {
                holder.sendMessage("invalid_book_translation_pair");
                return;
            }

            Executor mainExecutor = command -> Bukkit.getScheduler().runTask(PluginHolder.getInstance(), command);
            CompletableFuture<Optional<Dialog>> dialogProvider = AccessManager.getInstance().getChapter(pair.getTranslationId(), pair.getBookId(), chapterId).thenApplyAsync( (chapterOptional) -> {
                if (chapterOptional.isEmpty()) {
                    player.sendMessage("network_error_not_found");
                    return Optional.empty();
                }

                try {
                    TranslationBookChapter chapter = chapterOptional.get();

                    int currentChapter = chapter.getChapter().getNumber();
                    int maxChapter = chapter.getBook().getNumberOfChapters();

                    String collapsed = String.join("", ChapterContentParser.parse(chapter));
                    String title = chapter.getBook().getCommonName() + " " + chapter.getChapter().getNumber();

                    int width = 512;

                    DialogBody body = DialogBody.plainMessage(
                        PlainTextComponentSerializer.plainText().deserialize(collapsed),
                        width
                    );

                    Dialog dialog = Dialog.create((dialogRegistryBuilderFactory -> {
                        dialogRegistryBuilderFactory.empty()
                            .base(
                                DialogBase.builder(Component.text(title))
                                    .body(List.of(
                                        body
                                    ))
                                    .build())
                            .type(DialogType.multiAction(
                                List.of(
                                    getChapterButton(player, "prev_chapter", currentChapter - 1, maxChapter),
                                    getChapterButton(player, "next_chapter", currentChapter + 1, maxChapter)
                                ), null, 3
                            ));
                    }));

                    return Optional.of(dialog);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Optional.empty();
            }, ConnectionHandler.getInstance().getConnectionExecutor());

            dialogProvider.thenAcceptAsync((dialogOptional) -> {
                if (dialogOptional.isEmpty()) {
                    player.sendMessage("network_error_not_found");
                    return;
                }

                player.showDialog(dialogOptional.get());
            }, mainExecutor);
        }

        private ActionButton getChapterButton(Player player, String key, int updateChapter, int maxChapter) {
            FileManager fmg = FileManager.getInstance();
            String locale = player.getLocale();
            String chapterMessage = fmg.message(locale, key);
            String tooltipMessage = fmg.message(locale, key + "_tooltip");

            return ActionButton.create(
                Component.text(chapterMessage),
                Component.text(tooltipMessage),
                128,
                DialogAction.customClick((r, a) -> {
                    if (1 <= updateChapter && updateChapter <= maxChapter) {
                        player.closeInventory();
                        openDialog(player, updateChapter);
                    }
                }, ClickCallback.Options.builder().build())
            );
        }
    }
}
