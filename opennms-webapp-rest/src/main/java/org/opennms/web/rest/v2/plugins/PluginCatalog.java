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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The plugins offered on the page, read from etc/plugin-catalog.json on every
 * call so edits show up without a restart; the compiled-in list stands in when
 * the file is missing or unreadable.
 */
public class PluginCatalog {
    private static final Logger LOG = LoggerFactory.getLogger(PluginCatalog.class);

    public static final String FILE_NAME = "plugin-catalog.json";
    static final String DEFAULT_ASSET_PATTERN = ".*\\.kar$";
    static final Pattern ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");
    static final Pattern DOCS_URL = Pattern.compile("^https?://.+", Pattern.CASE_INSENSITIVE);

    public static class Entry {
        private String id;
        private String name;
        private String description;
        private String repository;
        private String assetPattern = DEFAULT_ASSET_PATTERN;
        private String docsUrl;
        /** Exact names of the features the page pre-selects for a KAR of this plugin; empty leaves the choice to the operator. */
        private List<String> bootFeatures = new ArrayList<>();

        public Entry() {
        }

        public Entry(final String id, final String name, final String description, final String repository, final String docsUrl, final String... bootFeatures) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.repository = repository;
            this.docsUrl = docsUrl;
            this.bootFeatures = new ArrayList<>(Arrays.asList(bootFeatures));
        }

        public String getId() { return id; }
        public void setId(final String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(final String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(final String description) { this.description = description; }
        public String getRepository() { return repository; }
        public void setRepository(final String repository) { this.repository = repository; }
        public String getAssetPattern() { return assetPattern; }
        public void setAssetPattern(final String assetPattern) { this.assetPattern = assetPattern == null || assetPattern.isBlank() ? DEFAULT_ASSET_PATTERN : assetPattern; }
        public String getDocsUrl() { return docsUrl; }
        public void setDocsUrl(final String docsUrl) { this.docsUrl = docsUrl; }
        public List<String> getBootFeatures() { return bootFeatures; }
        public void setBootFeatures(final List<String> bootFeatures) {
            this.bootFeatures = new ArrayList<>();
            if (bootFeatures != null) {
                for (final String feature : bootFeatures) {
                    if (feature != null && !feature.isBlank() && !this.bootFeatures.contains(feature.trim())) {
                        this.bootFeatures.add(feature.trim());
                    }
                }
            }
        }

        public Pattern assetPattern() {
            return Pattern.compile(assetPattern);
        }
    }

    public static class CatalogFile {
        private List<Entry> entries = new ArrayList<>();

        public List<Entry> getEntries() { return entries; }
        public void setEntries(final List<Entry> entries) { this.entries = entries; }
    }

    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final AtomicBoolean warnedEmpty = new AtomicBoolean();

    public PluginCatalog(final Path file) {
        this.file = file;
    }

    public static List<Entry> defaults() {
        final List<Entry> entries = new ArrayList<>();
        entries.add(new Entry("alec", "ALEC",
                "Architecture for Learning Enabled Correlation: groups related alarms into situations using configurable correlation engines.",
                "OpenNMS-Plugins/alec", "https://docs.opennms.com/alec/latest/", "alec-opennms-standalone"));
        entries.add(new Entry("prometheus-remotewrite", "Prometheus Remote Write",
                "Receives Prometheus remote-write streams and stores the samples as OpenNMS time-series metrics.",
                "OpenNMS-Plugins/opennms-prometheus-remotewrite-plugin", "https://github.com/OpenNMS-Plugins/opennms-prometheus-remotewrite-plugin#readme", "opennms-plugins-prometheus-remotewrite"));
        entries.add(new Entry("pagerduty", "PagerDuty",
                "Forwards OpenNMS alarms to PagerDuty as incidents and keeps them in sync as the alarms change.",
                "OpenNMS-Plugins/opennms-pagerduty-plugin", "https://github.com/OpenNMS-Plugins/opennms-pagerduty-plugin#readme", "opennms-plugins-pagerduty"));
        return entries;
    }

    /** The features the catalog pre-selects for a KAR fetched from {@code repository}; empty when the repository is not listed. */
    public List<String> bootFeaturesFor(final String repository) {
        if (repository == null) {
            return Collections.emptyList();
        }
        return entries().stream().filter(e -> repository.equals(e.getRepository())).map(Entry::getBootFeatures).findFirst().orElse(Collections.emptyList());
    }

    public Path getFile() {
        return file;
    }

    public List<Entry> entries() {
        if (file == null || !Files.isRegularFile(file)) {
            return defaults();
        }
        try {
            final CatalogFile parsed = mapper.readValue(file.toFile(), CatalogFile.class);
            final List<Entry> entries = parsed == null || parsed.getEntries() == null ? Collections.emptyList() : validated(parsed.getEntries());
            if (entries.isEmpty()) {
                if (warnedEmpty.compareAndSet(false, true)) {
                    LOG.warn("{} lists no usable entries; using the built-in catalog", file);
                }
                return defaults();
            }
            warnedEmpty.set(false);
            return entries;
        } catch (final IOException e) {
            LOG.warn("Cannot read {}; using the built-in catalog: {}", file, e.toString());
            return defaults();
        }
    }

    public Optional<Entry> find(final String id) {
        if (id == null) {
            return Optional.empty();
        }
        return entries().stream().filter(e -> id.equals(e.getId())).findFirst();
    }

    static List<Entry> validated(final List<Entry> raw) {
        final List<Entry> entries = new ArrayList<>();
        final Set<String> ids = new HashSet<>();
        for (final Entry entry : raw) {
            if (entry == null || entry.getId() == null || !ID.matcher(entry.getId()).matches()) {
                LOG.warn("Ignoring catalog entry without a valid id: {}", entry == null ? null : entry.getId());
                continue;
            }
            if (!GitHubReleases.isValidRepository(entry.getRepository())) {
                LOG.warn("Ignoring catalog entry '{}': repository '{}' is not owner/name", entry.getId(), entry.getRepository());
                continue;
            }
            if (!ids.add(entry.getId())) {
                LOG.warn("Ignoring duplicate catalog entry '{}'", entry.getId());
                continue;
            }
            try {
                entry.assetPattern();
            } catch (final PatternSyntaxException e) {
                LOG.warn("Catalog entry '{}': assetPattern '{}' is not a valid regular expression; using the default", entry.getId(), entry.getAssetPattern());
                entry.setAssetPattern(DEFAULT_ASSET_PATTERN);
            }
            if (entry.getName() == null || entry.getName().isBlank()) {
                entry.setName(entry.getId());
            }
            if (entry.getDocsUrl() != null && !DOCS_URL.matcher(entry.getDocsUrl().trim()).matches()) {
                LOG.warn("Catalog entry '{}': docsUrl '{}' is not an http(s) URL; dropping it", entry.getId(), entry.getDocsUrl());
                entry.setDocsUrl(null);
            }
            entries.add(entry);
        }
        return entries;
    }
}
