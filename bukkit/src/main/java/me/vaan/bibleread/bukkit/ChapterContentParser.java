package me.vaan.bibleread.bukkit;

import me.vaan.bibleread.api.data.chapter.ChapterData;
import me.vaan.bibleread.api.data.chapter.content.ChapterContent;
import me.vaan.bibleread.api.data.chapter.content.ChapterHeading;
import me.vaan.bibleread.api.data.chapter.content.ChapterLineBreak;
import me.vaan.bibleread.api.data.chapter.content.ChapterVerse;

import java.util.ArrayList;
import java.util.List;

public class ChapterContentParser {
    public static List<String> parse(ChapterData data) {
        ArrayList<String> output = new ArrayList<>(1024);

        for (ChapterContent ctn : data.getContent()) {
            if (ctn instanceof ChapterHeading) {
                output.add("§l");
                output.addAll(ctn.getParsed());
            } else if (ctn instanceof ChapterVerse) {
                output.add("§r");
                output.addAll(ctn.getParsed());
            } else {
                output.addAll(ctn.getParsed());
            }
        }

        return output;
    }
}
