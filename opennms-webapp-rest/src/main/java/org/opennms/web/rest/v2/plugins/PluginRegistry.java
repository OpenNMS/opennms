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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

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

    /** Persisted shape; only what an operator did, never derived state. */
    public static class Record {
        private String karName;
        private String fileName;
        private String sha256;
        private long size;
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
    private final KarafBridge bridge;
    private final long bootTimeMillis;
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(SerializationFeature.INDENT_OUTPUT);

    public PluginRegistry(final Path opennmsHome, final KarafBridge bridge) {
        this(opennmsHome.resolve("etc").resolve(FILE_NAME), opennmsHome.resolve("deploy"), bridge, ManagementFactory.getRuntimeMXBean().getStartTime());
    }

    PluginRegistry(final Path registryFile, final Path deployDir, final KarafBridge bridge, final long bootTimeMillis) {
        this.registryFile = registryFile;
        this.deployDir = deployDir;
        this.bridge = bridge;
        this.bootTimeMillis = bootTimeMillis;
    }

    public Path getRegistryFile() {
        return registryFile;
    }

    public synchronized List<PluginEntry> list() throws IOException {
        final RegistryFile file = read();
        final LiveState live = new LiveState();
        final List<PluginEntry> entries = new ArrayList<>();
        final Set<String> managedNames = new HashSet<>();
        for (final Record record : file.getPlugins()) {
            managedNames.add(record.getKarName());
            entries.add(toEntry(record, live));
        }
        for (final Path kar : deployedKars()) {
            final String name = kar.getFileName().toString();
            final String karName = name.substring(0, name.length() - KAR_SUFFIX.length());
            if (!managedNames.contains(karName)) {
                entries.add(unmanagedEntry(kar, karName, live));
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

    public synchronized boolean restartRequired() throws IOException {
        for (final PluginEntry entry : list()) {
            if (entry.isPendingRestart() || PluginEntry.STATUS_STAGED.equals(entry.getStatus())) {
                return true;
            }
        }
        return false;
    }

    /** Replaces any record with the same name; the previous audit trail lives in the log. */
    public synchronized PluginEntry recordInstall(final String karName, final String fileName, final String sha256, final long size,
                                                  final String user, final List<String> features, final String bootFile, final boolean autoStart) throws IOException {
        final RegistryFile file = read();
        file.getPlugins().removeIf(r -> karName.equals(r.getKarName()));
        final Record record = new Record();
        record.setKarName(karName);
        record.setFileName(fileName);
        record.setSha256(sha256);
        record.setSize(size);
        record.setUploadedBy(user);
        record.setUploadedAt(Instant.now().toString());
        record.setFeatures(new ArrayList<>(features));
        record.setBootFile(bootFile);
        record.setAutoStart(autoStart);
        record.setUnloaded(false);
        record.setPendingRestartSince(System.currentTimeMillis());
        file.getPlugins().add(record);
        write(file);
        return toEntry(record, new LiveState());
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

        List<String> kars() {
            if (kars == null) {
                kars = available ? bridge.installedKars() : new ArrayList<>();
            }
            return kars;
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
        entry.setUploadedBy(record.getUploadedBy());
        entry.setUploadedAt(record.getUploadedAt());
        entry.setUnloadedBy(record.getUnloadedBy());
        entry.setUnloadedAt(record.getUnloadedAt());
        entry.setFeatures(new ArrayList<>(record.getFeatures()));
        entry.setBootFile(record.getBootFile());
        entry.setAutoStart(record.isAutoStart());
        entry.setManaged(true);
        entry.setPendingRestart(record.getPendingRestartSince() != null && record.getPendingRestartSince() > bootTimeMillis);
        entry.setDeployed(Files.exists(deployDir.resolve(record.getKarName() + KAR_SUFFIX)));

        if (record.isUnloaded() || !entry.isDeployed()) {
            entry.setStatus(PluginEntry.STATUS_UNLOADED);
            return entry;
        }
        if (!live.available) {
            entry.setStatus(PluginEntry.STATUS_UNKNOWN);
            return entry;
        }
        entry.setKarLoaded(live.kars().contains(record.getKarName()));
        boolean allStarted = true;
        for (final String feature : record.getFeatures()) {
            final String state = live.featureState(feature);
            entry.getFeatureStates().put(feature, state);
            allStarted &= InstalledFeature.STATE_STARTED.equals(state);
        }
        entry.setStatus(entry.isKarLoaded() && allStarted ? PluginEntry.STATUS_INSTALLED : PluginEntry.STATUS_STAGED);
        return entry;
    }

    private PluginEntry unmanagedEntry(final Path kar, final String karName, final LiveState live) {
        final PluginEntry entry = new PluginEntry();
        entry.setKarName(karName);
        entry.setFileName(kar.getFileName().toString());
        try {
            entry.setSize(Files.size(kar));
        } catch (final IOException e) {
            LOG.warn("Cannot stat {}: {}", kar, e.toString());
        }
        entry.setManaged(false);
        entry.setDeployed(true);
        entry.setKarLoaded(live.available && live.kars().contains(karName));
        entry.setStatus(PluginEntry.STATUS_UNMANAGED);
        return entry;
    }

    private List<Path> deployedKars() throws IOException {
        final List<Path> kars = new ArrayList<>();
        if (!Files.isDirectory(deployDir)) {
            return kars;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(deployDir)) {
            for (final Path p : stream) {
                if (Files.isRegularFile(p) && p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(KAR_SUFFIX)) {
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
