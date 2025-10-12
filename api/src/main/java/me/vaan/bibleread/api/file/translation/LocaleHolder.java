package me.vaan.bibleread.api.file.translation;

import me.vaan.bibleread.api.file.FileManager;

import java.util.Locale;

public class LocaleHolder implements MessageReceiver {
    private final String locale;
    private final MessageReceiver messageSender;

    public LocaleHolder(String locale, MessageReceiver messageSender) {
        this.locale = locale;
        this.messageSender = messageSender;
    }

    public LocaleHolder(Locale locale, MessageReceiver messageSender) {
        this(locale.getLanguage() + "_" + locale.getCountry(), messageSender);
    }

    public void sendMessage(String key) {
        String message = FileManager.getInstance().message(this.locale, key);
        messageSender.sendMessage(message);
    }
}
