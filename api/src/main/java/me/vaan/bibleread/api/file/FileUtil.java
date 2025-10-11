package me.vaan.bibleread.api.file;

public class FileUtil {
    public static String replaceFileExtension(String name, String toExt) {
        int dotIndex = name.lastIndexOf('.');

        String baseName = (dotIndex != -1) ? name.substring(0, dotIndex) : name;

        return baseName + '.' + toExt;
    }
}
