package me.vaan.bibleread.api.connection;

import lombok.Getter;
import me.vaan.bibleread.api.access.AccessManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionHandler {
    public static final String STRING_URL = "https://bible.helloao.org/";
    public static final int ATTEMPTS = 5;

    @Getter
    private static ConnectionHandler instance;
    public static void initialize() {
        instance = new ConnectionHandler();
    }

    @Getter
    private volatile ExecutorService connectionExecutor;

    private ConnectionHandler() {
        ExecutorService temporary = Executors.newFixedThreadPool(ATTEMPTS);

        List<CompletableFuture<Long>> latencyScans = new ArrayList<>(ATTEMPTS);
        for (int i = 0; i < ATTEMPTS; i++) {
            latencyScans.add(
                CompletableFuture.supplyAsync( () ->
                    ConnectionUtil.measureAverageLatency(STRING_URL)
                , temporary)
            );
        }

        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(
            latencyScans.toArray(new CompletableFuture[0])
        );

        try {
            allDoneFuture.join();

            long sumLatency = 0;
            int success = 0;

            for (CompletableFuture<Long> future : latencyScans) {
                try {
                    Long latency = future.get();
                    if (latency != -1) {
                        sumLatency += latency;
                        success++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            long totalLatency = (success == 0) ? -1 : sumLatency / success;
            int poolSize = ConnectionUtil.calculateThreadPoolSize(totalLatency);

            this.connectionExecutor = Executors.newFixedThreadPool(poolSize);
            this.connectionExecutor.execute(AccessManager.getInstance()::updateTranslations);
        } catch (Exception e) {
            e.printStackTrace();
            this.connectionExecutor = Executors.newFixedThreadPool(ATTEMPTS); // Fallback
        } finally {
            temporary.shutdown();
        }
    }

    public static void shutdown() {
        instance.connectionExecutor.shutdown();
        instance = null;
    }
}
