package me.vaan.bibleread.api.data.translation;

public class MissingAvailableTranslations extends RuntimeException {
    public MissingAvailableTranslations() {
        super("Missing available translations");
    }
}
