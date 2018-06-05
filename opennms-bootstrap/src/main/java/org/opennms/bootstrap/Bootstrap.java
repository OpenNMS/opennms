/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2015 The OpenNMS Group, Inc.
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

package org.opennms.bootstrap;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.server.RMISocketFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * Bootstrap application for starting OpenNMS.
 */
public abstract class Bootstrap {

    /**
     * We don't have the class-loader setup with all of our logging libraries yet,
     * so we resort to logging to System.err when this flag is set
     */
    private static final boolean DEBUG = Boolean.getBoolean("opennms.bootstrap.debug");

    protected static final Path VERSION_PROPERTIES = Paths.get("jetty-webapps", "opennms", "WEB-INF", "version.properties");
    protected static final String BOOT_PROPERTIES_NAME = "bootstrap.properties";
    protected static final String RRD_PROPERTIES_NAME = "rrd-configuration.properties";
    protected static final String LIBRARY_PROPERTIES_NAME = "libraries.properties";
    protected static final String OPENNMS_PROPERTIES_NAME = "opennms.properties";
    protected static final String OPENNMS_PROPERTIES_D_NAME = "opennms.properties.d";
    protected static final String OPENNMS_HOME_PROPERTY = "opennms.home";

    /**
     * Matches any file that is a directory.
     */
    private static FileFilter m_dirFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    };

    /**
     * Matches any file that has a name ending in ".jar".
     */
    private static FilenameFilter m_jarFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    };
    private static HostRMIServerSocketFactory m_rmiServerSocketFactory;

    /**
     * Matches any file that has a name ending in ".properties".
     */
    private static FilenameFilter m_propertiesFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".properties");
        }
    };

    /**
     * A list of sub-folders found in $OPENNMS_HOME that should always be excluded
     * from the class-loader
     */
    private static final List<String> OPENNMS_HOME_CLASSLOADER_EXCLUDES = Arrays.asList(
            "data", // Temporary directory and Karaf cache
            "logs", // Logz
            "jetty-webapps",  // Handled by Jetty
            "share", // RRD files, MIBs, Reports, etc..
            "system" // Handled by Karaf
            );

    /**
     * The list of canonical files that should be excluded from the class-loader.
     */
    private static final Set<File> CLASSLOADER_DIRECTORY_EXCLUDES = new HashSet<>();

    static {
        // Here we determine the canonical files for the excluded sub-folders under
        // $OPENNMS_HOME, and add them to the list of excludes
        try {
            final File opennmsHome = Bootstrap.findOpenNMSHome();
            for (final String subfolder : OPENNMS_HOME_CLASSLOADER_EXCLUDES) {
                final File dir = new File(opennmsHome, subfolder);
                try {
                    CLASSLOADER_DIRECTORY_EXCLUDES.add(dir.getCanonicalFile());
                } catch (IOException e) {
                    if (DEBUG) {
                        System.err.printf("Failed to determine the canonical file for '%s'. This directory will not be excluded from the class-path.\n",  dir);
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                System.err.println("Failed to determine $OPENNMS_HOME. Skipping class-loader excludes.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Create a ClassLoader with the JARs found in dirStr.
     *
     * @param dirStr
     *            List of directories to search for JARs, separated by
     *            {@link java.io.File#pathSeparator File.pathSeparator}.
     * @param recursive
     *            Whether to recurse into subdirectories of the directories in
     *            dirStr.
     * @param append Append the URLs of the current {@link java.lang.Thread#getContextClassLoader())
     *            to this classloader.
     * @returns A new ClassLoader containing the found JARs
     * @return a {@link java.lang.ClassLoader} object.
     * @throws java.net.MalformedURLException if any.
     */
    public static ClassLoader loadClasses(String dirStr, boolean recursive, boolean append) throws MalformedURLException {
        LinkedList<URL> urls = new LinkedList<>();

        if (append) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            for (final URL u : ((URLClassLoader) classLoader).getURLs()) {
                urls.add(u);
            }
        }
        StringTokenizer toke = new StringTokenizer(dirStr, File.pathSeparator);
        while (toke.hasMoreTokens()) {
            String token = toke.nextToken();
            loadClasses(new File(token), recursive, urls);
        }

        if (DEBUG) {
            System.err.println("urls:");
            for (final URL u : urls) {
                System.err.println("  " + u);
            }
        }
        return newClassLoader(urls);
    }

    /**
     * Create a ClassLoader with the JARs found in dir.
     *
     * @param dir
     *            Directory to search for JARs
     * @param recursive
     *            Whether to recurse into subdirectories of dir
     * @returns A new ClassLoader containing the found JARs
     * @return a {@link java.lang.ClassLoader} object.
     * @throws java.net.MalformedURLException if any.
     */
    public static ClassLoader loadClasses(File dir, boolean recursive)
            throws MalformedURLException {
        LinkedList<URL> urls = new LinkedList<>();
        loadClasses(dir, recursive, urls);
        return newClassLoader(urls);
    }

    /**
     * Create a ClassLoader with the list of URLs found in urls.
     *
     * @param urls
     *            List of URLs to add to the ClassLoader's search list.
     * @returns A new ClassLoader with the specified search list
     * @return a {@link java.lang.ClassLoader} object.
     */
    public static ClassLoader newClassLoader(List<URL> urls) {
        URL[] urlsArray = urls.toArray(new URL[0]);

        return URLClassLoader.newInstance(urlsArray);
    }

    /**
     * Add JARs found in dir to the LinkedList urls.
     *
     * @param dir
     *            Directory to search for JARs
     * @param recursive
     *            Whether to recurse into subdirectories of the directory in
     *            dir
     * @param urls
     *            LinkedList to append found JARs onto
     * @throws java.net.MalformedURLException if any.
     */
    public static void loadClasses(File dir, boolean recursive, List<URL> urls) throws MalformedURLException {
        try {
            // Attempt to resolve the canonical file for the given directory
            // and avoid loading any classes if the corresponding directory
            // is excluded
            final File canonicalDir = dir.getCanonicalFile();
            if (CLASSLOADER_DIRECTORY_EXCLUDES.contains(canonicalDir)) {
                if (DEBUG) {
                   System.err.println("Skipping excluded directory: " + canonicalDir);
                }
                return;
            }
        } catch (IOException e) {
            if (DEBUG) {
                System.err.printf("Failed to determine the canonical path for '%s'.\n", dir);
                e.printStackTrace();
            }
        }

        // Add the directory
        urls.add(dir.toURI().toURL());

        if (recursive) {
            // Descend into sub-directories
            File[] dirlist = dir.listFiles(m_dirFilter);
            if (dirlist != null) {
            	Arrays.sort(dirlist);
                for (File childDir : dirlist) {
                    loadClasses(childDir, recursive, urls);
                }
            }
        }

        // Add individual JAR files
        File[] children = dir.listFiles(m_jarFilter);
        if (children != null) {
        	Arrays.sort(children);
            for (File childFile : children) {
                urls.add(childFile.toURI().toURL());
            }
        }
    }

    /**
     * Retrieves the list of configuration files containing
     * system properties to be set.
     *
     * The system properties must be set in the same order
     * as they are returned here.
     *
     * @param opennmsHome the OpenNMS home directory
     * @return a list of property files
     */
    protected static List<File> getPropertiesFiles(File opennmsHome) {
        final File etc = new File(opennmsHome, "etc");
        final List<File> propertiesFiles = new ArrayList<>();
        propertiesFiles.add(opennmsHome.toPath().resolve(VERSION_PROPERTIES).toFile());
        propertiesFiles.add(new File(etc, BOOT_PROPERTIES_NAME));
        propertiesFiles.add(new File(etc, RRD_PROPERTIES_NAME));
        propertiesFiles.add(new File(etc, LIBRARY_PROPERTIES_NAME));
        propertiesFiles.add(new File(etc, OPENNMS_PROPERTIES_NAME));

        // Add all of the .properties files in etc/opennms.properties.d/
        final File opennmsPropertiesDotD = new File(etc, OPENNMS_PROPERTIES_D_NAME);
        if (opennmsPropertiesDotD.isDirectory()) {
            final File[] properties = opennmsPropertiesDotD.listFiles(m_propertiesFilter);
            if (properties != null) {
                Arrays.sort(properties);
                propertiesFiles.addAll(Arrays.asList(properties));
            }
        }

        return propertiesFiles;
    }

    /**
     * Load default properties from the specified OpenNMS home into the
     * system properties.
     *
     * @param opennmsHome the OpenNMS home directory
     * @throws IOException
     */
    protected static void loadSystemProperties(File opennmsHome) throws IOException {
        // Grab a copy of the set of existing system properties:
        //  The set will include $OPENNMS_HOME and the values set via the Java command line
        Map<String, String> systemProperties = System.getProperties().entrySet().stream()
                .collect(Collectors.toMap(
                   // Explicitly convert the property names and values to string
                    e -> e.getKey().toString(),
                    e -> e.getValue().toString()
                ));

        // Load the properties from all of the available properties files
        // Order matters here since value may be overwritten by files
        // that appear later in the list
        for (File propertiesFile : getPropertiesFiles(opennmsHome)) {
            try (
                    InputStream is = new FileInputStream(propertiesFile);
            ) {
                if (DEBUG) { System.err.println("Loading system properties from: " + propertiesFile.getAbsolutePath()); }
                Properties props = new Properties();
                props.load(is);
                for (Entry<Object, Object> entry : props.entrySet()) {
                    String key = entry.getKey().toString();
                    String value = entry.getValue().toString();

                    if (systemProperties.containsKey(key)) {
                        if (DEBUG) {
                            System.err.printf("Skipping system property entry '%s' with value '%s' found in '%s'. "
                                    + " The property was already set on the JVM command line.\n",
                                    key, value, propertiesFile.getAbsolutePath());
                        }
                        // Skip properties that were already set outside of the .properties files
                        continue;
                    }

                    if (DEBUG) {
                        System.err.printf("Setting system property '%s' to '%s' found in '%s'.\n",
                                key, value, propertiesFile.getAbsolutePath());
                    }
                    System.setProperty(key, value);
                }
            } catch (FileNotFoundException e) {
                // Skip missing files
                if (DEBUG) { System.err.println("Skipping: " + propertiesFile.getAbsolutePath()); }
            }
        }
	}

    /**
     * Validates the OpenNMS home directory by checking
     * for known mandatory files.
     *
     * @param opennmsHome the OpenNMS home directory
     * @return true is the opennmsHome folder is a valid OpenNMS home directory
     */
    protected static boolean isValidOpenNMSHome(File opennmsHome) {
        File etc = new File(opennmsHome, "etc");
        File opennmsProperties = new File(etc, OPENNMS_PROPERTIES_NAME);
        return opennmsProperties.exists();
    }

    /**
     * Find the OpenNMS home directory.
     */
    protected static File findOpenNMSHome() throws Exception {
        // Try using the value of the OPENNMS_HOME system property, if set
        String opennmsHomeProperty = System.getProperty(OPENNMS_HOME_PROPERTY);
        if (opennmsHomeProperty != null) {
            File opennmsHome = new File(opennmsHomeProperty);
            if (isValidOpenNMSHome(opennmsHome)) {
                return opennmsHome;
            }
        }

        // Otherwise, attempt to infer the path from the location of this code-base
        File opennmsHome = findOpenNMSHomeUsingJarPath();
        if (opennmsHome == null) {
            System.err.println("Could not determine OpenNMS home "
                    + "directory.  Use \"-Dopennms.home=...\" "
                    + "option to Java to specify a specific "
                    + "OpenNMS home directory.  " + "E.g.: "
                    + "\"java -Dopennms.home=... -jar ...\".");
            System.exit(1);
        } else if (!isValidOpenNMSHome(opennmsHome)) {
            throw new RuntimeException("Unable to determine the location $OPENNMS_HOME.");
        }

        // Set the system property to reflect the path
        System.setProperty(OPENNMS_HOME_PROPERTY, opennmsHome.getAbsolutePath());
        return opennmsHome;
    }

    /**
     * Determine the OpenNMS home directory based on the location of the JAR
     * file containing this code. Finds the JAR file containing this code, and
     * if it is found, the file name of the JAR (e.g.: opennms_bootstrap.jar)
     * and its parent directory (e.g.: the lib directory) are removed from the
     * path and the resulting path (e.g.: /opt/OpenNMS) is returned.
     *
     * @return Home directory or null if it couldn't be found
     */
    public static File findOpenNMSHomeUsingJarPath() {
        ClassLoader l = Thread.currentThread().getContextClassLoader();

        try {
            String classFile = Bootstrap.class.getName().replace('.', '/') + ".class";
            URL url = l.getResource(classFile);
            if (url.getProtocol().equals("jar")) {
                URL subUrl = new URL(url.getFile());
                if (subUrl.getProtocol().equals("file")) {
                    String filePath = subUrl.getFile();
                    int i = filePath.lastIndexOf('!');
                    File file = new File(filePath.substring(0, i));
                    return file.getParentFile().getParentFile();
                }
            }
        } catch (MalformedURLException e) {
            return null;
        }

        return null;
    }

    /**
     * Bootloader main method. Takes the following steps to initialize a
     * ClassLoader, set properties, and start OpenNMS:
     * <ul>
     * <li>Checks for existence of opennms.home system property, and loads
     * properties file located at ${opennms.home}/etc/bootstrap.properties if
     * it exists.</li>
     * <li>Calls {@link #findOpenNMSHome findOpenNMSHome} to determine the
     * OpenNMS home directory if the bootstrap.properties file has not yet
     * been loaded. Sets the opennms.home system property to the path returned
     * from findOpenNMSHome.</li>
     * <li>Calls {@link #loadClasses(String, boolean, boolean) loadClasses} to create
     * a new ClassLoader. ${opennms.home}/etc and ${opennms.home}/lib are
     * passed to loadClasses.</li>
     * <li>Determines the proper default value for configuration options when
     * overriding system properties have not been set. Below are the default
     * values.
     * <ul>
     * <li>opennms.library.jicmp:
     * ClassLoader.getResource(System.mapLibraryName("jicmp"))</li>
     * <li>opennms.library.jrrd:
     * ClassLoader.getResource(System.mapLibraryName("jrrd"))</li>
     * <li>log4j.configuration: "log4j.properties"</li>
     * <li>jcifs.properties: ClassLoader.getResource("jcifs.properties")</li>
     * </ul>
     * </li>
     * <li>Finally, the main method of org.opennms.netmgt.vmmgr.Controller is
     * invoked with the parameters passed in argv.</li>
     * </ul>
     *
     * @param args
     *            Command line arguments
     * @throws java.lang.Exception if any.
     */
    public static void main(String[] args) throws Exception {
        final File opennmsHome = findOpenNMSHome();
        loadSystemProperties(opennmsHome);

        final String classToExec = System.getProperty("opennms.manager.class", "org.opennms.netmgt.vmmgr.Controller");
        final String classToExecMethod = System.getProperty("opennms.manager.method", "main");
        final String[] classToExecArgs = args;

        executeClass(classToExec, classToExecMethod, classToExecArgs, false);
    }

    protected static void executeClass(final String classToExec, final String classToExecMethod, final String[] classToExecArgs, boolean appendClasspath) throws ClassNotFoundException, NoSuchMethodException, IOException {
        executeClass(classToExec, classToExecMethod, classToExecArgs, appendClasspath, false);
    }

    protected static void executeClass(final String classToExec, final String classToExecMethod, final String[] classToExecArgs, boolean appendClasspath, final boolean recurse) throws ClassNotFoundException, NoSuchMethodException, IOException {
        String dir = System.getProperty("opennms.classpath");
        if (dir == null) {
            dir = System.getProperty(OPENNMS_HOME_PROPERTY) + File.separator
            		+ "classes" + File.pathSeparator
            		+ System.getProperty(OPENNMS_HOME_PROPERTY) + File.separator
                    + "lib" + File.pathSeparator
                    + System.getProperty(OPENNMS_HOME_PROPERTY)
                    + File.separator + "etc";
        }

        // Add the JDK tools.jar to the classpath so that we can use the Attach API
        dir += File.pathSeparator + System.getProperty("java.home") + File.separator + ".." + File.separator + "lib" + File.separator + "tools.jar";

        if (System.getProperty("org.opennms.protocols.icmp.interfaceJar") != null) {
        	dir += File.pathSeparator + System.getProperty("org.opennms.protocols.icmp.interfaceJar");
        }
        
        if (System.getProperty("org.opennms.rrd.interfaceJar") != null) {
        	dir += File.pathSeparator + System.getProperty("org.opennms.rrd.interfaceJar");
        }

        if (DEBUG) {
            System.err.println("dir = " + dir);
        }

        final ClassLoader cl = Bootstrap.loadClasses(dir, recurse, appendClasspath);

        configureRMI(cl);

        if (classToExec != null) {
            final String className = classToExec;
            final Class<?>[] classes = new Class[] { classToExecArgs.getClass() };
            final Object[] methodArgs = new Object[] { classToExecArgs };
            Class<?> c = cl.loadClass(className);
            final Method method = c.getMethod(classToExecMethod, classes);

            Runnable execer = new Runnable() {
                @Override
                public void run() {
                    try {
                        method.invoke(null, methodArgs);
                    } catch (final Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }

            };
            Thread bootstrapper = new Thread(execer, "Main");
            bootstrapper.setContextClassLoader(cl);
            bootstrapper.start();
        }
    }

    private static void configureRMI(final ClassLoader cl) throws IOException {
        if (m_rmiServerSocketFactory != null) {
            // socket already configured
            return;
        }

        final String host = System.getProperty("opennms.poller.server.serverHost", "localhost");
        if ("localhost".equals(host) || "127.0.0.1".equals(host) || "::1".equals(host)) {
            if (System.getProperty("java.rmi.server.hostname") == null) {
                System.setProperty("java.rmi.server.hostname", host);
            }
            m_rmiServerSocketFactory = new HostRMIServerSocketFactory("localhost");
            RMISocketFactory.setSocketFactory(m_rmiServerSocketFactory);
        }

        /**
          * This is necessary so the ProxyLoginModule can find the OpenNMSLoginModule because
          * otherwise we're at the mercy of which thread/context is the first to make a JAAS
          * request, since LoginModules are initialized statically.  In my testing, attempting
          * to connect to JMX with jconsole would give a class not found while attempting to
          * locate the OpenNMSLoginModule without using a classloader like this.
          */
        OpenNMSProxyLoginModule.setClassloader(cl);
    }
}
