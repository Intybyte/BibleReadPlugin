package me.vaan.bibleread.api.data.chapter.content.support;

import lombok.Getter;

@Getter
public class InlineLineBreak {
    private boolean lineBreak = true;

    @Override
    public String toString() {
        return "\n";
    }
}
