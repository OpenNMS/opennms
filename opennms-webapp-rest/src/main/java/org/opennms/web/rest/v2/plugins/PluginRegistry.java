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
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Persists what the Plugin Management page did in etc/plugin-management.json and
 * derives each plugin's live status from the deploy directory and the container.
 */
public class PluginRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(PluginRegistry.class);

    public static final String FILE_NAME = "plugin-management.json";
    static final String KAR_SUFFIX = ".kar";
    public static final String SOURCE_UPLOAD = "upload";
    static final String WAIT_FOR_KAR = "wait-for-kar=";
    /** written by Karaf's KarService next to each extracted KAR; one feature repository URI per line */
    static final String KAR_FEATURES_CFG = "features.cfg";

    /** Persisted shape; only what an operator did, never derived state. */
    public static class Record {
        private String karName;
        private String fileName;
        private String sha256;
        private long size;
        private String source;
        private String uploadedBy;
        private String uploadedAt;
        private String unloadedBy;
        private String unloadedAt;
        private List<String> features = new ArrayList<>();
        private String bootFile;
        private boolean autoStart;
        private boolean unloaded;
        private Long pendingRestartSince;

        public String getKarName() { return karName; }
        public void setKarName(final String karName) { this.karName = karName; }
        public String getFileName() { return fileName; }
        public void setFileName(final String fileName) { this.fileName = fileName; }
        public String getSha256() { return sha256; }
        public void setSha256(final String sha256) { this.sha256 = sha256; }
        public long getSize() { return size; }
        public void setSize(final long size) { this.size = size; }
        public String getSource() { return source; }
        public void setSource(final String source) { this.source = source; }
        public String getUploadedBy() { return uploadedBy; }
        public void setUploadedBy(final String uploadedBy) { this.uploadedBy = uploadedBy; }
        public String getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(final String uploadedAt) { this.uploadedAt = uploadedAt; }
        public String getUnloadedBy() { return unloadedBy; }
        public void setUnloadedBy(final String unloadedBy) { this.unloadedBy = unloadedBy; }
        public String getUnloadedAt() { return unloadedAt; }
        public void setUnloadedAt(final String unloadedAt) { this.unloadedAt = unloadedAt; }
        public List<String> getFeatures() { return features; }
        public void setFeatures(final List<String> features) { this.features = features; }
        public String getBootFile() { return bootFile; }
        public void setBootFile(final String bootFile) { this.bootFile = bootFile; }
        public boolean isAutoStart() { return autoStart; }
        public void setAutoStart(final boolean autoStart) { this.autoStart = autoStart; }
        public boolean isUnloaded() { return unloaded; }
        public void setUnloaded(final boolean unloaded) { this.unloaded = unloaded; }
        public Long getPendingRestartSince() { return pendingRestartSince; }
        public void setPendingRestartSince(final Long pendingRestartSince) { this.pendingRestartSince = pendingRestartSince; }
    }

    public static class RegistryFile {
        private List<Record> plugins = new ArrayList<>();

        public List<Record> getPlugins() { return plugins; }
        public void setPlugins(final List<Record> plugins) { this.plugins = plugins; }
    }

    private final Path registryFile;
    private final Path deployDir;
    /** etc/featuresBoot.d; null when boot files are not consulted. */
    private final Path bootDir;
    /** data/kar, where Karaf keeps each installed KAR's list of feature repositories; null when not consulted. */
    private final Path karStorageDir;
    private final KarafBridge bridge;
    private final long bootTimeMillis;
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(SerializationFeature.INDENT_OUTPUT);

    public PluginRegistry(final Path opennmsHome, final KarafBridge bridge) {
        this(opennmsHome.resolve("etc").resolve(FILE_NAME), opennmsHome.resolve("deploy"), opennmsHome.resolve("etc").resolve("featuresBoot.d"),
                opennmsHome.resolve("data").resolve("kar"), bridge, ManagementFactory.getRuntimeMXBean().getStartTime());
    }

    PluginRegistry(final Path registryFile, final Path deployDir, final KarafBridge bridge, final long bootTimeMillis) {
        this(registryFile, deployDir, null, null, bridge, bootTimeMillis);
    }

    PluginRegistry(final Path registryFile, final Path deployDir, final Path bootDir, final Path karStorageDir, final KarafBridge bridge, final long bootTimeMillis) {
        this.registryFile = registryFile;
        this.deployDir = deployDir;
        this.bootDir = bootDir;
        this.karStorageDir = karStorageDir;
        this.bridge = bridge;
        this.bootTimeMillis = bootTimeMillis;
    }

    public Path getRegistryFile() {
        return registryFile;
    }

    /**
     * Every recorded plugin, then every KAR in deploy/ or known to the container
     * that has no record, so hand-installed plugins show up with their features.
     */
    public synchronized List<PluginEntry> list() throws IOException {
        final RegistryFile file = read();
        final LiveState live = new LiveState();
        final List<PluginEntry> entries = new ArrayList<>();
        final Set<String> managedNames = new HashSet<>();
        for (final Record record : file.getPlugins()) {
            managedNames.add(record.getKarName());
            entries.add(toEntry(record, live));
        }
        final Map<String, Path> unmanaged = new TreeMap<>();
        for (final Path kar : deployedKars()) {
            final String name = kar.getFileName().toString();
            unmanaged.put(name.substring(0, name.length() - KAR_SUFFIX.length()), kar);
        }
        if (live.available) {
            for (final String karName : live.kars()) {
                unmanaged.putIfAbsent(karName, null);
            }
        }
        for (final Map.Entry<String, Path> e : unmanaged.entrySet()) {
            if (!managedNames.contains(e.getKey())) {
                entries.add(manualEntry(e.getKey(), e.getValue(), live));
            }
        }
        return entries;
    }

    public synchronized Optional<PluginEntry> find(final String karName) throws IOException {
        for (final PluginEntry entry : list()) {
            if (entry.getKarName().equals(karName)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    /** The persisted record for a name, so an install can be reverted to it. */
    public synchronized Optional<Record> record(final String karName) throws IOException {
        for (final Record r : read().getPlugins()) {
            if (karName.equals(r.getKarName())) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    /**
     * Replaces any record with the same name; the previous audit trail lives in the log.
     * Karaf installs the features as soon as the KAR lands in deploy/ unless the manifest
     * opts out, so only that case waits for a restart.
     */
    public synchronized PluginEntry recordInstall(final String karName, final String fileName, final String sha256, final long size, final String source,
                                                  final String user, final List<String> features, final String bootFile, final boolean autoStart) throws IOException {
        final RegistryFile file = read();
        file.getPlugins().removeIf(r -> karName.equals(r.getKarName()));
        final Record record = new Record();
        record.setKarName(karName);
        record.setFileName(fileName);
        record.setSha256(sha256);
        record.setSize(size);
        record.setSource(source == null ? SOURCE_UPLOAD : source);
        record.setUploadedBy(user);
        record.setUploadedAt(Instant.now().toString());
        record.setFeatures(new ArrayList<>(features));
        record.setBootFile(bootFile);
        record.setAutoStart(autoStart);
        record.setUnloaded(false);
        record.setPendingRestartSince(autoStart ? null : System.currentTimeMillis());
        file.getPlugins().add(record);
        write(file);
        return toEntry(record, new LiveState());
    }

    /** sha256 of every plugin whose KAR is still in deploy/, mapped to its install time; drives temp-area retention. */
    public synchronized Map<String, Instant> deployedInstalls() throws IOException {
        final Map<String, Instant> installs = new HashMap<>();
        for (final Record record : read().getPlugins()) {
            if (record.isUnloaded() || record.getSha256() == null || !Files.exists(deployDir.resolve(record.getKarName() + KAR_SUFFIX))) {
                continue;
            }
            Instant installedAt = Instant.EPOCH;
            try {
                if (record.getUploadedAt() != null) {
                    installedAt = Instant.parse(record.getUploadedAt());
                }
            } catch (final DateTimeParseException e) {
                // an unparseable timestamp falls back to the file's own age
            }
            installs.merge(record.getSha256(), installedAt, (a, b) -> a.isAfter(b) ? a : b);
        }
        return installs;
    }

    /** Drops the record written by {@link #recordInstall} and puts back the one it replaced, if any. */
    public synchronized void revertInstall(final String karName, final Record previous) throws IOException {
        final RegistryFile file = read();
        file.getPlugins().removeIf(r -> karName.equals(r.getKarName()));
        if (previous != null) {
            file.getPlugins().add(previous);
        }
        write(file);
    }

    /** Marks the plugin unloaded, creating a record when the KAR was hand-copied. */
    public synchronized PluginEntry recordUnload(final String karName, final String user, final String fileName) throws IOException {
        final RegistryFile file = read();
        Record record = null;
        for (final Record r : file.getPlugins()) {
            if (karName.equals(r.getKarName())) {
                record = r;
                break;
            }
        }
        if (record == null) {
            record = new Record();
            record.setKarName(karName);
            record.setFileName(fileName);
            file.getPlugins().add(record);
        }
        record.setUnloaded(true);
        record.setUnloadedBy(user);
        record.setUnloadedAt(Instant.now().toString());
        record.setPendingRestartSince(System.currentTimeMillis());
        write(file);
        return toEntry(record, new LiveState());
    }

    // --- status derivation ---------------------------------------------------

    /** Container answers fetched once per list() so a page load does not hammer the services. */
    private final class LiveState {
        private final boolean available = bridge.isAvailable();
        private List<String> kars;
        private List<InstalledFeature> features;
        private List<InstalledFeature> allFeatures;

        List<String> kars() {
            if (kars == null) {
                kars = available ? bridge.installedKars() : new ArrayList<>();
            }
            return kars;
        }

        /** Features of the repositories listed in data/kar/&lt;karName&gt;/features.cfg. */
        List<InstalledFeature> karFeatures(final String karName) {
            final Set<String> repositories = karRepositories(karName);
            final List<InstalledFeature> result = new ArrayList<>();
            if (repositories.isEmpty() || !available) {
                return result;
            }
            if (allFeatures == null) {
                allFeatures = bridge.features();
            }
            for (final InstalledFeature f : allFeatures) {
                if (f.getRepository() != null && repositories.contains(f.getRepository().trim())) {
                    result.add(f);
                }
            }
            return result;
        }

        String featureState(final String name) {
            if (features == null) {
                features = available ? bridge.installedFeatures() : new ArrayList<>();
            }
            String state = null;
            for (final InstalledFeature f : features) {
                if (name.equals(f.getName())) {
                    if (f.isStarted()) {
                        return InstalledFeature.STATE_STARTED;
                    }
                    state = f.getState();
                }
            }
            return state == null ? "Uninstalled" : state;
        }
    }

    private PluginEntry toEntry(final Record record, final LiveState live) {
        final PluginEntry entry = new PluginEntry();
        entry.setKarName(record.getKarName());
        entry.setFileName(record.getFileName());
        entry.setSha256(record.getSha256());
        entry.setSize(record.getSize());
        entry.setSource(record.getSource() == null ? SOURCE_UPLOAD : record.getSource());
        entry.setUploadedBy(record.getUploadedBy());
        entry.setUploadedAt(record.getUploadedAt());
        entry.setUnloadedBy(record.getUnloadedBy());
        entry.setUnloadedAt(record.getUnloadedAt());
        // The boot file is what actually starts on the next boot, so a hand-edited one wins over the record.
        final BootReferences boot = bootReferences(record.getKarName());
        entry.setFeatures(boot.features.isEmpty() ? new ArrayList<>(record.getFeatures()) : boot.features);
        entry.setBootFile(record.getBootFile());
        entry.setAutoStart(record.isAutoStart());
        entry.setManaged(true);
        entry.setPendingRestart(record.getPendingRestartSince() != null && record.getPendingRestartSince() > bootTimeMillis);
        entry.setDeployed(Files.exists(deployDir.resolve(record.getKarName() + KAR_SUFFIX)));

        if (record.isUnloaded() || !entry.isDeployed()) {
            entry.setStatus(PluginEntry.STATUS_UNLOADED);
            return entry;
        }
        deriveStatus(entry, live, installedDuringThisJvm(record));
        return entry;
    }

    private void deriveStatus(final PluginEntry entry, final LiveState live, final boolean installedDuringThisJvm) {
        if (!live.available) {
            entry.setStatus(PluginEntry.STATUS_UNKNOWN);
            return;
        }
        entry.setKarLoaded(live.kars().contains(entry.getKarName()));
        boolean allStarted = true;
        for (final String feature : entry.getFeatures()) {
            final String state = live.featureState(feature);
            entry.getFeatureStates().put(feature, state);
            allStarted &= InstalledFeature.STATE_STARTED.equals(state);
        }
        if (entry.isKarLoaded() && allStarted) {
            entry.setStatus(PluginEntry.STATUS_INSTALLED);
        } else if (entry.isKarLoaded() && !installedDuringThisJvm) {
            // The container has had a full boot to start the features from the boot file and did not.
            entry.setStatus(PluginEntry.STATUS_FAILED);
        } else {
            entry.setStatus(PluginEntry.STATUS_STAGED);
        }
    }

    private boolean installedDuringThisJvm(final Record record) {
        if (record.getUploadedAt() == null) {
            return false;
        }
        try {
            return Instant.parse(record.getUploadedAt()).toEpochMilli() > bootTimeMillis;
        } catch (final DateTimeParseException e) {
            return false;
        }
    }

    /**
     * A KAR with no record: copied into deploy/ by hand or installed with kar:install.
     * Its features are the ones its boot lines name, else the top-level features of
     * the repositories Karaf recorded for the KAR.
     *
     * @param kar the file in deploy/, or null when only the container knows the KAR
     */
    private PluginEntry manualEntry(final String karName, final Path kar, final LiveState live) {
        final PluginEntry entry = new PluginEntry();
        entry.setKarName(karName);
        entry.setSource(PluginEntry.SOURCE_MANUAL);
        entry.setManaged(false);
        entry.setDeployed(kar != null);
        boolean recentlyDeployed = false;
        if (kar != null) {
            entry.setFileName(kar.getFileName().toString());
            try {
                entry.setSize(Files.size(kar));
                recentlyDeployed = Files.getLastModifiedTime(kar).toMillis() > bootTimeMillis;
            } catch (final IOException e) {
                LOG.warn("Cannot stat {}: {}", kar, e.toString());
            }
        }
        final BootReferences boot = bootReferences(karName);
        entry.setBootFile(boot.files.isEmpty() ? null : String.join(", ", boot.files));
        final List<InstalledFeature> karFeatures = live.karFeatures(karName);
        final List<String> karFeatureNames = new ArrayList<>();
        for (final InstalledFeature f : karFeatures) {
            karFeatureNames.add(f.getName());
        }
        final List<String> features = new ArrayList<>();
        if (!boot.features.isEmpty()) {
            for (final String name : boot.features) {
                if (karFeatureNames.isEmpty() || karFeatureNames.contains(name)) {
                    features.add(name);
                }
            }
            if (features.isEmpty()) {
                features.addAll(boot.features);
            }
        } else {
            features.addAll(topLevel(karFeatures));
        }
        entry.setFeatures(features);
        deriveStatus(entry, live, recentlyDeployed);
        return entry;
    }

    /** Features no other feature of the same KAR depends on; all of them when the graph has no roots. */
    private static List<String> topLevel(final List<InstalledFeature> features) {
        final Set<String> referenced = new HashSet<>();
        for (final InstalledFeature f : features) {
            referenced.addAll(f.getDependencies());
        }
        final List<String> roots = new ArrayList<>();
        final List<String> all = new ArrayList<>();
        for (final InstalledFeature f : features) {
            if (!all.contains(f.getName())) {
                all.add(f.getName());
            }
            if (!referenced.contains(f.getName()) && !roots.contains(f.getName())) {
                roots.add(f.getName());
            }
        }
        return roots.isEmpty() ? all : roots;
    }

    /** Repository URIs Karaf wrote to data/kar/&lt;karName&gt;/features.cfg when it installed the KAR. */
    private Set<String> karRepositories(final String karName) {
        final Set<String> repositories = new LinkedHashSet<>();
        if (karStorageDir == null) {
            return repositories;
        }
        final Path cfg = karStorageDir.resolve(karName).resolve(KAR_FEATURES_CFG);
        if (!Files.isRegularFile(cfg)) {
            return repositories;
        }
        try {
            for (final String line : Files.readAllLines(cfg, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    repositories.add(line.trim());
                }
            }
        } catch (final IOException e) {
            LOG.warn("Cannot read {}: {}", cfg, e.toString());
        }
        return repositories;
    }

    // --- boot files ------------------------------------------------------------

    /** Boot lines under featuresBoot.d that wait for one KAR: the features they start and the files they live in. */
    static final class BootReferences {
        final List<String> features = new ArrayList<>();
        /** as etc/featuresBoot.d/x.boot, in directory order */
        final List<String> files = new ArrayList<>();
    }

    BootReferences bootReferences(final String karName) {
        final BootReferences references = new BootReferences();
        if (bootDir == null || !Files.isDirectory(bootDir)) {
            return references;
        }
        final List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(bootDir)) {
            for (final Path p : stream) {
                if (Files.isRegularFile(p)) {
                    files.add(p);
                }
            }
        } catch (final IOException e) {
            LOG.warn("Cannot list {}: {}", bootDir, e.toString());
            return references;
        }
        files.sort(null);
        for (final Path file : files) {
            try {
                boolean referenced = false;
                for (final String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                    if (waitsForKar(line, karName)) {
                        referenced = true;
                        final String feature = line.trim().split("\\s+")[0];
                        if (!feature.startsWith(WAIT_FOR_KAR) && !references.features.contains(feature)) {
                            references.features.add(feature);
                        }
                    }
                }
                if (referenced) {
                    final Path etc = bootDir.getParent();
                    references.files.add((etc == null || etc.getFileName() == null ? "" : etc.getFileName() + "/") + bootDir.getFileName() + "/" + file.getFileName());
                }
            } catch (final IOException e) {
                LOG.warn("Cannot read {}: {}", file, e.toString());
            }
        }
        return references;
    }

    /** True when a featuresBoot.d line carries the exact token {@code wait-for-kar=<karName>}. */
    static boolean waitsForKar(final String line, final String karName) {
        final String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return false;
        }
        final String wanted = WAIT_FOR_KAR + karName;
        for (final String token : trimmed.split("\\s+")) {
            if (wanted.equals(token)) {
                return true;
            }
        }
        return false;
    }

    private List<Path> deployedKars() throws IOException {
        final List<Path> kars = new ArrayList<>();
        if (!Files.isDirectory(deployDir)) {
            return kars;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(deployDir)) {
            for (final Path p : stream) {
                // Karaf's KarArtifactInstaller only handles the exact ".kar" suffix.
                if (Files.isRegularFile(p) && p.getFileName().toString().endsWith(KAR_SUFFIX)) {
                    kars.add(p);
                }
            }
        }
        kars.sort(null);
        return kars;
    }

    // --- persistence -----------------------------------------------------------

    private RegistryFile read() throws IOException {
        if (!Files.exists(registryFile)) {
            return new RegistryFile();
        }
        final RegistryFile file = mapper.readValue(registryFile.toFile(), RegistryFile.class);
        if (file.getPlugins() == null) {
            file.setPlugins(new ArrayList<>());
        }
        return file;
    }

    private void write(final RegistryFile file) throws IOException {
        final Path parent = registryFile.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        final Path tmp = parent.resolve(registryFile.getFileName() + ".tmp");
        mapper.writeValue(tmp.toFile(), file);
        try {
            Files.move(tmp, registryFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (final AtomicMoveNotSupportedException e) {
            Files.move(tmp, registryFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
