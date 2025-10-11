package me.vaan.bibleread.api.data.chapter.content;

import java.util.Collections;
import java.util.List;

public class ChapterLineBreak extends ChapterContent {
    @Override
    public List<String> getParsed() {
        return Collections.singletonList("\n");
    }
}
