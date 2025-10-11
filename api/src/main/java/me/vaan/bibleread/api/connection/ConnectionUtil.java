package me.vaan.bibleread.api.connection;

import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectionUtil {
    /**
     * Measures average latency (in ms) for a given URL over multiple requests.
     * @return average latency in milliseconds, or -1 if all attempts fail
     */
    public static long measureAverageLatency(String urlString) {
        long latency = -1;

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD"); // faster than GET, just to measure latency
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);

            long start = System.nanoTime();
            int responseCode = connection.getResponseCode();
            long durationMs = (System.nanoTime() - start) / 1_000_000;

            if (responseCode >= 200 && responseCode < 400) { // treat 2xx and 3xx as success
                latency = durationMs;
            }
            connection.disconnect();
        } catch (Exception ignored) {}


        return latency;
    }

    /**
     * Calculates thread pool size based on CPU cores and average wait time (latency).
     * Uses a simplified formula ignoring compute time.
     *
     * @param avgWaitTimeMs average network latency in milliseconds
     * @return recommended thread pool size
     */
    public static int calculateThreadPoolSize(long avgWaitTimeMs) {
        final int cpuCores = Runtime.getRuntime().availableProcessors();
        final int minPoolSize = cpuCores * 2;
        final int maxPoolSize = 100;

        if (avgWaitTimeMs <= 0) {
            return minPoolSize;
        }

        double waitToComputeRatio = Math.log10(avgWaitTimeMs);

        int estimatedSize = (int) Math.ceil(cpuCores * (1 + waitToComputeRatio));

        // Clamp to bounds
        return Math.max(minPoolSize, Math.min(estimatedSize, maxPoolSize));
    }
}
