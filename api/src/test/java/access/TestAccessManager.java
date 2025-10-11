package access;

import me.vaan.bibleread.api.access.AccessManager;
import me.vaan.bibleread.api.connection.ConnectionHandler;
import me.vaan.bibleread.api.data.book.TranslationBooks;
import me.vaan.bibleread.api.data.chapter.ChapterData;
import me.vaan.bibleread.api.data.chapter.TranslationBookChapter;
import me.vaan.bibleread.api.data.chapter.content.ChapterContent;
import me.vaan.bibleread.api.data.chapter.content.ChapterHeading;
import me.vaan.bibleread.api.data.chapter.content.ChapterVerse;
import me.vaan.bibleread.api.file.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAccessManager {

    @TempDir
    Path tempDir; // <--- field injection allows use in @BeforeEach

    File file;

    @BeforeEach
    void setUp() {
        FileManager.initialize(tempDir.toFile()); // or initFile if needed
        ConnectionHandler.initialize();
        AccessManager.initialize();
    }

    @Test
    void translationBookAmount() throws ExecutionException, InterruptedException {
        AccessManager.getInstance().getTranslationBooks("BSB").thenAccept(it -> {
           if (!it.isPresent()) {
               System.err.println("Not present? Network issue?");
               return;
           }

           TranslationBooks books = it.get();
           assertEquals(66, books.getBooks().size());
        }).get();
    }

    @Test
    void readAChapter() throws ExecutionException, InterruptedException {
        AccessManager.getInstance().getChapter("BSB", "GEN", 1).thenAccept( chapterOptional -> {
            if (!chapterOptional.isPresent()) {
                System.err.println("Not present? Network issue?");
                return;
            }

            TranslationBookChapter chapter = chapterOptional.get();
            ChapterData chpData = chapter.getChapter();
            List<ChapterContent> list = chpData.getContent();
            for (ChapterContent content : list) {
                if (content instanceof ChapterHeading) {
                    System.out.println("Printing header");
                    ((ChapterHeading) content).getContent().forEach(System.out::println);
                } else if (content instanceof ChapterVerse) {
                    System.out.println("Printing verse");
                    ((ChapterVerse) content).getContent().forEach(it -> {
                        System.out.println(it.toString());
                    });
                }
            }

            chpData.getFootnotes().forEach(it -> {
                System.out.println(it.toString("GEN"));
            });

        }).get();
    }
}
