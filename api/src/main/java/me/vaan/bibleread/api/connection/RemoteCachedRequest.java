package me.vaan.bibleread.api.connection;

import com.google.gson.Gson;
import lombok.Getter;
import me.vaan.bibleread.api.file.FileManager;
import me.vaan.bibleread.api.file.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class RemoteCachedRequest<T> {

    protected static final String ZIP_ENTRY_NAME = "data.json";

    @Getter
    protected CompletableFuture<Optional<T>> data = null;

    protected final Class<T> type;
    protected final String location;
    protected final String zipLocation;
    protected final ReentrantLock lock = new ReentrantLock();

    public RemoteCachedRequest(Class<T> type, String location) {
        this.type = type;
        this.location = location;
        this.zipLocation = FileUtil.replaceFileExtension(location, "zip");
        load();
    }

    public void update() {
        this.data = CompletableFuture.supplyAsync(this::updateInternal, ConnectionHandler.getInstance().getConnectionExecutor());
    }

    protected Optional<T> updateInternal() {

        Gson gson = new Gson();

        try {
            lock.lock();
            URL url = getUrl();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Check HTTP response code
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 400) {
                System.err.println("HTTP Error: " + responseCode);
                connection.disconnect();
                return Optional.empty();
            }

            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                T obtained = gson.fromJson(reader, type);

                File zipFile = getFile(); // will return a .zip file

                try {
                    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {;
                        String json = gson.toJson(obtained);
                        byte[] data = json.getBytes(StandardCharsets.UTF_8);

                        ZipEntry entry = new ZipEntry(ZIP_ENTRY_NAME);

                        zos.putNextEntry(entry);
                        zos.write(data);
                        zos.closeEntry();
                    }
                } catch (IOException zosError) {
                    System.err.println("ZIPPING ERROR, this will prevent caching");
                    zosError.printStackTrace();
                }

                connection.disconnect();
                return Optional.of(obtained);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        } finally {
            lock.unlock();
        }


    }

    public void load() {
        lock.lock();
        try {
            if (this.data != null) {
                return;
            }

            File zipFile = getFile();
            Gson gson = new Gson();

            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equals(ZIP_ENTRY_NAME)) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = zis.read(buffer)) != -1) {
                            baos.write(buffer, 0, read);
                        }

                        String json = baos.toString("UTF-8");
                        T loaded = gson.fromJson(json, type);
                        this.data = CompletableFuture.completedFuture(
                            Optional.of(
                                loaded
                            )
                        );
                    }

                    zis.closeEntry();
                }

            }
        } catch (IOException e) {
            this.data = CompletableFuture.completedFuture(Optional.empty());
        } finally {
            lock.unlock();
        }
    }

    public CompletableFuture<Optional<T>> getOrQueryData() {
        ExecutorService e = ConnectionHandler.getInstance().getConnectionExecutor();
        return data.thenApplyAsync((result) -> {
            if(result.isPresent()) {
                return result;
            }

            // file didn't load, get network config
            return updateInternal();
        }, e);
    }

    public Optional<T> blockingGetOrQueryData() {
        try {
            return getOrQueryData().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return File, might not have been created yet
     * @throws IOException if creating parent directory fails
     */
    public File getFile() throws IOException {
        File translationFile = FileManager.getInstance().getFile(zipLocation);
        if (translationFile.exists() && translationFile.isFile()) {
            return translationFile;
        }

        File parent = translationFile.getParentFile();
        if (parent != null && !parent.exists()) {
            boolean created = parent.mkdirs();
            if (!created) {
                throw new IOException("Failed to create directory: " + parent.getAbsolutePath());
            }
        }

        return translationFile;
    }

    public URL getUrl() throws MalformedURLException {
        return new URL(ConnectionHandler.STRING_URL + "api/" + location);
    }
}
