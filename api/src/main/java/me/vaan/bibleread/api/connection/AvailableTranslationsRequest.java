package me.vaan.bibleread.api.connection;

import me.vaan.bibleread.api.data.translation.AvailableTranslations;
import me.vaan.bibleread.api.data.translation.Translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AvailableTranslationsRequest extends RemoteCachedRequest<AvailableTranslations> {
    protected final Set<String> translationIds = new HashSet<>();
    protected final Map<String, List<Translation>> languageMap = new HashMap<>();
    
    // translation -> book -> chapterSize
    public AvailableTranslationsRequest(String location) {
        super(AvailableTranslations.class, location);
    }

    public Set<String> getTranslationIds() {
        if (!translationIds.isEmpty()) {
            return translationIds;
        }

        Optional<AvailableTranslations> obtained = this.blockingGetOrQueryData();
        if (!obtained.isPresent()) {
            return translationIds;
        }

        translationIds.addAll(obtained.get().getTranslationMap().keySet());
        return translationIds;
    }

    public Map<String, List<Translation>> getLanguageMap() {
        if (!languageMap.isEmpty()) {
            return languageMap;
        }

        Optional<AvailableTranslations> obtained = this.blockingGetOrQueryData();
        if (!obtained.isPresent()) {
            return languageMap;
        }

        AvailableTranslations tls = obtained.get();
        for (Translation tl : tls.getTranslations()) {
            String languageName = tl.getLanguageName();
            languageMap.putIfAbsent(languageName, new ArrayList<>());
            languageMap.get(languageName).add(tl);
        }

        return languageMap;
    }

    @Override
    public void update() {
        translationIds.clear();
        languageMap.clear();
        super.update();
    }
}
