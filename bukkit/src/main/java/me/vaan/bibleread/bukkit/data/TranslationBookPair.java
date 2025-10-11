package me.vaan.bibleread.bukkit.data;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class TranslationBookPair {
    private String translationId;
    private String bookId;

    public boolean isValid() {
        return translationId != null && bookId != null;
    }
}
