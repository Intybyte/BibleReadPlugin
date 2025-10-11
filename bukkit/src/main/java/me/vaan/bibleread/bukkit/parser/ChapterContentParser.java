package me.vaan.bibleread.bukkit.parser;

import me.vaan.bibleread.api.data.chapter.ChapterData;
import me.vaan.bibleread.api.data.chapter.TranslationBookChapter;
import me.vaan.bibleread.api.data.chapter.content.ChapterContent;
import me.vaan.bibleread.api.data.chapter.content.ChapterFootnote;
import me.vaan.bibleread.api.data.chapter.content.ChapterHeading;
import me.vaan.bibleread.api.data.chapter.content.ChapterVerse;

import java.util.ArrayList;
import java.util.List;

public class ChapterContentParser {
    public static List<String> parse(TranslationBookChapter chapter) {
        ChapterData data = chapter.getChapter();
        ArrayList<String> output = new ArrayList<>(1024);
        int progressive = 1;

        for (ChapterContent ctn : data.getContent()) {
            if (ctn instanceof ChapterHeading) {
                output.add("§l");
                output.addAll(ctn.getParsed());
                output.add("§r");
            } else if (ctn instanceof ChapterVerse) {
                output.add("§r[" + progressive++ + "]");
                output.addAll(ctn.getParsed());
            } else {
                output.addAll(ctn.getParsed());
            }
        }

        output.add("\n\n\n");
        for (ChapterFootnote footnote : data.getFootnotes()) {
            output.add(footnote.toString() + "\n");
        }

        return output;
    }
}
