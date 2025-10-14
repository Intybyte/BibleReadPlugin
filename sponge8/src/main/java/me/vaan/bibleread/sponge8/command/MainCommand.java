package me.vaan.bibleread.sponge8.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.vaan.bibleread.api.access.AccessManager;
import me.vaan.bibleread.api.data.FieldValueExtractor;
import me.vaan.bibleread.api.data.access.ChapterPointer;
import me.vaan.bibleread.api.data.access.PlayerDataManager;
import me.vaan.bibleread.api.data.access.TranslationBookPair;
import me.vaan.bibleread.api.data.book.TranslationBook;
import me.vaan.bibleread.api.data.book.TranslationBooks;
import me.vaan.bibleread.api.data.chapter.TranslationBookChapter;
import me.vaan.bibleread.api.data.translation.AvailableTranslations;
import me.vaan.bibleread.api.data.translation.Translation;
import me.vaan.bibleread.api.file.FileManager;
import me.vaan.bibleread.api.parser.BookParser;
import me.vaan.bibleread.sponge8.BibleReadPlugin;
import me.vaan.bibleread.sponge8.i18n.PlayerLocale;
import me.vaan.bibleread.sponge8.parser.SpongeBookParser;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommandAlias("bibleread")
@Description("Main bibleread command")
public class MainCommand extends BaseCommand {

    @Subcommand("language")
    public class LanguageCommand extends BaseCommand {

        @Subcommand("getTranslations")
        @CommandCompletion("@languages")
        public void getBooks(ServerPlayer player, String language) {
            PlayerLocale holder = new PlayerLocale(player);
            Map<String, List<Translation>> languageMap = AccessManager.getInstance().getTranslations().getLanguageMap();
            List<Translation> tls = languageMap.get(language);
            if (tls == null) {
                holder.sendMessage("invalid_language");
                return;
            }

            String message = tls.stream().map(Translation::getId).collect(Collectors.joining(", "));
            player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
        }
    }

    @Subcommand("translation")
    public class TranslationCommand extends BaseCommand {

        @Subcommand("info")
        public void info(ServerPlayer player) {
            PlayerLocale holder = new PlayerLocale(player);

            TranslationBookPair pair = PlayerDataManager.getData(player.uniqueId());
            String translationKey = pair.getTranslationId();

            if (translationKey == null) {
                holder.sendMessage("invalid_book_translation_pair");
                return;
            }

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

            }, BibleReadPlugin.getInstance().getMainExecutor());
        }

        @Subcommand("set")
        @CommandCompletion("@translations")
        public void set(ServerPlayer player, String translation) {
            PlayerLocale holder = new PlayerLocale(player);
            boolean valid = AccessManager.getInstance().getTranslations().getTranslationIds().contains(translation);
            if (!valid) {
                holder.sendMessage("invalid_translation");
                return;
            }

            PlayerDataManager.setTranslationBook(player.uniqueId(), translation);
            holder.sendMessage("successful");
        }
    }


    @Subcommand("book")
    public class BookCommand extends BaseCommand {

        @Subcommand("info")
        public void info(ServerPlayer player) {
            PlayerLocale holder = new PlayerLocale(player);

            TranslationBookPair pair = PlayerDataManager.getData(player.uniqueId());
            String translation = pair.getTranslationId();
            String bookId = pair.getBookId();

            if (!pair.isValid()) {
                holder.sendMessage("invalid_book_translation_pair");
                return;
            }

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

            }, BibleReadPlugin.getInstance().getMainExecutor());
        }

        @Subcommand("set")
        @CommandCompletion("@books")
        public void set(ServerPlayer player, String bookId) {
            PlayerLocale holder = new PlayerLocale(player);

            TranslationBookPair pair = PlayerDataManager.getData(player.uniqueId());
            String translation = pair.getTranslationId();
            if (translation == null) {
                holder.sendMessage("invalid_translation");
                return;
            }

            AccessManager.getInstance().getTranslationBooks(translation).thenAcceptAsync((translationBooksOptional) -> {
                if (!translationBooksOptional.isPresent()) {
                    holder.sendMessage("invalid_book");
                    return;
                }

                if (!translationBooksOptional.get().getBookMap().containsKey(bookId)) {
                    holder.sendMessage("network_error_not_found");
                    return;
                }

                PlayerDataManager.setBookId(player.uniqueId(), bookId);
                holder.sendMessage("successful");

            }, BibleReadPlugin.getInstance().getMainExecutor());
        }
    }

    @Subcommand("chapter")
    public class ChapterCommand extends BaseCommand {

        @Subcommand("openBook")
        public void openBook(ServerPlayer player, int chapterId) {
            PlayerLocale holder = new PlayerLocale(player);
            TranslationBookPair pair = PlayerDataManager.getData(player.uniqueId());
            if (!pair.isValid()) {
                holder.sendMessage("invalid_book_translation_pair");
                return;
            }

            AccessManager.getInstance().getChapter(pair.getTranslationId(), pair.getBookId(), chapterId).thenAcceptAsync( (chapterOptional) -> {
                if (!chapterOptional.isPresent()) {
                    holder.sendMessage("network_error_not_found");
                    return;
                }

                TranslationBookChapter chapter = chapterOptional.get();


                ItemStack bookItem = getChapterBook(pair, chapter, player.locale().toString());
                Book book = Book.book(
                    bookItem.require(Keys.DISPLAY_NAME),
                    bookItem.require(Keys.AUTHOR),
                    bookItem.require(Keys.PAGES)
                );
                player.openBook(book);
            }, BibleReadPlugin.getInstance().getMainExecutor());
        }

        @Subcommand("getBook")
        @CommandPermission("biblereadplugin.getbook")
        public void getBook(ServerPlayer player, int chapterId) {
            PlayerLocale holder = new PlayerLocale(player);
            TranslationBookPair pair = PlayerDataManager.getData(player.uniqueId());

            if (!pair.isValid()) {
                holder.sendMessage("invalid_book_translation_pair");
                return;
            }

            AccessManager.getInstance().getChapter(pair.getTranslationId(), pair.getBookId(), chapterId).thenAcceptAsync( (chapterOptional) -> {
                if (!chapterOptional.isPresent()) {
                    holder.sendMessage("network_error_not_found");
                    return;
                }

                TranslationBookChapter chapter = chapterOptional.get();


                ItemStack book = getChapterBook(pair, chapter, player.locale().toString());
                player.inventory().offer(book);
            }, BibleReadPlugin.getInstance().getMainExecutor());
        }
    }


    @Subcommand("update")
    @CommandPermission("PluginHolder.update")
    public class Update extends BaseCommand {

        @Subcommand("data")
        private void data(ServerPlayer player) {
            PlayerLocale holder = new PlayerLocale(player);
            holder.sendMessage("successful");
            AccessManager.getInstance().updateAll();
        }
    }

    private static final HashMap<ChapterPointer, ItemStack> bookMap = new HashMap<>();
    private static final SpongeBookParser bookParser = new SpongeBookParser();
    private static ItemStack getChapterBook(TranslationBookPair pair, TranslationBookChapter chapter, String locale) {
        ItemStack book = ItemStack.of(ItemTypes.WRITTEN_BOOK);
        int chpNumber = chapter.getChapter().getNumber();
        ChapterPointer ptr = new ChapterPointer(pair.getTranslationId(), pair.getBookId(), chpNumber);
        if (bookMap.containsKey(ptr)) {
            return bookMap.get(ptr);
        }

        book.offer(Keys.DISPLAY_NAME, Component.text(chapter.getBook().getCommonName() + " " + chpNumber));
        book.offer(Keys.AUTHOR, Component.text("Unknown"));
        book.offer(Keys.GENERATION, 3);


        try {

            BookParser.Result result = bookParser.getParsed(chapter);

            List<Component> components = new ArrayList<>();
            LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
            // Add pages to book
            for (int i = 0; i <= result.pageNumber; i++) {
                String page = result.finalPages.getOrDefault(i, "").trim();
                if (page.isEmpty()) page = " ";
                components.add(
                    serializer.deserialize(page)
                );
            }

            FileManager fmg = FileManager.getInstance();
            int totalChapters = chapter.getBook().getNumberOfChapters();
            String currChapter = String.format(
                fmg.message(locale, "curr_chapter"),
                chpNumber
            );

            TextComponent current = serializer.deserialize(currChapter + "\n").style(Style.style().decorate(TextDecoration.BOLD));
            TextComponent[] nextPages = new TextComponent[] {
                current,
                Component.empty(),
                Component.empty()
            };

            if (chpNumber < totalChapters) {
                TextComponent clickableText = getChapterComponent(locale, "next_chapter", chpNumber - 1);
                nextPages[1] = clickableText;
            }

            if (1 < chpNumber) {
                TextComponent clickableText = getChapterComponent(locale, "prev_chapter", chpNumber + 1);
                nextPages[2] = clickableText;
            }

            book.offer(Keys.PAGES, components);
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

        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        TextComponent clickableText = serializer.deserialize(chapterMessage + "\n").style(Style.style().decorate(TextDecoration.BOLD));
        clickableText = clickableText.hoverEvent(
            HoverEvent.showText(serializer.deserialize(tooltipMessage))
        );
        clickableText = clickableText.clickEvent(
            ClickEvent.runCommand(
                "/bibleread chapter openBook " + updateChapter
            )
        );
        return clickableText;
    }
}
