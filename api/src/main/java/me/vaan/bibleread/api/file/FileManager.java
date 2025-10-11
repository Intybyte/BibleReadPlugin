package me.vaan.bibleread.api.file;

import lombok.Getter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {
    @Getter
    private static FileManager instance = null;

    private final File mainDirectory;

    private FileManager(File mainDirectory) {
        this.mainDirectory = mainDirectory;
    }


    public static void initialize(File file) {
        if (instance == null) {
            instance = new FileManager(file);
        }
    }

    public File getFile(Path path) {
        return mainDirectory.toPath().resolve(path).toFile();
    }

    public File getFile(String... pathElements) {
        return Paths.get(mainDirectory.getAbsolutePath(), pathElements).toFile();
    }
}
