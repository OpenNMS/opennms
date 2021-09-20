/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

    private static final String FEATURE_CONFIG_FILE = "features.cfg";
    private static final String KAR_STORAGE = System.getProperty("karaf.data") + File.separator + "kar";

    public KarDependencyHandler(List<Feature> features, KarService karService, FeaturesService featuresService) {
        this.features = Objects.requireNonNull(features);
        this.karService = Objects.requireNonNull(karService);
        this.featuresService = Objects.requireNonNull(featuresService);
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
                LOG.info("Waiting on {}", karsToWaitFor);
                karsToWaitFor.removeAll(karService.list());
                if (karsToWaitFor.isEmpty()) {
                    break;
                }
            } catch (Exception e) {
                LOG.warn("Enumerating installed .kar files failed. Will retry in {}ms.", KAR_LIST_SLEEP_MS, e);
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
            LOG.warn("Failed to retrieve feature repository details. " +
                    "Assuming there are not feature repositories installed.", e);
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
                    LOG.error("Failed to install feature repository: {}", featureUri, e);
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
            LOG.error("Failed to install one or more features.", e);
        }
    }

    private List<URI> getFeaturesUrisForKar(String kar) {
        final Path featureConfig = Paths.get(KAR_STORAGE, kar, FEATURE_CONFIG_FILE);
        final File featureConfigFile = featureConfig.toFile();

        if (!featureConfigFile.isFile()) {
            LOG.debug("Kar '{}' is installed, but the feature configuration is not yet written. " +
                    "Waiting up-to 30 seconds for it to show up...");
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
            LOG.warn("Cannot read feature repository list for kar '{}' in: {}. Assuming no feature repositories are used.",
                    kar, featureConfig, e);
            return Collections.emptyList();
        }
    }
}
