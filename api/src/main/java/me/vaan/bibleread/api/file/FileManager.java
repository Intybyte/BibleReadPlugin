package me.vaan.bibleread.api.file;

import com.google.gson.Gson;
import lombok.Getter;
import me.vaan.bibleread.api.file.translation.LocaleTranslation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;

public class FileManager {
    @Getter
    private static FileManager instance = null;

    private final File mainDirectory;
    private final LocaleTranslation localeTranslation;

    private FileManager(File mainDirectory) {
        this.mainDirectory = mainDirectory;
        ResourceLoader.saveAll(mainDirectory, "translation.json", FileManager.class);

        LocaleTranslation localeTranslation1;
        try (FileReader stream = new FileReader(getFile("translation.json"))) {
            localeTranslation1 = new Gson().fromJson(stream, LocaleTranslation.class);
        } catch (IOException ignored) {
            localeTranslation1 = null;
        }

        this.localeTranslation = localeTranslation1;
    }


    public static void initialize(File file) {
        if (instance == null) {
            instance = new FileManager(file);
        }
    }

    public Path getPath(String... pathElementrs) {
        return Paths.get(mainDirectory.getAbsolutePath(), pathElementrs);
    }

    public File getFile(Path path) {
        return mainDirectory.toPath().resolve(path).toFile();
    }

    public File getFile(String... pathElements) {
        return getPath(pathElements).toFile();
    }

    public String message(Locale locale, String translationKey) {
        String localeKey = locale.getLanguage() + "_" + locale.getCountry();
        Map<String, String> messageMap = this.localeTranslation.getLocaleMap().get(localeKey);
        if (messageMap == null) {
            return this.localeTranslation.getLocaleMap().get("en_US").get(translationKey);
        }

        String message = messageMap.get(translationKey);
        if (message == null) {
            return this.localeTranslation.getLocaleMap().get("en_US").get(translationKey);
        }

        return message;
    }
}
