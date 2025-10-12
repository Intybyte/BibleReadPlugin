package me.vaan.bibleread.api.file.translation;

@FunctionalInterface
public interface MessageReceiver {
    void sendMessage(String key, Object... arguments);
}
