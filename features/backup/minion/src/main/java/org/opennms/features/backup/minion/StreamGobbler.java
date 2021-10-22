package org.opennms.features.backup.minion;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts a thread that reads lines from an input stream, logs them, and stores them in an array list. The list of
 * stored lines is used to search for expected output.
 * <p>
 * Used for consuming {@code stdout} and {@code stderr} of processes. Processes get blocked when their output buffers
 * are full. Consuming their output prevents them from getting blocked.
 */
public class StreamGobbler extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(StreamGobbler.class);
    private static final AtomicInteger counter = new AtomicInteger();

    private final InputStream stream;
    private final String kind;
    private final List<String> lines = new ArrayList<>();


    public StreamGobbler(InputStream stream, String kind) {
        super("StreamGobbler-" + counter.incrementAndGet());
        this.stream = stream;
        this.kind = kind;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        try (var inputStreamReader = new InputStreamReader(stream)) {
            try (var bufferedReader = new BufferedReader(inputStreamReader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    LOG.debug(kind + ": " + line);
                    synchronized (lines) {
                        lines.add(line);
                    }
                }
            }
        } catch (InterruptedIOException e) {
            // ignore
        } catch (Throwable e) {
            LOG.error("error reading stream", e);
        }
        LOG.debug(kind + "<EOF>");
    }
}