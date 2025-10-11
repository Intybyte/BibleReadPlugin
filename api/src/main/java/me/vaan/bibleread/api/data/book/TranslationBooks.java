package me.vaan.bibleread.api.data.book;

import lombok.Getter;
import me.vaan.bibleread.api.data.translation.Translation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class TranslationBooks {
    private Translation translation;

    private List<TranslationBook> books;

    // id -> book map
    private transient Map<String, TranslationBook> bookMap = new ConcurrentHashMap<>();

    public Map<String, TranslationBook> getBookMap() {
        if (bookMap.isEmpty()) {
            books.forEach(book -> bookMap.put(book.getId(), book));
        }

        return bookMap;
    }
}
