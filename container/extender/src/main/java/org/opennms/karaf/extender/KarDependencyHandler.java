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
package org.opennms.karaf.extender;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.features.Repository;
import org.apache.karaf.kar.KarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KarDependencyHandler implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(KarDependencyHandler.class);

    private static final int KAR_LIST_SLEEP_MS = 5000;

    private final List<Feature> features;
    private final KarService karService;
    private final FeaturesService featuresService;
    private final KarafExtender.ExtenderStatus extenderStatus;

    private static final String FEATURE_CONFIG_FILE = "features.cfg";
    private static final List<String> KAR_EXTENSIONS = List.of(".kar", ".zip");

    private final Path karStorage;
    private final List<Path> deployFolders;
    private final boolean cacheCleanedAtStart;
    private final long jvmStartTime;

    public KarDependencyHandler(List<Feature> features, KarService karService, FeaturesService featuresService, KarafExtender.ExtenderStatus extenderStatus,
                                List<Path> deployFolders) {
        this(features, karService, featuresService, extenderStatus,
                Paths.get(System.getProperty("karaf.data"), "kar"),
                deployFolders,
                Boolean.parseBoolean(System.getProperty("karaf.clean.cache")),
                ManagementFactory.getRuntimeMXBean().getStartTime());
    }

    KarDependencyHandler(List<Feature> features, KarService karService, FeaturesService featuresService, KarafExtender.ExtenderStatus extenderStatus,
                         Path karStorage, List<Path> deployFolders, boolean cacheCleanedAtStart, long jvmStartTime) {
        this.features = Objects.requireNonNull(features);
        this.karService = Objects.requireNonNull(karService);
        this.featuresService = Objects.requireNonNull(featuresService);
        this.extenderStatus = Objects.requireNonNull(extenderStatus);
        this.karStorage = Objects.requireNonNull(karStorage);
        this.deployFolders = Objects.requireNonNull(deployFolders);
        this.cacheCleanedAtStart = cacheCleanedAtStart;
        this.jvmStartTime = jvmStartTime;
    }

    @Override
    public void run() {
        final String repairFailure;
        if (cacheCleanedAtStart) {
            repairFailure = repairInstalledKars();
        } else {
            LOG.debug("karaf.clean.cache is not enabled. Skipping the check for missing .kar feature repositories.");
            repairFailure = null;
        }
        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        if (features.isEmpty()) {
            LOG.debug("No features with dependencies on .kar files to install.");
            karsDone("", repairFailure);
            return;
        }

        final Set<String> allKarDependencies = features.stream()
                .map(Feature::getKarDependency)
                .collect(Collectors.toSet());
        final Set<String> karsToWaitFor = new HashSet<>(allKarDependencies);

        // Wait for all the .kars to be installed
        while (true) {
            try {
                extenderStatus.info("Waiting on {}", karsToWaitFor);
                karsToWaitFor.removeAll(karService.list());
                if (karsToWaitFor.isEmpty()) {
                    break;
                }
            } catch (Exception e) {
                extenderStatus.warn("Enumerating installed .kar files failed. Will retry in {}ms.", KAR_LIST_SLEEP_MS, e);
            }

            try {
                Thread.sleep(KAR_LIST_SLEEP_MS);
            } catch (InterruptedException e) {
                LOG.info("Interrupted. Stopping thread.");
                return;
            }
        }
        LOG.info("All .kar dependencies are ready now.");

        // Gather the set of known feature URIs
        final Set<URI> availableFeatureUris = getAvailableFeatureUris();

        // Ensure that all of the feature repositories for the .kar files are installed
        final Set<URI> missingFeatureUris = new HashSet<>();
        for (String karDependency : allKarDependencies) {
            missingFeatureUris.addAll(getFeaturesUrisForKar(karDependency));
        }
        missingFeatureUris.removeAll(availableFeatureUris);
        if (missingFeatureUris.isEmpty()) {
            LOG.debug("No missing feature repositories.");
        } else {
            LOG.info("Installing feature repositories: {}", missingFeatureUris);
            for (URI featureUri : missingFeatureUris) {
                try {
                    featuresService.addRepository(featureUri);
                } catch (Exception e) {
                    extenderStatus.error("Failed to install feature repository: {}", featureUri, e);
                }
            }
        }

        // All set, install the features
        final Set<String> featuresToInstall = features.stream()
                .map(Feature::toInstallString)
                .collect(Collectors.toSet());
        try {
            LOG.info("Installing features: {}", featuresToInstall);
            featuresService.installFeatures(featuresToInstall, EnumSet.noneOf(FeaturesService.Option.class));
        } catch (Exception e) {
            extenderStatus.error("Failed to install one or more features", e);
        }

        karsDone(String.format("%s KAR features installed", featuresToInstall.size()), repairFailure);
    }

    private void karsDone(String message, String repairFailure) {
        if (repairFailure == null) {
            extenderStatus.karsDone(message);
        } else {
            extenderStatus.karsFailed(repairFailure);
        }
    }

    private List<URI> getFeaturesUrisForKar(String kar) {
        final Path featureConfig = karStorage.resolve(kar).resolve(FEATURE_CONFIG_FILE);
        final File featureConfigFile = featureConfig.toFile();

        if (!featureConfigFile.isFile()) {
            LOG.debug("Kar '{}' is installed, but the feature configuration is not yet written. " +
                    "Waiting up-to 30 seconds for it to show up...", kar);
            try {
                for (int i = 30; i > 0 && !featureConfigFile.isFile(); i--) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                LOG.info("Interrupted while waiting for {}. Assuming no feature repositories are used.", featureConfigFile);
                return Collections.emptyList();
            }
        }

        return readFeatureUris(kar, featureConfig);
    }

    private List<URI> readFeatureUris(String kar, Path featureConfig) {
        try {
            LOG.debug("Reading feature repository list for kar '{}' in: {}", kar, featureConfig);
            return Files.readAllLines(featureConfig).stream()
                    .filter(l -> !l.isBlank())
                    .map(String::trim)
                    .map(URI::create)
                    .collect(Collectors.toList());
        } catch (IOException | IllegalArgumentException e) {
            extenderStatus.warn("Cannot read feature repository list for kar '{}' in: {}. Assuming no feature repositories are used.",
                    kar, featureConfig, e);
            return Collections.emptyList();
        }
    }

    /**
     * The Karaf KAR deployer skips any .kar that already has a folder in data/kar, but the feature repositories
     * it registered are kept in data/cache. When the cache is cleaned at startup (karaf.clean.cache=true) the
     * repositories are lost while data/kar survives, so the .kar features are never installed again.
     * Install those .kars again from the deploy folder, the same way the deployer would have.
     *
     * @return a description of the .kars that could not be restored, or null if there were none
     */
    private String repairInstalledKars() {
        final List<String> installedKars;
        try {
            installedKars = karService.list();
        } catch (Exception e) {
            extenderStatus.warn("Enumerating installed .kar files failed. Skipping the .kar feature repository check.", e);
            return null;
        }

        final Set<URI> availableFeatureUris = getAvailableFeatureUris();
        final Map<String, List<URI>> missingFeatureUrisByKar = new LinkedHashMap<>();
        for (String kar : installedKars) {
            final Path featureConfig = karStorage.resolve(kar).resolve(FEATURE_CONFIG_FILE);
            if (!wasInstalledBeforeThisStart(featureConfig)) {
                // Either the deployer is installing it right now, or there is nothing we can restore
                continue;
            }

            final List<URI> missingFeatureUris = readFeatureUris(kar, featureConfig).stream()
                    .filter(uri -> !availableFeatureUris.contains(uri))
                    .collect(Collectors.toList());
            if (!missingFeatureUris.isEmpty()) {
                missingFeatureUrisByKar.put(kar, missingFeatureUris);
            }
        }

        final Map<String, Path> karFiles = new LinkedHashMap<>();
        missingFeatureUrisByKar.keySet().forEach(kar -> findKarFile(kar).ifPresent(karFile -> karFiles.put(kar, karFile)));

        final List<String> failedKars = new ArrayList<>();
        // Without the .kar file all we can do is add the feature repositories again, their artifacts are kept in
        // data/kar. Do these first, so that the .kars we install again can depend on them. The .kar may have been
        // installed with kar:install, or removed from the deploy folder while stopped, which leaves data/kar behind.
        // We can't tell these apart, so don't fail the health check for them.
        for (Map.Entry<String, List<URI>> entry : missingFeatureUrisByKar.entrySet()) {
            final String kar = entry.getKey();
            if (karFiles.containsKey(kar)) {
                continue;
            }
            LOG.warn("Feature repositories for kar '{}' are missing, most likely because the Karaf cache was cleaned, " +
                    "and the .kar is not in {}. Adding its feature repositories again without installing its features: {}. " +
                    "If the .kar was removed on purpose, run 'kar:uninstall {}'.",
                    kar, deployFolders, entry.getValue(), kar);
            for (URI featureUri : entry.getValue()) {
                try {
                    featuresService.addRepository(featureUri);
                } catch (Exception e) {
                    extenderStatus.warn("Failed to install feature repository: {}", featureUri, e);
                }
            }
        }

        // Install the .kars one at a time, like the deployer does, Karaf takes care of dependencies between them
        for (Map.Entry<String, Path> entry : karFiles.entrySet()) {
            if (Thread.currentThread().isInterrupted()) {
                LOG.info("Interrupted. Stopping the .kar feature repository check.");
                break;
            }
            final String kar = entry.getKey();
            LOG.info("Feature repositories for kar '{}' are missing, most likely because the Karaf cache was cleaned. " +
                    "Installing it again from: {}", kar, entry.getValue());
            try {
                karService.install(entry.getValue().toUri());
            } catch (Exception e) {
                extenderStatus.error("Failed to install kar '{}' again from: {}", kar, entry.getValue(), e);
                failedKars.add(kar);
            }
        }

        return failedKars.isEmpty() ? null : String.format("Failed to restore KAR(s) %s", failedKars);
    }

    private Set<URI> getAvailableFeatureUris() {
        final Set<URI> availableFeatureUris = new HashSet<>();
        try {
            for (Repository repository : featuresService.listRepositories()) {
                availableFeatureUris.add(repository.getURI());
            }
        } catch (Exception e) {
            extenderStatus.warn("Failed to retrieve feature repository details. " +
                    "Assuming there are no feature repositories installed.", e);
        }
        return availableFeatureUris;
    }

    private boolean wasInstalledBeforeThisStart(Path featureConfig) {
        try {
            return Files.getLastModifiedTime(featureConfig).toMillis() < jvmStartTime;
        } catch (IOException e) {
            return false;
        }
    }

    private Optional<Path> findKarFile(String kar) {
        return deployFolders.stream()
                .flatMap(folder -> KAR_EXTENSIONS.stream().map(ext -> folder.resolve(kar + ext)))
                .filter(Files::isRegularFile)
                .findFirst();
    }
}
