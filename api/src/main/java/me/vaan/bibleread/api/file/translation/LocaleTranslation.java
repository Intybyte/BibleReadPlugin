package me.vaan.bibleread.api.file.translation;

import lombok.Getter;

import java.util.Map;

@Getter
public class LocaleTranslation {
    private Map<String, Map<String, String>> localeMap;
}
