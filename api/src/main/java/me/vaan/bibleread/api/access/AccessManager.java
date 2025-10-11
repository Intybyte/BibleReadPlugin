package me.vaan.bibleread.api.access;

import lombok.Getter;
import me.vaan.bibleread.api.connection.AvailableTranslationsRequest;
import me.vaan.bibleread.api.connection.ConnectionHandler;
import me.vaan.bibleread.api.connection.RemoteCachedRequest;
import me.vaan.bibleread.api.data.book.TranslationBook;
import me.vaan.bibleread.api.data.book.TranslationBooks;
import me.vaan.bibleread.api.data.chapter.TranslationBookChapter;
import me.vaan.bibleread.api.data.translation.AvailableTranslations;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AccessManager {
    @Getter
    private static AccessManager instance = null;

    @Getter
    private final AvailableTranslationsRequest translations = new AvailableTranslationsRequest("available_translations.json");

    // translation -> books.json
    private final Map<String, RemoteCachedRequest<TranslationBooks>> translationBooks = new ConcurrentHashMap<>();

    // translation -> book -> chapter -> data
    private final Map<String, Map<String, Map<Integer, RemoteCachedRequest<TranslationBookChapter>>>> translationBookChapterMapping = new ConcurrentHashMap<>();

    public static void initialize() {
        instance = new AccessManager();
        ConnectionHandler.getInstance().getConnectionExecutor().execute(AccessManager.getInstance()::updateTranslations);
    }

    private AccessManager() {}

    public void updateTranslations() {
        translations.update();
    }

    // crazy expensive call
    public void updateAll() {
        translations.update();
        translationBooks.values().forEach(RemoteCachedRequest::update);

        translationBookChapterMapping.values().forEach( tl ->
            tl.values().forEach( book ->
                book.values().forEach(
                    RemoteCachedRequest::update
                )
            )
        );
    }

    public CompletableFuture<Optional<TranslationBooks>> getTranslationBooks(String translation) {
        translationBooks.putIfAbsent(translation, new RemoteCachedRequest<>(
                TranslationBooks.class,
                translation + "/books.json"
            )
        );

        RemoteCachedRequest<TranslationBooks> obtained = translationBooks.get(translation);
        return obtained.getOrQueryData();
    }

    /**
     * @return chapter amounts of a book
     */
    public CompletableFuture<Optional<Integer>> getBookSize(String translation, String bookId) {
        return getTranslationBooks(translation).thenApplyAsync(books -> {
           if (books.isPresent()) {
               TranslationBook book = books.get().getBookMap().get(bookId);
               return Optional.of(book.getNumberOfChapters());
           }

           return Optional.empty();
        }, ConnectionHandler.getInstance().getConnectionExecutor());
    }

    public CompletableFuture<Optional<TranslationBookChapter>> getChapter(String translation, String bookId, int chapterId) {
        CompletableFuture<Optional<Integer>> bookSize = getBookSize(translation, bookId);

        // Ensure translationBookChapterMapping has the translation key
        translationBookChapterMapping.putIfAbsent(translation, new ConcurrentHashMap<>());
        Map<String, Map<Integer, RemoteCachedRequest<TranslationBookChapter>>> bookMap = translationBookChapterMapping.get(translation);

        // Ensure there's a chapter map for the book
        bookMap.putIfAbsent(bookId, new ConcurrentHashMap<>());
        Map<Integer, RemoteCachedRequest<TranslationBookChapter>> chapterMap = bookMap.get(bookId);

        // Get or create the RemoteCachedRequest for the chapter
        return chapterMap.computeIfAbsent(chapterId, id ->
            new RemoteCachedRequest<>(
                TranslationBookChapter.class,
                String.format("%s/%s/%d.json", translation, bookId, id)
            )
        ).getOrQueryData().thenCombineAsync(bookSize, (chapter, size) -> {
            if (!size.isPresent()) {
                // If we can't determine the book size, reject the chapter to be safe
                return Optional.empty();
            }

            int maxChapter = size.get();
            if (maxChapter < chapterId) {
                // Invalid chapter ID
                return Optional.empty();
            }

            return chapter;
        }, ConnectionHandler.getInstance().getConnectionExecutor());
    }
}
