package connection;

import me.vaan.bibleread.api.access.AccessManager;
import me.vaan.bibleread.api.connection.ConnectionHandler;
import me.vaan.bibleread.api.file.FileManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class TestConnectionHandler {

    @Test
    void init(@TempDir Path path) {
        File file = path.toFile();

        FileManager.initialize(file);
        ConnectionHandler.initialize();

        ExecutorService service = ConnectionHandler.getInstance().getConnectionExecutor();
        if (service instanceof ThreadPoolExecutor) {
            int cores = ((ThreadPoolExecutor) service).getCorePoolSize();
            System.out.println("Pool size: " + cores);
        }
    }
}
