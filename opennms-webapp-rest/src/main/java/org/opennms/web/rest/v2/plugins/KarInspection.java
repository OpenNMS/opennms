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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of inspecting a candidate KAR file: what it contains and the outcome
 * of every check that was run against it.
 */
public class KarInspection {

    public enum Level { PASS, WARN, FAIL }

    public static class Check {
        private String id;
        private Level level;
        private String message;

        public Check() {
        }

        public Check(final String id, final Level level, final String message) {
            this.id = id;
            this.level = level;
            this.message = message;
        }

        public String getId() { return id; }
        public void setId(final String id) { this.id = id; }
        public Level getLevel() { return level; }
        public void setLevel(final Level level) { this.level = level; }
        public String getMessage() { return message; }
        public void setMessage(final String message) { this.message = message; }

        @Override
        public String toString() {
            return level + " " + id + ": " + message;
        }
    }

    public static class DependencyInfo {
        private String name;
        private String version;
        private boolean dependencyOnly;

        public DependencyInfo() {
        }

        public DependencyInfo(final String name, final String version, final boolean dependencyOnly) {
            this.name = name;
            this.version = version;
            this.dependencyOnly = dependencyOnly;
        }

        public String getName() { return name; }
        public void setName(final String name) { this.name = name; }
        public String getVersion() { return version; }
        public void setVersion(final String version) { this.version = version; }
        public boolean isDependencyOnly() { return dependencyOnly; }
        public void setDependencyOnly(final boolean dependencyOnly) { this.dependencyOnly = dependencyOnly; }
    }

    public static class FeatureInfo {
        private String name;
        private String version;
        private String repository;
        private String description;
        private boolean hidden;
        /** not hidden and not pulled in by another feature of the same KAR; see {@link #topLevelFeatures()}. */
        private boolean topLevel;
        private List<DependencyInfo> dependencies = new ArrayList<>();
        private List<String> bundles = new ArrayList<>();

        public String getName() { return name; }
        public void setName(final String name) { this.name = name; }
        public String getVersion() { return version; }
        public void setVersion(final String version) { this.version = version; }
        public String getRepository() { return repository; }
        public void setRepository(final String repository) { this.repository = repository; }
        public String getDescription() { return description; }
        public void setDescription(final String description) { this.description = description; }
        public boolean isHidden() { return hidden; }
        public void setHidden(final boolean hidden) { this.hidden = hidden; }
        public boolean isTopLevel() { return topLevel; }
        public void setTopLevel(final boolean topLevel) { this.topLevel = topLevel; }
        public List<DependencyInfo> getDependencies() { return dependencies; }
        public void setDependencies(final List<DependencyInfo> dependencies) { this.dependencies = dependencies; }
        public List<String> getBundles() { return bundles; }
        public void setBundles(final List<String> bundles) { this.bundles = bundles; }
    }

    public static class ImportDescriptor {
        /** version range string, "" when the import has no version. */
        private String version = "";
        private boolean optional;

        public ImportDescriptor() {
        }

        public ImportDescriptor(final String version, final boolean optional) {
            this.version = version == null ? "" : version;
            this.optional = optional;
        }

        public String getVersion() { return version; }
        public void setVersion(final String version) { this.version = version; }
        public boolean isOptional() { return optional; }
        public void setOptional(final boolean optional) { this.optional = optional; }
    }

    public static class BundleDescriptor {
        private String path;
        private String symbolicName;
        private String version;
        /** package name to its version range and resolution directive. */
        private Map<String, ImportDescriptor> imports = new LinkedHashMap<>();
        private List<String> exports = new ArrayList<>();
        private String requiredJavaVersion;

        public String getPath() { return path; }
        public void setPath(final String path) { this.path = path; }
        public String getSymbolicName() { return symbolicName; }
        public void setSymbolicName(final String symbolicName) { this.symbolicName = symbolicName; }
        public String getVersion() { return version; }
        public void setVersion(final String version) { this.version = version; }
        public Map<String, ImportDescriptor> getImports() { return imports; }
        public void setImports(final Map<String, ImportDescriptor> imports) { this.imports = imports; }
        public List<String> getExports() { return exports; }
        public void setExports(final List<String> exports) { this.exports = exports; }
        public String getRequiredJavaVersion() { return requiredJavaVersion; }
        public void setRequiredJavaVersion(final String requiredJavaVersion) { this.requiredJavaVersion = requiredJavaVersion; }
    }

    public static class Source {
        private String repository;
        private String tag;
        private String assetName;
        private String url;

        public Source() {
        }

        public Source(final String repository, final String tag, final String assetName, final String url) {
            this.repository = repository;
            this.tag = tag;
            this.assetName = assetName;
            this.url = url;
        }

        public String getRepository() { return repository; }
        public void setRepository(final String repository) { this.repository = repository; }
        public String getTag() { return tag; }
        public void setTag(final String tag) { this.tag = tag; }
        public String getAssetName() { return assetName; }
        public void setAssetName(final String assetName) { this.assetName = assetName; }
        public String getUrl() { return url; }
        public void setUrl(final String url) { this.url = url; }

        /** The registry form, e.g. {@code github:OpenNMS-Plugins/alec@v3.0.4}. */
        public String toRegistryString() {
            return "github:" + repository + "@" + tag;
        }
    }

    private String karName;
    private String fileName;
    private String uploadToken;
    private Source source;
    private long size;
    private String sha256;
    private String featureStart;
    private String createdBy;
    private Map<String, String> manifest = new LinkedHashMap<>();
    private List<String> featureRepositories = new ArrayList<>();
    private List<FeatureInfo> features = new ArrayList<>();
    private List<BundleDescriptor> bundles = new ArrayList<>();
    private List<Check> checks = new ArrayList<>();
    /** Top-level features the page should pre-select; empty when the operator has to choose. */
    private List<String> suggestedFeatures = new ArrayList<>();

    public String getKarName() { return karName; }
    public void setKarName(final String karName) { this.karName = karName; }
    public String getFileName() { return fileName; }
    public void setFileName(final String fileName) { this.fileName = fileName; }
    public String getUploadToken() { return uploadToken; }
    public void setUploadToken(final String uploadToken) { this.uploadToken = uploadToken; }
    public Source getSource() { return source; }
    public void setSource(final Source source) { this.source = source; }
    public long getSize() { return size; }
    public void setSize(final long size) { this.size = size; }
    public String getSha256() { return sha256; }
    public void setSha256(final String sha256) { this.sha256 = sha256; }
    public String getFeatureStart() { return featureStart; }
    public void setFeatureStart(final String featureStart) { this.featureStart = featureStart; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(final String createdBy) { this.createdBy = createdBy; }
    public Map<String, String> getManifest() { return manifest; }
    public void setManifest(final Map<String, String> manifest) { this.manifest = manifest; }
    public List<String> getFeatureRepositories() { return featureRepositories; }
    public void setFeatureRepositories(final List<String> featureRepositories) { this.featureRepositories = featureRepositories; }
    public List<FeatureInfo> getFeatures() { return features; }
    public void setFeatures(final List<FeatureInfo> features) { this.features = features; }
    public List<BundleDescriptor> getBundles() { return bundles; }
    public void setBundles(final List<BundleDescriptor> bundles) { this.bundles = bundles; }
    public List<Check> getChecks() { return checks; }
    public void setChecks(final List<Check> checks) { this.checks = checks; }
    public List<String> getSuggestedFeatures() { return suggestedFeatures; }
    public void setSuggestedFeatures(final List<String> suggestedFeatures) { this.suggestedFeatures = suggestedFeatures; }

    public List<Check> checksAt(final Level level) {
        final List<Check> result = new ArrayList<>();
        for (final Check c : checks) {
            if (c.getLevel() == level) {
                result.add(c);
            }
        }
        return result;
    }

    /**
     * Features declared by the KAR that no other feature in the same KAR depends on.
     * Falls back to every declared feature when the graph has no roots (cycles).
     */
    public List<FeatureInfo> topLevelFeatures() {
        final List<FeatureInfo> roots = new ArrayList<>();
        for (final FeatureInfo candidate : features) {
            boolean referenced = false;
            for (final FeatureInfo other : features) {
                if (other == candidate) {
                    continue;
                }
                for (final DependencyInfo dep : other.getDependencies()) {
                    if (candidate.getName().equals(dep.getName())) {
                        referenced = true;
                        break;
                    }
                }
                if (referenced) {
                    break;
                }
            }
            if (!referenced && !candidate.isHidden()) {
                roots.add(candidate);
            }
        }
        return roots.isEmpty() ? new ArrayList<>(features) : roots;
    }

    /** Stamps {@link FeatureInfo#isTopLevel()} on every feature once all repositories are read. */
    public void markTopLevelFeatures() {
        final List<FeatureInfo> roots = topLevelFeatures();
        for (final FeatureInfo feature : features) {
            feature.setTopLevel(roots.contains(feature));
        }
    }

    public List<String> topLevelFeatureNames() {
        final List<String> names = new ArrayList<>();
        for (final FeatureInfo feature : topLevelFeatures()) {
            names.add(feature.getName());
        }
        return names;
    }
}
