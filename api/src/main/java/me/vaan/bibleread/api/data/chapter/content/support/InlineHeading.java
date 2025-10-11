package me.vaan.bibleread.api.data.chapter.content.support;

import lombok.Getter;

@Getter
public class InlineHeading {
    private String heading;

    @Override
    public String toString() {
        return "\n" + heading + "\n\n";
    }
}
