package me.vaan.bibleread.api.data.chapter;

import lombok.Getter;
import me.vaan.bibleread.api.data.chapter.content.ChapterContent;
import me.vaan.bibleread.api.data.chapter.content.ChapterFootnote;

import java.util.List;

@Getter
public class ChapterData {
    private int number;

    private List<ChapterContent> content;

    private List<ChapterFootnote> footnotes;
}
