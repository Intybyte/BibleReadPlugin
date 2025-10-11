package me.vaan.bibleread.api.data.chapter.content;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class ChapterHeading extends ChapterContent {
    private List<String> content;

    @Override
    public List<String> getParsed() {
        ArrayList<String> list = new ArrayList<>(content.size() + 2);
        list.add("\n");
        list.addAll(content);
        list.add("\n\n");
        return list;
    }
}
