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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
    private static final String KAR_STORAGE = System.getProperty("karaf.data") + File.separator + "kar";

    public KarDependencyHandler(List<Feature> features, KarService karService, FeaturesService featuresService, KarafExtender.ExtenderStatus extenderStatus) {
        this.features = Objects.requireNonNull(features);
        this.karService = Objects.requireNonNull(karService);
        this.featuresService = Objects.requireNonNull(featuresService);
        this.extenderStatus = Objects.requireNonNull(extenderStatus);
    }

    @Override
    public void run() {
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
        final Set<URI> availableFeatureUris = new HashSet<>();
        try {
            for (Repository repository : featuresService.listRepositories()) {
                availableFeatureUris.add(repository.getURI());
            }
        } catch (Exception e) {
            extenderStatus.warn("Failed to retrieve feature repository details. " +
                    "Assuming there are no feature repositories installed.", e);
        }

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

        extenderStatus.karsDone(String.format("%s KAR features installed", featuresToInstall.size()));
    }

    private List<URI> getFeaturesUrisForKar(String kar) {
        final Path featureConfig = Paths.get(KAR_STORAGE, kar, FEATURE_CONFIG_FILE);
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

        try {
            LOG.debug("Reading feature repository list for kar '{}' in: {}", kar, featureConfig);
            return Files.readAllLines(featureConfig).stream()
                    .map(URI::create)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            extenderStatus.warn("Cannot read feature repository list for kar '{}' in: {}. Assuming no feature repositories are used.",
                    kar, featureConfig, e);
            return Collections.emptyList();
        }
    }
}
