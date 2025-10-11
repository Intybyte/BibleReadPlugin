package me.vaan.bibleread.api.data.chapter.content.support;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class VerseFootnoteReference {
    @SerializedName("noteId")
    private int noteId;

    @Override
    public String toString() {
        return " (" + noteId + ") ";
    }
}
