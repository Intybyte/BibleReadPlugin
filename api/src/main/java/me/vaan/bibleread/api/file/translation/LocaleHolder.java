package me.vaan.bibleread.api.file.translation;

import me.vaan.bibleread.api.file.FileManager;

import java.util.Locale;
import java.util.function.Consumer;

public class LocaleHolder implements MessageReceiver {
    private final String locale;
    private final Consumer<String> messageSender;

    public LocaleHolder(String locale, Consumer<String> messageSender) {
        this.locale = locale;
        this.messageSender = messageSender;
    }

    public LocaleHolder(Locale locale, Consumer<String> messageSender) {
        this(locale.getLanguage() + "_" + locale.getCountry(), messageSender);
    }

    public void sendMessage(String key, Object... arguments) {
        String message = FileManager.getInstance().message(this.locale, key);
        String parsed = String.format(message, arguments);
        messageSender.accept(parsed);
    }
}
