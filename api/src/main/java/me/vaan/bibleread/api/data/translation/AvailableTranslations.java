package me.vaan.bibleread.api.data.translation;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class AvailableTranslations {
    private List<Translation> translations;

    private final transient Map<String, Translation> translationMap = new ConcurrentHashMap<>();

    public synchronized Map<String, Translation> getTranslationMap() {
        if (translations == null || translations.isEmpty()) {
            return translationMap; // return empty
        }

        // compute entries
        if (translationMap.isEmpty()) {
            for (Translation tl : translations) {
                translationMap.put(
                    tl.getId(),
                    tl
                );
            }
        }

        return translationMap;
    }
}
