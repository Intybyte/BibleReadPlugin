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
import me.vaan.bibleread.bukkit.PluginHolder;
import me.vaan.bibleread.bukkit.parser.ChapterContentParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@CommandAlias("bibleread")
@Description("Main bibleread command")
public class PaperCommand extends BaseCommand {

    private final Map<ChapterPointer, Dialog> dialogCache = new ConcurrentHashMap<>();

    @Subcommand("chapter")
    public class ChapterCommand extends BaseCommand {

        @Subcommand("openDialog")
        @SuppressWarnings("UnstableApiUsage")
        public void openDialog(Player player, int chapterId) {
            TranslationBookPair pair = PlayerDataManager.getData(player.getUniqueId());
            if (!pair.isValid()) {
                player.sendMessage("Error");
                return;
            }

            ChapterPointer ptr = pair.toPointer(chapterId);
            Dialog cachaedDialog = dialogCache.get(ptr);
            if (cachaedDialog != null) {
                player.showDialog(cachaedDialog);
                return;
            }

            Executor mainExecutor = command -> Bukkit.getScheduler().runTask(PluginHolder.getInstance(), command);
            CompletableFuture<Optional<Dialog>> dialogProvider = AccessManager.getInstance().getChapter(pair.getTranslationId(), pair.getBookId(), chapterId).thenApplyAsync( (chapterOptional) -> {
                if (chapterOptional.isEmpty()) {
                    player.sendMessage("Error");
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
                                    .body(Collections.singletonList(body))
                                    .build())
                            .type(DialogType.confirmation(
                                ActionButton.create(
                                    Component.text("Previous chapter"),
                                    Component.text("Go to previous chapter if present."),
                                    100,
                                    DialogAction.customClick((r, a) -> {
                                        if (currentChapter > 1) {
                                            player.closeInventory();
                                            openDialog(player, currentChapter - 1);
                                        }
                                    }, ClickCallback.Options.builder().build())
                                ),
                                ActionButton.create(
                                    Component.text("Next chapter"),
                                    Component.text("Go to next chapter if present."),
                                    100,
                                    DialogAction.customClick((r, a) -> {
                                        if (currentChapter < maxChapter) {
                                            player.closeInventory();
                                            openDialog(player, currentChapter + 1);
                                        }
                                    }, ClickCallback.Options.builder().build())
                                )
                            ));
                    }));
                    dialogCache.put(ptr, dialog);

                    return Optional.of(dialog);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Optional.empty();
            }, ConnectionHandler.getInstance().getConnectionExecutor());

            dialogProvider.thenAcceptAsync((dialogOptional) -> {
                if (dialogOptional.isEmpty()) {
                    player.sendMessage("Error");
                    return;
                }

                player.showDialog(dialogOptional.get());
            }, mainExecutor);
        }
    }

}
