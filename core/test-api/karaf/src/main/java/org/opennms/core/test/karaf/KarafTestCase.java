/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.core.test.karaf;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.CoreOptions.systemPackages;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.debugConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.URI;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.security.auth.Subject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.api.console.SessionFactory;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public abstract class KarafTestCase {

    private static Logger LOG = LoggerFactory.getLogger(KarafTestCase.class);

    public static final String MIN_RMI_SERVER_PORT = "44444";
    public static final String MAX_RMI_SERVER_PORT = "66666";
    public static final String MIN_HTTP_PORT = "9080";
    public static final String MAX_HTTP_PORT = "9999";
    public static final String MIN_RMI_REG_PORT = "1099";
    public static final String MAX_RMI_REG_PORT = "9999";
    public static final String MIN_SSH_PORT = "8101";
    public static final String MAX_SSH_PORT = "8888";

    protected static String getKarafVersion() {
        final String karafVersion = System.getProperty("karafVersion", "4.1.5");
        Objects.requireNonNull(karafVersion, "Please define a system property 'karafVersion'.");
        return karafVersion;
    }

    protected static File findPom(final File root) {
        final File absoluteRoot = root.getAbsoluteFile();
        LOG.error("findPom: {}", absoluteRoot);
        final File pomFile = new File(absoluteRoot, "pom.xml");
        if (pomFile.exists()) {
            return pomFile;
        } else if (absoluteRoot.getParentFile() != null) {
            return findPom(absoluteRoot.getParentFile());
        } else {
            return null;
        }
    }

    protected static String getOpenNMSVersion() {
        final File pomFile = findPom(new File("."));
        LOG.error("getOpenNMSVersion pom file: {}", pomFile);
        Objects.requireNonNull(pomFile, "Unable to find pom.xml!  This should not happen...");
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder(); 
            final Document doc = db.parse(pomFile);
            final Element root = doc.getDocumentElement();
            final NodeList versions = root.getElementsByTagName("version");
            final String version = versions.item(0).getFirstChild().getNodeValue();
            LOG.error("getOpenNMSVersion found version tag: {} ", version);
            return version;
        } catch (final Exception e) {
            LOG.error("Failed to get version from POM.", e);
            return null;
        }
    }

    @Inject
    protected BundleContext bundleContext;

    @Inject
    protected FeaturesService featuresService;

    @Inject
    protected SessionFactory sessionFactory;

    /**
     * This {@link ProbeBuilder} can be used to add OSGi metadata to the test
     * probe bundle. We only use it to give the bundle a nice human-readable name
     * of "org.opennms.core.test.karaf.test".
     */
    @ProbeBuilder
    public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
        probe.setHeader(Constants.BUNDLE_MANIFESTVERSION, "2");
        probe.setHeader(Constants.BUNDLE_SYMBOLICNAME, "org.opennms.core.test.karaf.test");
        //probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "org.opennms.core.test.karaf,*,org.apache.felix.service.*;status=provisional");
        return probe;
    }

    private static int getAvailablePort(int min, int max) {
        for (int i = min; i <= max; i++) {
            try (ServerSocket socket = new ServerSocket(i)) {
                return socket.getLocalPort();
            } catch (Throwable e) {}
        }
        throw new IllegalStateException("Can't find an available network port");
    }

    /**
     * This is the default {@link Configuration} for any Pax Exam tests that
     * use this abstract base class. If you wish to add more Configuration parameters,
     * you should call {@link #configAsList()}, append the {@link Option} values
     * to the list, and then return it in a {@link Configuration} function that
     * overrides {@link #config()}.
     */
    @Configuration
    public Option[] config() {
        return configAsArray();
    }

    protected List<Option> configAsList() {
        return new ArrayList<Option>(Arrays.asList(configAsArray()));
    }

    protected Option[] configAsArray() {
        String httpPort = Integer.toString(getAvailablePort(Integer.parseInt(MIN_HTTP_PORT), Integer.parseInt(MAX_HTTP_PORT)));
        String rmiRegistryPort = Integer.toString(getAvailablePort(Integer.parseInt(MIN_RMI_REG_PORT), Integer.parseInt(MAX_RMI_REG_PORT)));
        String rmiServerPort = Integer.toString(getAvailablePort(Integer.parseInt(MIN_RMI_SERVER_PORT), Integer.parseInt(MAX_RMI_SERVER_PORT)));
        String sshPort = Integer.toString(getAvailablePort(Integer.parseInt(MIN_SSH_PORT), Integer.parseInt(MAX_SSH_PORT)));

        // Create a new empty file
        File emptyFile = new File("target/emptyFile");
        try {
            emptyFile.createNewFile();
        } catch (IOException e) {
            LOG.warn("Could not create empty file");
        }

        Option[] options = new Option[]{
            // Use Karaf as the container
            karafDistributionConfiguration().frameworkUrl(
                getFrameworkUrl())
                .karafVersion(getKarafVersion())
                .name("Apache Karaf")
                .unpackDirectory(new File("target/paxexam/")
            )
                // Turn off using the deploy folder or stream bundle provisioning
                // won't happen before the probe bundle executes, causing problems
                // like {@link NoClassDefFoundError}.
                .useDeployFolder(false),

            // Pack this parent class from src/main/java into a stream bundle
            // so that it is accessible inside the container
            provision(
                 bundle()
                     .add(KarafTestCase.class)
                     .set(Constants.BUNDLE_MANIFESTVERSION, "2")
                     .set(Constants.BUNDLE_SYMBOLICNAME, "org.opennms.core.test.karaf")
                     .set(Constants.DYNAMICIMPORT_PACKAGE, "*")
                     .set(Constants.EXPORT_PACKAGE, "org.opennms.core.test.karaf")
                     .build()
            ),

            //keepRuntimeFolder(),

            // Set logging to INFO
            logLevel(LogLevelOption.LogLevel.INFO),

            /**
             * CAUTION: Do not use editConfigurationFileExtend(), it appears to overwrite its own changes
             * if there are multiple statements.
             */
            editConfigurationFilePut("etc/org.ops4j.pax.url.mvn.cfg", "org.ops4j.pax.url.mvn.defaultRepositories",
                String.join(",", new String[] {
                    "file:${karaf.home}/${karaf.default.repository}@snapshots@id=karaf.${karaf.default.repository}",

                    // This path needs to match the path in the POM to the repo created by the features-maven-plugin's 'add-features-to-repo' execution, ie:
                    // <repository>target/paxexam/test-repo</repository>
                    //
                    // TODO: Make it possible for tests to override these paths with the path where their 'add-features-to-repo' execution is creating a repo
                    //
                    "file:${karaf.home}/../test-repo@snapshots@id=default-repo",
                    // These repositories are unpacked by the opennms-full-assembly project's build
                    // for final integration testing
                    "file:${karaf.home}/../../opennms-repo@snapshots@id=opennms-repo",
                    "file:${karaf.home}/../../experimental-repo@snapshots@id=experimental-repo",
                    "file:${karaf.home}/../../minion-core-repo@snapshots@id=minion-core-repo",
                    "file:${karaf.home}/../../minion-default-repo@snapshots@id=minion-default-repo"
                })
            ),

            // Disable all standard internet repositories so that we only rely on the defaultRepositories
            editConfigurationFilePut("etc/org.ops4j.pax.url.mvn.cfg", "org.ops4j.pax.url.mvn.repositories", ""),

            // TODO: I'm not sure what generates this directory
            //editConfigurationFileExtend("etc/org.ops4j.pax.url.mvn.cfg", "org.ops4j.pax.url.mvn.localRepository", "file:${karaf.home}/../opennms-repo@snapshots@id=opennms-repo"),

            //editConfigurationFilePut("etc/org.apache.karaf.features.cfg", "featuresBoot", "config,ssh,http,http-whiteboard,exam"),

            // Change the all network ports so they don't conflict with a running OpenNMS instance
            // or previously run Karaf integration tests
            editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port", httpPort),
            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", rmiRegistryPort),
            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort", rmiServerPort),
            editConfigurationFilePut("etc/org.apache.karaf.shell.cfg", "sshPort", sshPort),

            // Work around bug KARAF-5251
            editConfigurationFilePut("etc/startup.properties", "mvn:net.java.dev.jna/jna/4.5.0", "5"),
            editConfigurationFilePut("etc/startup.properties", "mvn:net.java.dev.jna/jna-platform/4.5.0", "5"),

            // This port is already being allocated according to an org.ops4j.net.FreePort call
            //editConfigurationFilePut("etc/system.properties", "org.ops4j.pax.exam.rbc.rmi.port", paxExamRmiRegistryPort),

            // Work around bug KARAF-5223, should be unnecessary once we upgrade past Karaf 4.1.1
            replaceConfigurationFile("etc/shell.init.script", emptyFile),
        };

        // Work around bug KARAF-5384
        // If there is a MINA JAR available, then add MINA to the startup classpath
        // TODO: Don't hardcode the version number here
        File minaJar = new File("target/dependency/mina-core-2.0.16.jar");
        if (minaJar.exists()) {
            options = Arrays.copyOf(options, options.length + 2);
            options[options.length - 2] = replaceConfigurationFile("system/org/apache/mina/mina-core/2.0.16/mina-core-2.0.16.jar", minaJar);
            options[options.length - 1] = editConfigurationFilePut("etc/startup.properties", "mvn:org.apache.mina/mina-core/2.0.16", "10");
        }

        if (Boolean.valueOf(System.getProperty("debug"))) {
            options = Arrays.copyOf(options, options.length + 1);
            options[options.length -1] = debugConfiguration("8889", true);
        }

        String[] systemPackages = getSystemPackages();
        if (systemPackages.length > 0) {
            options = Arrays.copyOf(options, options.length + 1);
            options[options.length -1] = systemPackages(systemPackages);
        }

        return options;
    }

    /**
     * Use the vanilla Apache Karaf container. Override this method to use
     * a different Karaf-compatible framework artifact.
     */
    protected MavenUrlReference getFrameworkUrl() {
        return maven()
                .groupId("org.apache.karaf")
                .artifactId("apache-karaf")
                .type("tar.gz")
                .version(getKarafVersion());
    }

    /**
     * Override this method to add system packages to the test container.
     */
    protected String[] getSystemPackages() {
        return new String[0];
    }

    protected void addFeaturesUrl(String url) {
        try {
            featuresService.addRepository(URI.create(url));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void installFeature(String featureName) {
        try {
            LOG.info("Installing feature {}", featureName);
            featuresService.installFeature(featureName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void installFeature(String featureName, EnumSet<FeaturesService.Option> options) {
        try {
            LOG.info("Installing feature {}", featureName);
            featuresService.installFeature(featureName, options);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void installFeature(String featureName, String version) {
        try {
            LOG.info("Installing feature {}/{}", featureName, version);
            featuresService.installFeature(featureName, version);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a shell command and returns output as a String.
     * Commands have a default timeout of 10 seconds.
     *
     * @param command
     * @return
     */
    protected String executeCommand(final String command) {
        try (
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final PrintStream printStream = new PrintStream(byteArrayOutputStream);
            final PrintStream errStream = new PrintStream(byteArrayOutputStream);
        ) {
            final ExecutorService executor = Executors.newCachedThreadPool();

            Subject subject = new Subject();
            subject.getPrincipals().add(new RolePrincipal("admin"));
            return Subject.doAs(subject, new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    final Session session = sessionFactory.create(System.in, printStream, errStream);

                    LOG.info("Command: {}", command);

                    FutureTask<String> commandFuture = new FutureTask<String>(new Callable<String>() {
                        public String call() {
                            try {
                                session.execute(command);
                            } catch (Exception e) {
                                e.printStackTrace(System.err);
                            }
                            printStream.flush();
                            errStream.flush();
                            return byteArrayOutputStream.toString();
                        }
                    });

                    try {
                        executor.submit(commandFuture);
                        String response = commandFuture.get(10, TimeUnit.SECONDS);
                        LOG.info("Response: {}", response);
                        return response;
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                        return "SHELL COMMAND TIMED OUT: " + command;
                    }
                }
            });
        } catch (Exception e) {
            LOG.error("Error while executing command", e);
            throw new RuntimeException(e);
        }
    }

    protected <T> T getOsgiService(Class<T> type) {
        ServiceReference<T> serviceReference = bundleContext.getServiceReference(type);
        if (serviceReference != null) {
            return type.cast(bundleContext.getService(serviceReference));
        }
        return null;
    }
}
