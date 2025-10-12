package me.vaan.bibleread.bukkit.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.vaan.bibleread.api.access.AccessManager;
import me.vaan.bibleread.api.data.FieldValueExtractor;
import me.vaan.bibleread.api.data.book.TranslationBook;
import me.vaan.bibleread.api.data.book.TranslationBooks;
import me.vaan.bibleread.api.data.chapter.TranslationBookChapter;
import me.vaan.bibleread.api.data.translation.AvailableTranslations;
import me.vaan.bibleread.api.data.translation.Translation;
import me.vaan.bibleread.api.file.FileManager;
import me.vaan.bibleread.api.file.translation.LocaleHolder;
import me.vaan.bibleread.bukkit.PluginHolder;
import me.vaan.bibleread.bukkit.parser.BookParser;
import me.vaan.bibleread.api.data.access.PlayerDataManager;
import me.vaan.bibleread.api.data.access.ChapterPointer;
import me.vaan.bibleread.api.data.access.TranslationBookPair;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@CommandAlias("bibleread")
@Description("Main bibleread command")
public class MainCommand extends BaseCommand {

    @Subcommand("language")
    public class LanguageCommand extends BaseCommand {

        @Subcommand("getBooks")
        @CommandCompletion("@languages")
        public void getBooks(Player player, String language) {
            LocaleHolder holder = new LocaleHolder(player.getLocale(), player::sendMessage);
            Map<String, List<Translation>> languageMap = AccessManager.getInstance().getTranslations().getLanguageMap();
            List<Translation> tls = languageMap.get(language);
            if (tls == null) {
                holder.sendMessage("invalid_language");
                return;
            }

            String message = tls.stream().map(Translation::getId).collect(Collectors.joining(", "));
            player.sendMessage(message);
        }
    }

    @Subcommand("translation")
    public class TranslationCommand extends BaseCommand {

        @Subcommand("info")
        public void info(Player player) {
            LocaleHolder holder = new LocaleHolder(player.getLocale(), player::sendMessage);

            TranslationBookPair pair = PlayerDataManager.getData(player.getUniqueId());
            String translationKey = pair.getTranslationId();

            if (translationKey == null) {
                holder.sendMessage("invalid_book_translation_pair");
                return;
            }

            Executor mainExecutor = command -> Bukkit.getScheduler().runTask(PluginHolder.getInstance(), command);
            AccessManager.getInstance().getTranslations().getOrQueryData().thenAcceptAsync((translationBooksOptional) -> {
                if (!translationBooksOptional.isPresent()) {
                    holder.sendMessage("network_error_not_found");
                    return;
                }

                AvailableTranslations availableTranslations = translationBooksOptional.get();

                Translation translation = availableTranslations.getTranslationMap().get(translationKey);

                Map<String, String> fieldMap = FieldValueExtractor.getFieldStringValues(translation, "availableFormats", "listOfBooksApiLink");

                holder.sendMessage("successful");
                fieldMap.forEach((f, v) -> holder.sendMessage("content_mapper", f, v));

            }, mainExecutor);
        }

        @Subcommand("set")
        @CommandCompletion("@translations")
        public void set(Player player, String translation) {
            LocaleHolder holder = new LocaleHolder(player.getLocale(), player::sendMessage);
            boolean valid = AccessManager.getInstance().getTranslations().getTranslationIds().contains(translation);
            if (!valid) {
                holder.sendMessage("invalid_translation");
                return;
            }

            PlayerDataManager.setTranslationBook(player.getUniqueId(), translation);
            holder.sendMessage("successful");
        }
    }


    @Subcommand("book")
    public class BookCommand extends BaseCommand {

        @Subcommand("info")
        public void info(Player player) {
            LocaleHolder holder = new LocaleHolder(player.getLocale(), player::sendMessage);

            TranslationBookPair pair = PlayerDataManager.getData(player.getUniqueId());
            String translation = pair.getTranslationId();
            String bookId = pair.getBookId();

            if (!pair.isValid()) {
                holder.sendMessage("invalid_book_translation_pair");
                return;
            }

            Executor mainExecutor = command -> Bukkit.getScheduler().runTask(PluginHolder.getInstance(), command);
            AccessManager.getInstance().getTranslationBooks(translation).thenAcceptAsync((translationBooksOptional) -> {
                if (!translationBooksOptional.isPresent()) {
                    holder.sendMessage("network_error_not_found");
                    return;
                }

                TranslationBooks books = translationBooksOptional.get();
                TranslationBook book = books.getBookMap().get(bookId);
                if (book == null) {
                    holder.sendMessage("network_error_not_found");
                    return;
                }

                Map<String, String> fieldMap = FieldValueExtractor.getFieldStringValues(book);
                holder.sendMessage("successful");
                fieldMap.forEach((f, v) -> holder.sendMessage("content_mapper", f, v));

            }, mainExecutor);
        }

        @Subcommand("set")
        @CommandCompletion("@books")
        public void set(Player player, String bookId) {
            LocaleHolder holder = new LocaleHolder(player.getLocale(), player::sendMessage);

            TranslationBookPair pair = PlayerDataManager.getData(player.getUniqueId());
            String translation = pair.getTranslationId();
            if (translation == null) {
                holder.sendMessage("invalid_translation");
                return;
            }

            Executor mainExecutor = command -> Bukkit.getScheduler().runTask(PluginHolder.getInstance(), command);
            AccessManager.getInstance().getTranslationBooks(translation).thenAcceptAsync((translationBooksOptional) -> {
                if (!translationBooksOptional.isPresent()) {
                    holder.sendMessage("invalid_book");
                    return;
                }

                if (!translationBooksOptional.get().getBookMap().containsKey(bookId)) {
                    holder.sendMessage("network_error_not_found");
                    return;
                }

                PlayerDataManager.setBookId(player.getUniqueId(), bookId);
                holder.sendMessage("successful");

            }, mainExecutor);
        }
    }

    @Subcommand("chapter")
    public class ChapterCommand extends BaseCommand {

        @Subcommand("openBook")
        public void openBook(Player player, int chapterId) {
            LocaleHolder holder = new LocaleHolder(player.getLocale(), player::sendMessage);
            TranslationBookPair pair = PlayerDataManager.getData(player.getUniqueId());
            if (!pair.isValid()) {
                holder.sendMessage("invalid_book_translation_pair");
                return;
            }

            Executor mainExecutor = command -> Bukkit.getScheduler().runTask(PluginHolder.getInstance(), command);
            AccessManager.getInstance().getChapter(pair.getTranslationId(), pair.getBookId(), chapterId).thenAcceptAsync( (chapterOptional) -> {
                if (!chapterOptional.isPresent()) {
                    holder.sendMessage("network_error_not_found");
                    return;
                }

                TranslationBookChapter chapter = chapterOptional.get();


                ItemStack book = getChapterBook(pair, chapter, player.getLocale());
                player.openBook(book);
            }, mainExecutor);
        }

        @Subcommand("getBook")
        @CommandPermission("biblereadplugin.getbook")
        public void getBook(Player player, int chapterId) {
            LocaleHolder holder = new LocaleHolder(player.getLocale(), player::sendMessage);
            TranslationBookPair pair = PlayerDataManager.getData(player.getUniqueId());

            if (!pair.isValid()) {
                holder.sendMessage("invalid_book_translation_pair");
                return;
            }

            Executor mainExecutor = command -> Bukkit.getScheduler().runTask(PluginHolder.getInstance(), command);
            AccessManager.getInstance().getChapter(pair.getTranslationId(), pair.getBookId(), chapterId).thenAcceptAsync( (chapterOptional) -> {
                if (!chapterOptional.isPresent()) {
                    holder.sendMessage("network_error_not_found");
                    return;
                }

                TranslationBookChapter chapter = chapterOptional.get();


                ItemStack book = getChapterBook(pair, chapter, player.getLocale());
                player.getInventory().addItem(book);
            }, mainExecutor);
        }
    }


    @Subcommand("update")
    @CommandPermission("PluginHolder.update")
    public class Update extends BaseCommand {

        @Subcommand("data")
        private void data(Player player) {
            LocaleHolder holder = new LocaleHolder(player.getLocale(), player::sendMessage);
            holder.sendMessage("successful");
            AccessManager.getInstance().updateAll();
        }
    }

    private static final HashMap<ChapterPointer, ItemStack> bookMap = new HashMap<>();
    private static ItemStack getChapterBook(TranslationBookPair pair, TranslationBookChapter chapter, String locale) {
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

            FileManager fmg = FileManager.getInstance();
            int totalChapters = chapter.getBook().getNumberOfChapters();
            String currChapter = String.format(
                fmg.message(locale, "curr_chapter"),
                chpNumber
            );

            TextComponent current = new TextComponent(currChapter + "\n");
            current.setBold(true);
            BaseComponent[] nextPages = new BaseComponent[] {
                current,
                new TextComponent(),
                new TextComponent()
            };

            if (chpNumber < totalChapters) {
                TextComponent clickableText = getChapterComponent(locale, "next_chapter", chpNumber - 1);
                nextPages[1] = clickableText;
            }

            if (1 < chpNumber) {
                TextComponent clickableText = getChapterComponent(locale, "prev_chapter", chpNumber + 1);
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

    private static TextComponent getChapterComponent(String locale, String key, int updateChapter) {
        FileManager fmg = FileManager.getInstance();
        String chapterMessage = fmg.message(locale, key);
        String tooltipMessage = fmg.message(locale, key + "_tooltip");

        TextComponent clickableText = new TextComponent(chapterMessage + "\n");
        clickableText.setBold(true);
        clickableText.setHoverEvent(
            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(tooltipMessage))
        );
        clickableText.setClickEvent(
            new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/bibleread chapter openBook " + updateChapter
            )
        );
        return clickableText;
    }
}
