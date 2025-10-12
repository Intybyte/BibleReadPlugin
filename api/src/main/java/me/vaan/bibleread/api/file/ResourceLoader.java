package me.vaan.bibleread.api.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceLoader {
    public static void saveAll(File outputDirectory, String startWithTarget, Class<?> main) {
        try {
            CodeSource codeSource = main.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                return;
            }

            URI jarUri = codeSource.getLocation().toURI();
            try (JarFile jarFile = new JarFile(new File(jarUri))) {

                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (!name.startsWith(startWithTarget) || entry.isDirectory()) {
                        continue;
                    }

                    File outFile = new File(outputDirectory, name);

                    if (outFile.exists()) {
                        continue;
                    }

                    saveResourceManually(jarFile, entry, outFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveResourceManually(JarFile jarFile, JarEntry entry, File outFile) throws IOException {
        // Ensure parent directories exist
        File parentDir = outFile.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + parentDir);
        }

        try (InputStream inputStream = jarFile.getInputStream(entry);
             OutputStream outputStream = Files.newOutputStream(outFile.toPath())) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

}
