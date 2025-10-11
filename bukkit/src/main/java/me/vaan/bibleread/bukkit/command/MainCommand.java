package me.vaan.bibleread.bukkit.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.vaan.bibleread.api.access.AccessManager;
import me.vaan.bibleread.api.data.chapter.TranslationBookChapter;
import me.vaan.bibleread.api.data.translation.Translation;
import me.vaan.bibleread.bukkit.BibleReadPlugin;
import me.vaan.bibleread.bukkit.parser.BookParser;
import me.vaan.bibleread.bukkit.PlayerDataManager;
import me.vaan.bibleread.api.data.access.ChapterPointer;
import me.vaan.bibleread.api.data.access.TranslationBookPair;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@CommandAlias("bibleread")
@Description("Main bibleread command")
public class MainCommand extends BaseCommand {

    @Subcommand("language")
    public class LanguageCommand extends BaseCommand {

        @Subcommand("getBooks")
        public void getBooks(Player player, String language) {
            Map<String, List<Translation>> languageMap = AccessManager.getInstance().getTranslations().getLanguageMap();
            List<Translation> tls = languageMap.get(language);
            if (tls == null) {
                player.sendMessage("Error");
                return;
            }


        }
    }

    @Subcommand("translation")
    public class TranslationCommand extends BaseCommand {

        @Subcommand("info")
        public void info(Player player) {

        }

        @Subcommand("set")
        @CommandCompletion("@translations")
        public void set(Player player, String translation) {
            boolean valid = AccessManager.getInstance().getTranslations().getTranslationIds().contains(translation);
            if (!valid) {
                player.sendMessage("Error");
                return;
            }

            PlayerDataManager.setTranslationBook(player, translation);
            player.sendMessage("Success");
        }
    }


    @Subcommand("book")
    public class BookCommand extends BaseCommand {

        @Subcommand("info")
        public void info(Player player) {

        }

        @Subcommand("set")
        @CommandCompletion("@books")
        public void set(Player player, String bookId) {
            TranslationBookPair pair = PlayerDataManager.getData(player);
            String translation = pair.getTranslationId();
            if (translation == null) {
                player.sendMessage("Error");
                return;
            }

            Executor mainExecutor = command -> Bukkit.getScheduler().runTask(BibleReadPlugin.getInstance(), command);
            AccessManager.getInstance().getTranslationBooks(translation).thenAcceptAsync((translationBooksOptional) -> {
                if (!translationBooksOptional.isPresent()) {
                    player.sendMessage("Error");
                    return;
                }

                if (!translationBooksOptional.get().getBookMap().containsKey(bookId)) {
                    player.sendMessage("Error");
                    return;
                }

                PlayerDataManager.setBookId(player, bookId);
                player.sendMessage("Success");

            }, mainExecutor);
        }
    }

    @Subcommand("chapter")
    public class ChapterCommand extends BaseCommand {

        @Subcommand("get")
        public void get(Player player, int chapterId) {
            TranslationBookPair pair = PlayerDataManager.getData(player);
            if (!pair.isValid()) {
                player.sendMessage("Error");
                return;
            }

            Executor mainExecutor = command -> Bukkit.getScheduler().runTask(BibleReadPlugin.getInstance(), command);
            AccessManager.getInstance().getChapter(pair.getTranslationId(), pair.getBookId(), chapterId).thenAcceptAsync( (chapterOptional) -> {
                if (!chapterOptional.isPresent()) {
                    player.sendMessage("Error");
                    return;
                }

                TranslationBookChapter chapter = chapterOptional.get();


                ItemStack book = getChapterBook(pair, chapter);
                player.openBook(book);
                //player.getInventory().addItem(book);
            }, mainExecutor);
        }

        @Subcommand("getBook")
        public void getBook(Player player, int chapterId) {
            TranslationBookPair pair = PlayerDataManager.getData(player);
            if (!pair.isValid()) {
                player.sendMessage("Error");
                return;
            }

            Executor mainExecutor = command -> Bukkit.getScheduler().runTask(BibleReadPlugin.getInstance(), command);
            AccessManager.getInstance().getChapter(pair.getTranslationId(), pair.getBookId(), chapterId).thenAcceptAsync( (chapterOptional) -> {
                if (!chapterOptional.isPresent()) {
                    player.sendMessage("Error");
                    return;
                }

                TranslationBookChapter chapter = chapterOptional.get();


                ItemStack book = getChapterBook(pair, chapter);
                player.getInventory().addItem(book);
            }, mainExecutor);
        }
    }


    @Subcommand("update")
    @CommandPermission("biblereadplugin.update")
    public class Update extends BaseCommand {

        @Subcommand("data")
        private void data(CommandSender sender) {
            AccessManager.getInstance().updateAll();
        }
    }

    private static final HashMap<ChapterPointer, ItemStack> bookMap = new HashMap<>();
    private static ItemStack getChapterBook(TranslationBookPair pair, TranslationBookChapter chapter) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        int chpNumber = chapter.getChapter().getNumber();
        ChapterPointer ptr = new ChapterPointer(pair.getTranslationId(), pair.getBookId(), chpNumber);
        if (bookMap.containsKey(ptr)) {
            return bookMap.get(ptr);
        }

        try {

            BookMeta meta = (BookMeta) book.getItemMeta();
            meta.setTitle(chapter.getBook().getCommonName() + " " + chpNumber);
            meta.setGeneration(BookMeta.Generation.TATTERED);
            meta.setAuthor("Unknown");

            BookParser.Result result = BookParser.getParsed(chapter);

            // Add pages to book
            for (int i = 0; i <= result.pageNumber; i++) {
                String page = result.finalPages.getOrDefault(i, "").trim();
                if (page.isEmpty()) page = " ";
                meta.addPage(page);
            }

            int totalChapters = chapter.getBook().getNumberOfChapters();
            TextComponent current = new TextComponent("[Current: " + chpNumber + "]\n");
            current.setBold(true);
            BaseComponent[] nextPages = new BaseComponent[] {
                current,
                new TextComponent(),
                new TextComponent()
            };

            if (chpNumber < totalChapters) {
                TextComponent clickableText = new TextComponent("[Next Chapter]\n");
                clickableText.setBold(true);
                clickableText.setClickEvent(
                    new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/bibleread chapter get " + (chpNumber + 1)
                    )
                );

                nextPages[1] = clickableText;
            }

            if (1 < chpNumber) {
                TextComponent clickableText = new TextComponent("[Previous Chapter]\n");
                clickableText.setBold(true);
                clickableText.setClickEvent(
                    new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/bibleread chapter get " + (chpNumber - 1)
                    )
                );

                nextPages[2] = clickableText;
            }

            meta.spigot().addPage(nextPages);

            book.setItemMeta(meta);
        } catch (Exception e) {
            e.printStackTrace();
        }

        bookMap.put(ptr, book);
        return book;
    }
}
