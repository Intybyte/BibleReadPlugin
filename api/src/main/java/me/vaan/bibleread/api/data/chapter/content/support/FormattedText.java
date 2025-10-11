package me.vaan.bibleread.api.data.chapter.content.support;

import lombok.Getter;

@Getter
public class FormattedText {
    private String text;
    private Integer poem;             // Optional
    private Boolean wordsOfJesus;     // Optional

    @Override
    public String toString() {
        return text;
    }
}
