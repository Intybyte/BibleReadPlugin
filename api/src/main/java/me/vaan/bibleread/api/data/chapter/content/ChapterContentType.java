package me.vaan.bibleread.api.data.chapter.content;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public enum ChapterContentType {
    @SerializedName("heading")
    HEADING(ChapterHeading.class),

    @SerializedName("line_break")
    LINE_BREAK(ChapterLineBreak.class),

    @SerializedName("verse")
    VERSE(ChapterVerse.class),

    @SerializedName("hebrew_subtitle")
    HEBREW_SUBTITLE(ChapterHebrewSubtitle.class);

    private final transient Class<? extends ChapterContent> chapterClass;

    ChapterContentType(Class<? extends ChapterContent> clazz) {
        this.chapterClass = clazz;
    }
}
