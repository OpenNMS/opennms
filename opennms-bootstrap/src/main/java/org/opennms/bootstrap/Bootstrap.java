/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.server.RMISocketFactory;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Bootstrap application for starting OpenNMS.
 */
public abstract class Bootstrap {
    protected static final String BOOT_PROPERTIES_NAME = "bootstrap.properties";
    protected static final String RRD_PROPERTIES_NAME = "rrd-configuration.properties";
    protected static final String LIBRARY_PROPERTIES_NAME = "libraries.properties";
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
     * Create a ClassLoader with the JARs found in dirStr.
     *
     * @param dirStr
     *            List of directories to search for JARs, separated by
     *            {@link java.io.File#pathSeparator File.pathSeparator}
     * @param recursive
     *            Whether to recurse into subdirectories of the directories in
     *            dirStr
     * @param append TODO
     * @returns A new ClassLoader containing the found JARs
     * @return a {@link java.lang.ClassLoader} object.
     * @throws java.net.MalformedURLException if any.
     */
    public static ClassLoader loadClasses(String dirStr, boolean recursive, boolean append) throws MalformedURLException {
        LinkedList<URL> urls = new LinkedList<URL>();

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

        final boolean debug = Boolean.getBoolean("opennms.bootstrap.debug");
        if (debug) {
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
        LinkedList<URL> urls = new LinkedList<URL>();
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
     * Determine the OpenNMS home directory based on the location of the JAR
     * file containing this code. Finds the JAR file containing this code, and
     * if it is found, the file name of the JAR (e.g.: opennms_bootstrap.jar)
     * and its parent directory (e.g.: the lib directory) are removed from the
     * path and the resulting path (e.g.: /opt/OpenNMS) is returned.
     *
     * @return Home directory or null if it couldn't be found
     */
    public static File findOpenNMSHome() {
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
     * Copy properties from a property input stream to the system properties.
     * Specific properties are copied from the given InputStream.
     * 
     * @param is
     *            InputStream of the properties file to load.
     */
    protected static void loadProperties(InputStream is) throws IOException {
        Properties p = new Properties();
        p.load(is);

        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            String propertyName = entry.getKey().toString();
            Object value = entry.getValue();
            if (value != null) {
                System.setProperty(propertyName, value.toString());
            }
        }
    }
    
    /**
     * Copy properties from a property file to the system properties.
     */
    protected static boolean loadProperties(File f) throws IOException {
    	if (!f.exists()) {
    		return false;
    	}
    	InputStream is = null;
    	try {
    		is = new FileInputStream(f);
    		loadProperties(is);
    		return true;
    	}
    	finally {
    		if (is != null) {
    			is.close();
    		}
    	}
    }
    
    /**
     * Load default properties from the specified OpenNMS home into the
     * system properties.
     * @param opennmsHome the OpenNMS home directory
     * @return whether the property file was able to be loaded into the System properties
     * @throws IOException
     */
    protected static boolean loadDefaultProperties(File opennmsHome) throws IOException {
		boolean propertiesLoaded = true;
		File etc = new File(opennmsHome, "etc");
		File bootstrapFile = new File(etc, BOOT_PROPERTIES_NAME);
		loadProperties(bootstrapFile);

		File rrdFile = new File(etc, RRD_PROPERTIES_NAME);
		loadProperties(rrdFile);
		
		File libraryFile = new File(etc, LIBRARY_PROPERTIES_NAME);
		if (!loadProperties(libraryFile)) {
			propertiesLoaded = false;
		}
		
		return propertiesLoaded;
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
        loadDefaultProperties();
        
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

        if (System.getProperty("org.opennms.protocols.icmp.interfaceJar") != null) {
        	dir += File.pathSeparator + System.getProperty("org.opennms.protocols.icmp.interfaceJar");
        }
        
        if (System.getProperty("org.opennms.rrd.interfaceJar") != null) {
        	dir += File.pathSeparator + System.getProperty("org.opennms.rrd.interfaceJar");
        }

        final boolean debug = Boolean.getBoolean("opennms.bootstrap.debug");
        
        if (debug) {
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
    }

    protected static void loadDefaultProperties() throws Exception {
        boolean propertiesLoaded = false;
        String opennmsHome = System.getProperty(OPENNMS_HOME_PROPERTY);
        if (opennmsHome != null) {
            propertiesLoaded = loadDefaultProperties(new File(opennmsHome));
        }
        
        if (!propertiesLoaded) {
            File parent = findOpenNMSHome();
            if (parent == null) {
                System.err.println("Could not determine OpenNMS home "
                        + "directory.  Use \"-Dopennms.home=...\" "
                        + "option to Java to specify a specific "
                        + "OpenNMS home directory.  " + "E.g.: "
                        + "\"java -Dopennms.home=... -jar ...\".");
                System.exit(1);
            }
            propertiesLoaded = loadDefaultProperties(parent);
            System.setProperty(OPENNMS_HOME_PROPERTY, parent.getPath());
        }
        
        if (!propertiesLoaded) {
            throw new RuntimeException("Unable to load default properties from $OPENNMS_HOME!");
        }
    }

}
