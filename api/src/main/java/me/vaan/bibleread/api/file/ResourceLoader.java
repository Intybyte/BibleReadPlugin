package me.vaan.bibleread.api.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceLoader {
    /**
     * Saves a resource from the JAR to the specified directory.
     *
     * @param destinationDirectory Directory to save the file into.
     * @param resourceName         Name of the resource in the JAR (e.g., "translation.json").
     * @param clazz                Class to load the resource from (usually the caller class).
     */
    public static void saveAll(File destinationDirectory, String resourceName, Class<?> clazz) {
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs();
        }

        try (InputStream in = clazz.getResourceAsStream("/" + resourceName)) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found in JAR: " + resourceName);
            }

            File outFile = new File(destinationDirectory, resourceName);
            Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to save resource: " + resourceName);
        }
    }

}
