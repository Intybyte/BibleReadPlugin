package me.vaan.bibleread.api.data.chapter;

import lombok.Getter;
import me.vaan.bibleread.api.data.book.TranslationBook;
import me.vaan.bibleread.api.data.translation.Translation;

@Getter
public class TranslationBookChapter {
    private Translation translation;

    private TranslationBook book;

    private String thisChapterApiLink;

    // idc about audio links
    // TranslationBookChapterAudioLinks thisChapterAudioLinks

    private String nextChapterApiLink;

    // idc about audio links pt 2
    // TranslationBookChapterAudioLinks nextChapterAudioLinks

    private String previousChapterApiLink;

    // idc about audio links pt 3
    // TranslationBookChapterAudioLinks previousChapterAudioLinks

    private int numberOfVerses;

    private ChapterData chapter;
}
