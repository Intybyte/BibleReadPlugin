package me.vaan.bibleread.api.data.chapter.content;

import lombok.Getter;

@Getter
public class ChapterFootnote {
    private int noteId;

    private String text;

    private Reference reference;

    @Getter
    public static class Reference {
        private int chapter;
        private int verse;
    }

    private String caller;

    @Override
    public String toString() {
        String callerOutput;
        if (caller == null) callerOutput = "";
        else if (caller.equals("+")) callerOutput = "(" + noteId + ")" ;
        else callerOutput = caller;

        String referenceOutput;
        if (reference == null) referenceOutput = "";
        else referenceOutput = "[" + reference.chapter + ":" + reference.verse + "]";

        return callerOutput + " " + text + " " + referenceOutput;
    }

    public String toString(String bookName) {
        String callerOutput;
        if (caller == null) callerOutput = "";
        else if (caller.equals("+")) callerOutput = "(" + noteId + ")" ;
        else callerOutput = caller;

        String referenceOutput;
        if (reference == null) referenceOutput = "";
        else referenceOutput = "[" + bookName + " " + reference.chapter + ":" + reference.verse + "]";

        return callerOutput + " " + text + " " + referenceOutput;
    }
}
