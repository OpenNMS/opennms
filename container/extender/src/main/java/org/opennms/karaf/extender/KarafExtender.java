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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.features.FeaturesService.Option;
import org.apache.karaf.kar.KarService;
import org.opennms.core.health.api.DefaultPassiveHealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;
import org.ops4j.pax.url.mvn.MavenResolver;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

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

    public static final String WAIT_FOR_KAR_ATTRIBUTE = "wait-for-kar";
    public static final String FEATURES_URI = "features.uri";
    public static final String FEATURES_BOOT = "features.boot";
    private static final String COMMENT_REGEX = "^\\s*(#.*)?$";
    private static final Pattern FEATURE_VERSION_PATTERN = Pattern.compile("(?<name>.+?)(/(?<version>.*))?(\\s+(?<attributes>.*))?");

    private final Path m_karafHome = Paths.get(System.getProperty("karaf.home"));
    private final Path m_repositories = m_karafHome.resolve("repositories");
    private final Path m_featuresBootDotD = m_karafHome.resolve(Paths.get("etc", "featuresBoot.d"));

    private ConfigurationAdmin m_configurationAdmin;
    private MavenResolver m_mavenResolver;
    private FeaturesService m_featuresService;
    private KarService m_karService;
    private DefaultPassiveHealthCheck m_defaultPassiveHealthCheck;

    private Thread m_installThread;
    private Thread m_karDependencyInstallThread;
    private ExtenderStatus m_extenderStatus = new ExtenderStatus();

    public void init() throws InterruptedException {
        Objects.requireNonNull(m_configurationAdmin, "configurationAdmin");
        Objects.requireNonNull(m_mavenResolver, "mavenResolver");
        Objects.requireNonNull(m_featuresService, "featuresService");
        Objects.requireNonNull(m_karService, "karService");
        Objects.requireNonNull(m_defaultPassiveHealthCheck, "healthCheckResponseCache");

        m_defaultPassiveHealthCheck.setResponse(new Response(Status.Starting), false);

        List<Repository> repositories;
        try {
            repositories = getRepositories();
        } catch (IOException e) {
            m_extenderStatus.error("Failed to retrieve the list of repositories. Aborting", e);
            return;
        }

        // Prepend the featuresBoot from the repository definitions
        List<Feature> featuresBoot = repositories.stream()
                .flatMap(r -> r.getFeaturesBoot().stream())
                .collect(Collectors.toList());
        try {
            featuresBoot.addAll(getFeaturesBoot());
        } catch (IOException e) {
            m_extenderStatus.error("Failed to retrieve the list of features to boot. Aborting", e);
            return;
        }

        // Filter the list of features
        filterFeatures(featuresBoot);

        if (!repositories.isEmpty()) {
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
                m_extenderStatus.error("Failed to update the list of Maven repositories to '{}'. Aborting",
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
                        m_extenderStatus.error("Failed to add feature repository '{}'. Skipping", featureUri, e);
                    }
                }
            }
        } else {
            LOG.debug("No repositories to install.");
        }

        final Set<String> featuresToInstall = featuresBoot.stream()
                .filter(f -> f.getKarDependency() == null) // Exclude any features depending on .kars
                .map(Feature::toInstallString)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (!featuresToInstall.isEmpty()) {
            // Because of the fix for the following issue, we need to call the
            // feature installation in another thread since this method is invoked
            // during a feature installation itself and feature installations are
            // now single-threaded.
            //
            // https://issues.apache.org/jira/browse/KARAF-3798
            // https://github.com/apache/karaf/pull/138
            //
            m_installThread = new Thread(() -> {
                try {
                    LOG.info("Installing features: {}", featuresToInstall);
                    m_featuresService.installFeatures(featuresToInstall, EnumSet.noneOf(Option.class));
                    m_extenderStatus.featuresDone(String.format("%s non-KAR features installed", featuresToInstall.size()));
                } catch (Exception e) {
                    m_extenderStatus.error("Failed to install one or more features", e);
                }
            });
            m_installThread.setName("Karaf-Extender-Feature-Install");
            m_installThread.start();
        } else {
            LOG.debug("No features to install.");
            m_extenderStatus.featuresDone("");
        }


        final List<Feature> featuresWithKarDependencies = featuresBoot.stream()
                .filter(f -> f.getKarDependency() != null)
                .collect(Collectors.toList());
        if (!featuresWithKarDependencies.isEmpty()) {
            final KarDependencyHandler karDependencyHandler = new KarDependencyHandler(featuresWithKarDependencies,
                    m_karService, m_featuresService, m_extenderStatus);
            m_karDependencyInstallThread = new Thread(karDependencyHandler);
            m_karDependencyInstallThread.setName("Karaf-Extender-Feature-Install-For-Kars");
            m_karDependencyInstallThread.start();
        } else {
            LOG.debug("No features with dependencies on .kar files to install.");
            m_extenderStatus.karsDone("");
        }
    }

    public void destroy() {
        if (m_installThread != null) {
            m_installThread.interrupt();
        }
        if (m_karDependencyInstallThread != null) {
            m_karDependencyInstallThread.interrupt();
        }
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
        final List<Path> repositoryPaths = getRepositoryFolders(m_repositories);

        final Path systemFolder = m_karafHome.resolve("system");
        if (isValidPath(systemFolder)) {
            repositoryPaths.add(systemFolder);
        } else {
            LOG.info("system folder {} does not exist.", systemFolder);
        }

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
                m_extenderStatus.error("Failed to generate one or more feature URIs for repository {}. Skipping",
                        repositoryPath, e);
            }
        }

        return repositories;
    }

    private static Map<String,String> parseAttributes(String attributes) {
        final Map<String,String> attributeMap = new LinkedHashMap<>();
        if (attributes == null) {
            return attributeMap;
        }

        for (String attributeKvp : attributes.split("\\s")) {
            String tokens[] = attributeKvp.split("=");
            if (tokens.length == 2) {
                attributeMap.put(tokens[0].trim(), tokens[1].trim());
            }
        }
        return attributeMap;
    }

    public List<Feature> getFeaturesIn(Path featuresBootFile) throws IOException {
        final List<Feature> features = Lists.newLinkedList();
        for (String line : getLinesIn(featuresBootFile)) {
            final Matcher m = FEATURE_VERSION_PATTERN.matcher(line);
            if (!m.matches()) {
                continue;
            }
            final Map<String,String> attributes = parseAttributes(m.group("attributes"));
            final Feature feature = Feature.builder()
                    .withName(m.group("name"))
                    .withVersion(m.group("version"))
                    .withKarDependency(attributes.get(WAIT_FOR_KAR_ATTRIBUTE))
                    .build();
            features.add(feature);
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
                featuresToExclude.add(Feature.builder()
                                .withName(feature.getName().substring(1))
                                .withVersion(feature.getVersion())
                                .build());
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

    private static List<Path> getRepositoryFolders(final Path folder) throws IOException {
        final List<Path> paths = Lists.newLinkedList();

        if (!folder.toFile().exists()) {
            LOG.info("Repository folder {} does not exist. No sub-repositories will be added.", folder);
            return paths;
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folder)) {
            for (Path path : directoryStream) {
                if (isValidPath(path)) {
                    paths.add(path);
                }
            }
        }
        Collections.sort(paths);

        return paths;
    }

    private static boolean isValidPath(final Path path) {
        if (!path.toFile().isDirectory()) {
            // Ignore non-directories
            return false;
        }
        if (path.getFileName().toString().startsWith(".")) {
            // Ignore dot folders
            return false;
        }
        return true;
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

    public void setKarService(KarService karService) {
        m_karService = karService;
    }

    public void setDefaultPassiveHealthCheck(DefaultPassiveHealthCheck defaultPassiveHealthCheck) {
        m_defaultPassiveHealthCheck = defaultPassiveHealthCheck;
    }

    public class ExtenderStatus {
        private String featuresToInstallDone = null;
        private String karDependencyInstallDone = null;

        public void info(String messagePattern, Object... argArray) {
            LOG.info(messagePattern, argArray);
            setResponse(Status.Starting, messagePattern, argArray);
        }

        public void warn(String messagePattern, Object... argArray) {
            LOG.warn(messagePattern, argArray);
            setResponse(Status.Starting, messagePattern, argArray);
        }

        public void error(String messagePattern, Object... argArray) {
            LOG.error(messagePattern, argArray);
            setResponse(Status.Failure, messagePattern, argArray);
        }

        private void setResponse(Status status, String messagePattern, Object[] argArray) {
            var formattingTuple = MessageFormatter.arrayFormat(messagePattern, argArray);
            var failureMessage = new StringBuffer(formattingTuple.getMessage());
            var throwable = formattingTuple.getThrowable();
            if (throwable != null) {
                failureMessage.append(": ");
                failureMessage.append(throwable.getMessage());
            }
            m_defaultPassiveHealthCheck.setResponse(new Response(status, failureMessage.toString()), false);
        }

        public void featuresDone(String message) {
            featuresToInstallDone = Objects.requireNonNull(message);
            areWeCompletelyDone();
        }

        public void karsDone(String message) {
            karDependencyInstallDone = Objects.requireNonNull(message);
            areWeCompletelyDone();
        }

        private void areWeCompletelyDone() {
            if (featuresToInstallDone != null && karDependencyInstallDone != null) {
                StringBuffer message = new StringBuffer();
                if (!featuresToInstallDone.isEmpty()) {
                    message.append(featuresToInstallDone);
                    message.append(". ");
                }
                if (!karDependencyInstallDone.isEmpty()) {
                    message.append(karDependencyInstallDone);
                    message.append(". ");
                }
                if (message.length() > 0) {
                    m_defaultPassiveHealthCheck.setResponse(new Response(Status.Success, message.toString()), false);
                } else {
                    m_defaultPassiveHealthCheck.setResponse(new Response(Status.Success), false);
                }
            }
        }
    }
}
