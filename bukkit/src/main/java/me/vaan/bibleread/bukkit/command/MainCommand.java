package me.vaan.bibleread.bukkit.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import lombok.var;
import me.vaan.bibleread.api.access.AccessManager;
import me.vaan.bibleread.api.connection.ConnectionHandler;
import me.vaan.bibleread.api.data.book.TranslationBooks;
import me.vaan.bibleread.api.data.chapter.TranslationBookChapter;
import me.vaan.bibleread.api.data.chapter.content.ChapterContent;
import me.vaan.bibleread.bukkit.BibleReadPlugin;
import me.vaan.bibleread.bukkit.ChapterContentParser;
import me.vaan.bibleread.bukkit.PlayerDataManager;
import me.vaan.bibleread.bukkit.data.ChapterPointer;
import me.vaan.bibleread.bukkit.data.TranslationBookPair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@CommandAlias("bibleread")
@Description("Main bibleread command")
public class MainCommand extends BaseCommand {


    @Subcommand("translation")
    public class Translation extends BaseCommand {

        @Default
        public void help(Player player) {
            player.sendMessage("");
        }

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
    public class Book extends BaseCommand {

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
            ExecutorService e = ConnectionHandler.getInstance().getConnectionExecutor();
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
    public class Chapter extends BaseCommand {

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
    public class Update extends BaseCommand {

        @Subcommand("data")
        private void data(CommandSender sender) {
            AccessManager.getInstance().updateAll();
        }
    }

    @Subcommand("linecount")
    public void count(Player player, int number) {
        ItemStack stack = player.getInventory().getItemInMainHand();
        if (stack.getType().isAir()) return;

        if (stack.getItemMeta() instanceof BookMeta) {
            BookMeta bm = (BookMeta) stack.getItemMeta();
            if (bm.getPageCount() < number) {
                return;
            }

            int lines = lineBookSize(bm.getPage(number));
            player.sendMessage("Lines: " + lines);
        }


    }

    private static final HashMap<ChapterPointer, ItemStack> bookMap = new HashMap<>();
    private static ItemStack getChapterBook(TranslationBookPair pair, TranslationBookChapter chapter) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        int number = chapter.getChapter().getNumber();
        ChapterPointer ptr = new ChapterPointer(pair.getTranslationId(), pair.getBookId(), number);
        if (bookMap.containsKey(ptr)) {
            return bookMap.get(ptr);
        }

        try {

            BookMeta meta = (BookMeta) book.getItemMeta();
            meta.setTitle(chapter.getBook().getCommonName() + " " + number);
            meta.setGeneration(BookMeta.Generation.TATTERED);
            meta.setAuthor("Unknown");

            HashMap<Integer, String> finalPages = new HashMap<>();
            List<String> wordList = new ArrayList<>(2048);
            int pageNumber = 0;

            String collapsed = String.join("", ChapterContentParser.parse(chapter));
            wordList.addAll(
                Arrays.asList(collapsed.split(" "))
            );


            for (String word : wordList) {
                String currentPage = finalPages.getOrDefault(pageNumber, "");

                String combined;
                if (currentPage.isEmpty()) {
                    combined = word;
                } else {
                    combined = currentPage + " " + word;
                }

                int lineCount = lineBookSize(combined);

                if (combined.length() > 1023 || lineCount > 13) {
                    // Doesn't fit on current page — move to next page
                    pageNumber++;
                    finalPages.put(pageNumber, word);
                } else {
                    // Fits, update the current page
                    finalPages.put(pageNumber, combined);
                }
            }

            // Add pages to book
            for (int i = 0; i <= pageNumber; i++) {
                String page = finalPages.getOrDefault(i, "").trim();
                if (page.isEmpty()) page = " ";
                meta.addPage(page);
            }

            book.setItemMeta(meta);
        } catch (Exception e) {
            e.printStackTrace();
        }

        bookMap.put(ptr, book);
        return book;
    }

    private static int lineBookSize(String str) {
        int lineCount = 1;
        int pixelWidth = 0;
        char[] array = str.toCharArray();
        boolean bold = false;

        for (int i = 0; i < str.length(); i++) {
            char c = array[i];
            if (c == '\n') {
                lineCount++;
                pixelWidth = 0;
                continue;
            }

            if (c == '§' && i + 1 < str.length()) {
                char code = str.charAt(++i);

                switch (code) {
                    case 'l': // Bold
                        bold = true;
                        break;
                    case 'r': // Reset
                    case 'o': // Italic
                    case 'n': // Underline
                    case 'm': // Strikethrough
                    case 'k': // Obfuscated
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                    case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                        bold = false; // most formatting resets bold
                        break;
                }
                continue;
            }

            MapFont.CharacterSprite sprite = MinecraftFont.Font.getChar(c);
            int charWidth = sprite != null ? sprite.getWidth() : 6;

            if (bold && charWidth > 0 && c != ' ') {
                charWidth++; // Bold adds 1 pixel width
            }

            pixelWidth += charWidth + 1;
            //  normally it is 114, however it might skip a few words for some reason,
            //  so I am giving it a bit of tolerance
            if (pixelWidth >= 112) {
                lineCount++;
                pixelWidth = charWidth + 1;
            }
        }

        return lineCount;
    }
}
