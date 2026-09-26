/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.rest.v2.plugins;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The upload and download area under data/tmp/plugin-management. A checked KAR
 * waits here as {@code <sha256>.kar} with its original name in {@code .name} and
 * its origin in {@code .source}; the retention rules and the size cap keep the
 * directory from growing without bound.
 */
public class TempArea {
    private static final Logger LOG = LoggerFactory.getLogger(TempArea.class);
    private static final Logger AUDIT = LoggerFactory.getLogger(PluginManagementRestService.AUDIT_LOGGER);

    static final Pattern TOKEN_FILE = Pattern.compile("([0-9a-f]{64})\\.(kar|name|source)");
    static final String KAR_SUFFIX = ".kar";
    static final String NAME_SUFFIX = ".name";
    static final String SOURCE_SUFFIX = ".source";
    static final String PART_SUFFIX = ".part";
    static final Duration RETENTION = Duration.ofHours(1);
    static final Duration PART_GRACE = Duration.ofMinutes(5);
    static final Duration CLEANUP_INTERVAL = Duration.ofMinutes(15);
    static final long CAP_BYTES = 2L * 1024 * 1024 * 1024;
    static final String THREAD_NAME = "plugin-management-cleanup";
    static final String NO_SPACE = "Not enough temporary space";

    /** sha256 of each plugin whose KAR is in deploy/, mapped to when it was installed. */
    public interface DeployedInstalls {
        Map<String, Instant> get() throws IOException;
    }

    public static final class Usage {
        private final long bytes;
        private final int files;

        Usage(final long bytes, final int files) {
            this.bytes = bytes;
            this.files = files;
        }

        public long getBytes() { return bytes; }
        public int getFiles() { return files; }
    }

    public static final class Cleanup {
        private int removed;
        private long bytes;

        public int getRemoved() { return removed; }
        public long getBytes() { return bytes; }
    }

    private static final class Group {
        final List<Path> files = new ArrayList<>();
        Instant reference = Instant.EPOCH;
        long bytes;
    }

    private final Path dir;
    private final Clock clock;
    private final DeployedInstalls deployed;
    private final long capBytes;
    /** Bytes promised to part files still being written, so concurrent transfers share the cap. */
    private final Map<Path, Long> reservations = new HashMap<>();
    private ScheduledExecutorService scheduler;

    public TempArea(final Path dir, final DeployedInstalls deployed) {
        this(dir, Clock.systemUTC(), deployed, CAP_BYTES);
    }

    TempArea(final Path dir, final Clock clock, final DeployedInstalls deployed, final long capBytes) {
        this.dir = dir;
        this.clock = clock;
        this.deployed = deployed;
        this.capBytes = capBytes;
    }

    public Path getDir() {
        return dir;
    }

    public Path karFile(final String token) {
        return dir.resolve(token + KAR_SUFFIX);
    }

    public Optional<String> fileName(final String token) {
        return readSidecar(token + NAME_SUFFIX);
    }

    public Optional<String> source(final String token) {
        return readSidecar(token + SOURCE_SUFFIX);
    }

    public synchronized Usage usage() {
        long bytes = 0;
        int files = 0;
        for (final Path p : regularFiles()) {
            bytes += size(p);
            files++;
        }
        return new Usage(bytes, files);
    }

    /** A fresh {@code .part} file; every write goes through one so a crash leaves nothing that looks committed. */
    public synchronized Path newPartFile(final String prefix) throws IOException {
        Files.createDirectories(dir);
        return Files.createTempFile(dir, prefix, PART_SUFFIX);
    }

    /** Moves a part file into place under its sha256, writes the sidecars and releases the part's reservation. */
    public synchronized void commit(final Path part, final String sha256, final String fileName, final String source) throws IOException {
        try {
            Files.createDirectories(dir);
            final Path target = karFile(sha256);
            try {
                Files.move(part, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (final AtomicMoveNotSupportedException e) {
                Files.move(part, target, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.write(dir.resolve(sha256 + NAME_SUFFIX), fileName.getBytes(StandardCharsets.UTF_8));
            Files.write(dir.resolve(sha256 + SOURCE_SUFFIX), source.getBytes(StandardCharsets.UTF_8));
        } finally {
            reservations.remove(part);
        }
    }

    /** Deletes a part file that will not be committed and releases its reservation. */
    public synchronized void abort(final Path part) {
        reservations.remove(part);
        try {
            Files.deleteIfExists(part);
        } catch (final IOException e) {
            LOG.warn("Cannot delete {}: {}", part, e.toString());
        }
    }

    /**
     * Reserves room for the bytes about to be written to {@code part}: applies the
     * retention rules, then evicts the oldest stored KARs until the file fits under
     * the cap next to the other transfers in flight, and on the disk. A size that
     * is not known yet ({@code <= 0}) reserves the KAR size limit.
     */
    public synchronized void ensureSpace(final Path part, final long incoming) throws PluginSourceException {
        final long needed = incoming <= 0 ? KarInspector.MAX_SIZE_BYTES : incoming;
        reservations.remove(part);
        if (needed > capBytes) {
            throw new PluginSourceException(PluginSourceException.INSUFFICIENT_STORAGE,
                    NO_SPACE + ": " + megabytes(needed) + " MB is more than the " + megabytes(capBytes) + " MB cap of " + dir + ".");
        }
        final long reserved = reservations.values().stream().mapToLong(Long::longValue).sum();
        final Cleanup cleanup = new Cleanup();
        evict(prune(cleanup), reserved + needed, cleanup);
        report(cleanup);
        if (total() + reserved + needed > capBytes) {
            throw new PluginSourceException(PluginSourceException.INSUFFICIENT_STORAGE,
                    NO_SPACE + ": " + megabytes(needed) + " MB would exceed the " + megabytes(capBytes) + " MB cap of " + dir + ". Wait for the area to be cleaned up or install the pending plugins.");
        }
        try {
            Files.createDirectories(dir);
            final long usable = Files.getFileStore(dir).getUsableSpace();
            if (usable < reserved + needed) {
                throw new PluginSourceException(PluginSourceException.INSUFFICIENT_STORAGE,
                        NO_SPACE + ": " + megabytes(usable) + " MB free under " + dir + ", " + megabytes(reserved + needed) + " MB needed. Free disk space and try again.");
            }
        } catch (final IOException e) {
            throw new PluginSourceException(PluginSourceException.INSUFFICIENT_STORAGE, "Cannot use " + dir + ": " + PluginSourceException.describe(e));
        }
        reservations.put(part, needed);
    }

    synchronized long reservedBytes() {
        return reservations.values().stream().mapToLong(Long::longValue).sum();
    }

    public synchronized Cleanup cleanup() {
        final Cleanup cleanup = new Cleanup();
        evict(prune(cleanup), 0, cleanup);
        report(cleanup);
        return cleanup;
    }

    public synchronized void ensureScheduled() {
        if (scheduler != null) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, THREAD_NAME);
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                cleanup();
            } catch (final Throwable t) {
                LOG.error("Scheduled cleanup of {} failed: {}", dir, t.toString(), t);
            }
        }, CLEANUP_INTERVAL.toMillis(), CLEANUP_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
    }

    public synchronized void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    // --- rules -----------------------------------------------------------------

    /** Deletes what the retention rules say and returns the surviving stored KARs, oldest first. */
    private List<Group> prune(final Cleanup cleanup) {
        final Instant now = clock.instant();
        final Map<String, Instant> installs = deployedInstalls();
        final Map<String, Group> groups = new LinkedHashMap<>();
        for (final Path p : regularFiles()) {
            final String name = p.getFileName().toString();
            final Matcher token = TOKEN_FILE.matcher(name);
            if (token.matches()) {
                groups.computeIfAbsent(token.group(1), sha -> new Group()).files.add(p);
            } else if (name.endsWith(PART_SUFFIX)) {
                if (age(p, now).compareTo(PART_GRACE) > 0) {
                    delete(p, cleanup);
                }
            } else {
                delete(p, cleanup);
            }
        }
        final List<Group> kept = new ArrayList<>();
        for (final Map.Entry<String, Group> e : groups.entrySet()) {
            final Group group = e.getValue();
            Instant reference = installs == null ? Instant.EPOCH : installs.getOrDefault(e.getKey(), Instant.EPOCH);
            for (final Path p : group.files) {
                final Instant modified = modified(p);
                if (modified.isAfter(reference)) {
                    reference = modified;
                }
            }
            // Without the registry the deployed set is unknown, so nothing expires on age this run.
            if (installs != null && Duration.between(reference, now).compareTo(RETENTION) > 0) {
                group.files.forEach(p -> delete(p, cleanup));
                continue;
            }
            group.files.removeIf(p -> {
                if (size(p) == 0 && age(p, now).compareTo(PART_GRACE) > 0) {
                    delete(p, cleanup);
                    return true;
                }
                return false;
            });
            if (group.files.isEmpty()) {
                continue;
            }
            group.reference = reference;
            group.bytes = group.files.stream().mapToLong(TempArea::size).sum();
            kept.add(group);
        }
        kept.sort(Comparator.comparing(g -> g.reference));
        return kept;
    }

    private void evict(final List<Group> oldestFirst, final long incoming, final Cleanup cleanup) {
        long total = total();
        for (final Group group : oldestFirst) {
            if (total + incoming <= capBytes) {
                return;
            }
            group.files.forEach(p -> delete(p, cleanup));
            total -= group.bytes;
        }
    }

    private void report(final Cleanup cleanup) {
        if (cleanup.removed > 0) {
            AUDIT.info("action=cleanup removed={} bytes={}", cleanup.removed, cleanup.bytes);
        }
    }

    private Map<String, Instant> deployedInstalls() {
        try {
            return deployed == null ? Map.of() : deployed.get();
        } catch (final IOException e) {
            LOG.warn("Cannot read the plugin registry; keeping every stored KAR this run: {}", e.toString());
            return null;
        }
    }

    // --- plumbing --------------------------------------------------------------

    private long total() {
        long bytes = 0;
        for (final Path p : regularFiles()) {
            bytes += size(p);
        }
        return bytes;
    }

    private List<Path> regularFiles() {
        final List<Path> files = new ArrayList<>();
        if (!Files.isDirectory(dir)) {
            return files;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (final Path p : stream) {
                if (Files.isRegularFile(p)) {
                    files.add(p);
                }
            }
        } catch (final IOException e) {
            LOG.warn("Cannot list {}: {}", dir, e.toString());
        }
        files.sort(null);
        return files;
    }

    private void delete(final Path p, final Cleanup cleanup) {
        final long bytes = size(p);
        try {
            if (Files.deleteIfExists(p)) {
                reservations.remove(p);
                cleanup.removed++;
                cleanup.bytes += bytes;
            }
        } catch (final IOException e) {
            LOG.warn("Cannot delete {}: {}", p, e.toString());
        }
    }

    private static Duration age(final Path p, final Instant now) {
        return Duration.between(modified(p), now);
    }

    private static Instant modified(final Path p) {
        try {
            return Files.getLastModifiedTime(p).toInstant();
        } catch (final IOException e) {
            return Instant.EPOCH;
        }
    }

    private static long size(final Path p) {
        try {
            return Files.size(p);
        } catch (final IOException e) {
            return 0;
        }
    }

    static String megabytes(final long bytes) {
        return String.format(Locale.ROOT, "%.1f", bytes / (1024.0 * 1024.0));
    }

    private Optional<String> readSidecar(final String name) {
        final Path file = dir.resolve(name);
        try {
            if (Files.isRegularFile(file)) {
                final String value = new String(Files.readAllBytes(file), StandardCharsets.UTF_8).trim();
                if (!value.isEmpty()) {
                    return Optional.of(value);
                }
            }
        } catch (final IOException e) {
            LOG.warn("Cannot read {}: {}", file, e.toString());
        }
        return Optional.empty();
    }
}
