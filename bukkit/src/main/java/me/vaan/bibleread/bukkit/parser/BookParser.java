package me.vaan.bibleread.bukkit.parser;

import me.vaan.bibleread.api.data.chapter.TranslationBookChapter;
import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BookParser {
    public static class Result {
        public final HashMap<Integer, String> finalPages;
        public final int pageNumber;

        public Result(HashMap<Integer, String> finalPages, int pageNumber) {
            this.finalPages = finalPages;
            this.pageNumber = pageNumber;
        }
    }

    public static Result getParsed(TranslationBookChapter chapter) {
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
        Result result = new Result(finalPages, pageNumber);
        return result;
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
