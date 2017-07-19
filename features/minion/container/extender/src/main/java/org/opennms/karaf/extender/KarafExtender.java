/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.features.FeaturesService.Option;
import org.ops4j.pax.url.mvn.MavenResolver;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Allows Maven repositories, feature repositories and features to boot
 * to be extended using a .d style configuration format.
 *
 * @author jwhite
 */
public class KarafExtender {
    private static final Logger LOG = LoggerFactory.getLogger(KarafExtender.class);
    private static final String PAX_MVN_PID = "org.ops4j.pax.url.mvn";
    private static final String PAX_MVN_REPOSITORIES = "org.ops4j.pax.url.mvn.repositories";

    public static final String FEATURES_URI = "features.uri";
    public static final String FEATURES_BOOT = "features.boot";
    private static final String COMMENT_REGEX = "^\\s*(#.*)?$";
    private static final Pattern FEATURE_VERSION_PATTERN = Pattern.compile("(.*?)(/(.*))?");

    private final Path m_karafHome = Paths.get(System.getProperty("karaf.home"));
    private final Path m_repositories = m_karafHome.resolve("repositories");
    private final Path m_featuresBootDotD = m_karafHome.resolve(Paths.get("etc", "featuresBoot.d"));

    private ConfigurationAdmin m_configurationAdmin;
    private MavenResolver m_mavenResolver;
    private FeaturesService m_featuresService;

    public void init() throws InterruptedException {
        Objects.requireNonNull(m_configurationAdmin, "configurationAdmin");
        Objects.requireNonNull(m_mavenResolver, "mavenResolver");
        Objects.requireNonNull(m_featuresService, "featuresService");

        List<Repository> repositories;
        try {
            repositories = getRepositories();
        } catch (IOException e) {
            LOG.error("Failed to retrieve the list of repositories. Aborting.", e);
            return;
        }

        // Prepend the featuresBoot from the repository definitions
        List<Feature> featuresBoot = repositories.stream()
                .flatMap(r -> r.getFeaturesBoot().stream())
                .collect(Collectors.toList());
        try {
            featuresBoot.addAll(getFeaturesBoot());
        } catch (IOException e) {
            LOG.error("Failed to retrieve the list of features to boot. Aborting.", e);
            return;
        }

        // Filter the list of features
        filterFeatures(featuresBoot);

        // Build a comma separated list of our Maven repositories
        final StringBuilder mavenReposSb = new StringBuilder();
        for (Repository repository : repositories) {
            if (mavenReposSb.length() != 0) {
                mavenReposSb.append(",");
            }
            mavenReposSb.append(repository.toMavenUri());
        }
        final String mavenRepos = mavenReposSb.toString();

        LOG.info("Updating Maven repositories to include: {}", mavenRepos);
        try {
            final Configuration config = m_configurationAdmin.getConfiguration(PAX_MVN_PID);
            if (config == null) {
                throw new IOException("The OSGi configuration (admin) registry was found for pid " + PAX_MVN_PID +
                        ", but a configuration could not be located/generated.  This shouldn't happen.");
            }
            final Dictionary<String, Object> props = config.getProperties();
            if (!mavenRepos.equals(props.get(PAX_MVN_REPOSITORIES))) {
                props.put(PAX_MVN_REPOSITORIES, mavenRepos);
                config.update(props);
            }
        } catch (IOException e) {
            LOG.error("Failed to update the list of Maven repositories to '{}'. Aborting.",
                    mavenRepos, e);
            return;
        }

        // The configuration update is async, we need to wait for the feature URLs to be resolvable before we use them
        LOG.info("Waiting up-to 30 seconds for the Maven repositories to be updated...");
        // Attempting to resolve a missing features writes an exception to the log
        // We sleep fix a fixed amount of time before our first try in order to help minimize the logged
        // exceptions, even if we catch them
        Thread.sleep(2000);
        for (int i = 28; i > 0 && !canResolveAllFeatureUris(repositories); i--) {
            Thread.sleep(1000);
        }

        for (Repository repository : repositories) {
            for (URI featureUri : repository.getFeatureUris()) {
                try {
                    LOG.info("Adding feature repository: {}", featureUri);
                    m_featuresService.addRepository(featureUri);
                } catch (Exception e) {
                    LOG.error("Failed to add feature repository '{}'. Skipping.", featureUri, e);
                }
            }
        }

        final Set<String> featuresToInstall = featuresBoot.stream()
            .map(f -> f.getVersion() != null ? f.getName() + "/" + f.getVersion() : f.getName())
            .collect(Collectors.toCollection(LinkedHashSet::new));

        // Because of the fix for the following issue, we need to call the
        // feature installation in another thread since this method is invoked
        // during a feature installation itself and feature installations are
        // now single-threaded.
        //
        // https://issues.apache.org/jira/browse/KARAF-3798
        // https://github.com/apache/karaf/pull/138
        //
        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    LOG.info("Installing features: {}", featuresToInstall);
                    m_featuresService.installFeatures(featuresToInstall, EnumSet.noneOf(Option.class));
                } catch (Exception e) {
                    LOG.error("Failed to install one or more features.", e);
                }
            }
        });
    }

    private boolean canResolveAllFeatureUris(List<Repository> repositories) {
        for (Repository repository : repositories) {
            for (URI featureUri : repository.getFeatureUris()) {
                try {
                    if (m_mavenResolver.resolve(featureUri.toString()) == null) {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<Repository> getRepositories() throws IOException {
        final List<Path> repositoryPaths = getFoldersIn(m_repositories);

        final List<Repository> repositories = Lists.newLinkedList();
        for (Path repositoryPath : repositoryPaths) {
            try {
                List<URI> featureUris = Lists.newLinkedList();
                Path featuresUriPath = repositoryPath.resolve(FEATURES_URI);
                if (featuresUriPath.toFile().isFile()) {
                    for (String line : getLinesIn(featuresUriPath)) {
                        featureUris.add(new URI(line));
                    }
                }

                List<Feature> featuresBoot;
                Path featuresBootPath = repositoryPath.resolve(FEATURES_BOOT);
                if (featuresBootPath.toFile().isFile()) {
                    featuresBoot = getFeaturesIn(featuresBootPath);
                } else {
                    featuresBoot = Collections.emptyList();
                }
                repositories.add(new Repository(repositoryPath, featureUris, featuresBoot));
            } catch (URISyntaxException e) {
                LOG.error("Failed to generate one or more feature URIs for repository {}. Skipping.",
                        repositoryPath, e);
            }
        }

        return repositories;
    }

    public List<Feature> getFeaturesIn(Path featuresBootFile) throws IOException {
        final List<Feature> features = Lists.newLinkedList();
        for (String line : getLinesIn(featuresBootFile)) {
            final Matcher m = FEATURE_VERSION_PATTERN.matcher(line);
            if (!m.matches()) {
                continue;
            }
            if (m.group(3) == null) {
                features.add(new Feature(m.group(1)));
            } else {
                features.add(new Feature(m.group(1), m.group(3)));
            }
        }
        return features;
    }

    public List<Feature> getFeaturesBoot() throws IOException {
        final List<Feature> features = Lists.newLinkedList();
        for (Path featuresBootFile : getFilesIn(m_featuresBootDotD)) {
            features.addAll(getFeaturesIn(featuresBootFile));
        }
        return features;
    }

    /**
     * Any feature that starts with '!' will be removed
     * from the list, and will remove all other features
     * with the same name and version.
     *
     * i.e. if the feature list contains:
     * <pre>
     *   feature-a
     *   feature-b
     *   !feature-b
     *   !feature-c
     * </pre>
     *
     * after calling this function, the list will contain:
     * <pre>
     *   feature-a
     * </pre>
     *
     * @param features
     */
    public void filterFeatures(List<Feature> features) {
        // Determine the set of features to remove
        final Set<Feature> featuresToExclude = new HashSet<>();
        final Iterator<Feature> it = features.iterator();
        while (it.hasNext()) {
            final Feature feature = it.next();
            if (feature.getName().startsWith("!") && feature.getName().length() > 1) {
                featuresToExclude.add(new Feature(feature.getName().substring(1), feature.getVersion()));
                it.remove();
            }
        }
        // Remove all matching features
        features.removeAll(featuresToExclude);
    }

    private static List<Path> getFilesIn(Path folder) throws IOException {
        final List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folder)) {
            for (Path path : directoryStream) {
                if (path.toFile().isDirectory()) {
                    // Ignore directories
                    continue;
                }
                if (path.getFileName().toString().startsWith(".")) {
                    // Ignore dot files
                    continue;
                }
                files.add(path);
            }
        }
        Collections.sort(files);

        return files;
    }

    private static List<Path> getFoldersIn(Path folder) throws IOException {
        final List<Path> paths = Lists.newLinkedList();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folder)) {
            for (Path path : directoryStream) {
                if (!path.toFile().isDirectory()) {
                    continue;
                }
                if (path.getFileName().toString().startsWith(".")) {
                    // Ignore dot folders
                    continue;
                }
                paths.add(path);
            }
        }
        Collections.sort(paths);

        return paths;
    }

    private static List<String> getLinesIn(Path file) throws IOException {
        return Files.readAllLines(file).stream()
            .filter(l -> !l.matches(COMMENT_REGEX))
            .collect(Collectors.toList());
    }

    public void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        m_configurationAdmin = configurationAdmin;
    }

    public void setMavenResolver(MavenResolver mavenResolver) {
        m_mavenResolver = mavenResolver;
    }

    public void setFeaturesService(FeaturesService featuresService) {
        m_featuresService = featuresService;
    }
}
