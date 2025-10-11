package me.vaan.bibleread.bukkit.data;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class ChapterPointer {
    private String translationId;
    private String bookId;
    private int chapter;
}
