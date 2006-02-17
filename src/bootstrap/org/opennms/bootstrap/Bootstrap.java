package org.opennms.bootstrap;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Bootstrap application for starting OpenNMS.
 */

public class Bootstrap {
    /**
     * Matches any file that is a directory.
     */
    private static FileFilter m_dirFilter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };

    /**
     * Matches any file that has a name ending in ".jar".
     */
    private static FilenameFilter m_jarFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        };

    /**
     * Create a ClassLoader with the JARs found in dirStr.
     *
     * @param dirStr List of directories to search for JARs, separated by
     *               {@link java.io.File#pathSeparator File.pathSeparator}
     * @param recursive Whether to recurse into subdirectories of the
     *                  directories in dirStr
     * @returns A new ClassLoader containing the found JARs
     */
    public static ClassLoader loadClasses(String dirStr, boolean recursive)
		throws MalformedURLException {
	LinkedList urls = new LinkedList();

	StringTokenizer toke = new StringTokenizer(dirStr, File.pathSeparator);
	while (toke.hasMoreTokens()) {
	    String token = (String) toke.nextToken();
	    loadClasses(new File(token), recursive, urls);
	}

	return newClassLoader(urls);
    }

    /**
     * Create a ClassLoader with the JARs found in dir.
     *
     * @param dir Directory to search for JARs
     * @param recursive Whether to recurse into subdirectories of dir
     * @returns A new ClassLoader containing the found JARs
     */
    public static ClassLoader loadClasses(File dir, boolean recursive)
		throws MalformedURLException {
	LinkedList urls = new LinkedList();
	loadClasses(dir, recursive, urls);
	return newClassLoader(urls);
    }

    /**
     * Create a ClassLoader with the list of URLs found in urls.
     *
     * @param urls List of URLs to add to the ClassLoader's search list.
     * @returns A new ClassLoader with the specified search list
     */
    public static ClassLoader newClassLoader(LinkedList urls) {
	URL[] urlsArray = (URL[]) urls.toArray(new URL[0]);

	return URLClassLoader.newInstance(urlsArray);
    }


    /**
     * Add JARs found in dir to the LinkedList urls.
     *
     * @param dir Directory to search for JARs
     * @param recursive Whether to recurse into subdirectories of the
     *                  directory in dir
     * @param urls LinkedList to append found JARs onto
     */
    public static void loadClasses(File dir, boolean recursive, LinkedList urls)
		throws MalformedURLException {
	// Add the directory
	urls.add(dir.toURL());

	if (recursive) {
	    // Descend into sub-directories
	    File[] dirlist = dir.listFiles(m_dirFilter);
	    if (dirlist != null) {
		for (int i = 0; i < dirlist.length; i++) {
		    loadClasses(dirlist[i], recursive, urls);
		}
	    }
	}

	// Add individual JAR files
        File[] children = dir.listFiles(m_jarFilter);
	if (children != null) {
	    for (int i = 0; i < children.length; i++) {
		urls.add(children[i].toURL());
	    }
	}
    }

    /**
     * Determine the OpenNMS home directory based on the location of the
     * JAR file containing this code.
     *
     * Finds the JAR file containing this code, and if it is found,
     * the file name of the JAR (e.g.: opennms_bootstrap.jar) and its
     * parent directory (e.g.: the lib directory) are removed from the
     * path and the resulting path (e.g.: /opt/OpenNMS) is returned.
     *
     * @return Home directory or null if it couldn't be found
     */
    public static File findOpenNMSHome() {
	ClassLoader l = Thread.currentThread().getContextClassLoader();

	try {
	    String classFile = Bootstrap.class.getName().replace('.', '/') +
		".class";
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
     * Copy properties from a properties file to the system properties.
     *
     * Specific properties are copied from the given InputStream.
     *
     * @param is InputStream of the properties file to load.
     */
    static void loadProperties(InputStream is) throws IOException {
	Properties p = new Properties();
	p.load(is);

	String[] propertyNames = new String[] {
	    "opennms.classpath",
	    "opennms.library.jicmp",
	    "opennms.library.jrrd"
	};

	for (int i = 0; i < propertyNames.length; i++) {
	    String value = p.getProperty(propertyNames[i]);
	    if (value != null) {
		System.setProperty(propertyNames[i], value);
	    }
	}
    }

    /**
     * Bootloader main method.
     *
     * Takes the following steps to initialize a ClassLoader, set
     * properties, and start OpenNMS:
     * <ul>
     *   <li>Checks for existence of opennms.home system property, and
     *     loads properties file located at
     *     ${opennms.home}/etc/bootstrap.properties if it exists.</li>
     *   <li>Calls {@link #findOpenNMSHome findOpenNMSHome} to determine 
     *     the OpenNMS home directory if the bootstrap.properties file
     *     has not yet been loaded.  Sets the opennms.home system property
     *     to the path returned from findOpenNMSHome.</li>
     *   <li>Calls {@link #loadClasses(String, boolean) loadClasses} to
     *     create a new ClassLoader.  ${opennms.home}/etc and
     *     ${opennms.home/lib} are passed to loadClasses.</li>
     *   <li>Determines the proper default value for configuration options
     *     when overriding system properties have not been set.  Below are
     *     the default values.
     *     <ul>
     *       <li>opennms.library.jicmp:
     *         ClassLoader.getResource(System.mapLibraryName("jicmp"))</li>
     *       <li>opennms.library.jrrd:
     *         ClassLoader.getResource(System.mapLibraryName("jrrd"))</li>
     *       <li>log4j.configuration: "log4j.properties"</li>
     *       <li>jcifs.properties:
     *         ClassLoader.getResource("jcifs.properties")</li>
     *     </ul>
     *   </li>
     *   <li>Finally, the main method of org.opennms.netmgt.vmmgr.Manager
     *     is invoked with the parameters passed in argv.</li>
     * </ul>
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) throws Exception {
	final String bootPropertiesName = "bootstrap.properties";
	final String opennmsHomeProperty = "opennms.home";

	final String classToExec = "org.opennms.netmgt.vmmgr.Manager";
        final String classToExecMethod = "main";
        final String[] classToExecArgs = args;

	boolean propertiesLoaded = false;
	String opennmsHome = System.getProperty(opennmsHomeProperty);
	if (opennmsHome != null) {
	    File f = new File(opennmsHome + File.separator + "etc" +
			      File.separator + bootPropertiesName);
	    if (f.exists()) {
		loadProperties(new FileInputStream(f));
		propertiesLoaded = true;
	    }
	}

	/*
         * This would search for the bootstrap.properties file in the JAR
	 * containing this code.  We no longer need this file in the JAR,
	 * though, since we can determine everything we need at runtime.
         */
        /*
	if (!propertiesLoaded) {
            ClassLoader l =  Thread.currentThread().getContextClassLoader();
            is = l.getResourceAsStream(bootPropertiesName);

	    if (is == null) {
		loadProperties(is);
		propertiesLoaded = true;
	    }
	}
	*/

	if (!propertiesLoaded) {
	    File parent = findOpenNMSHome();
	    if (parent == null) {
		System.err.println("Could not determine OpenNMS home "
				   + "directory.  Use \"-Dopennms.home=...\" "
				   + "option to Java to specify a specific "
				   + "OpenNMS home directory.  "
				   + "E.g.: "
				   + "\"java -Dopennms.home=... -jar ...\".");
		System.exit(1);
	    }
	    File f = new File(parent.getPath() +
			      File.separator + "etc" +
			      File.separator + bootPropertiesName);
	    if (f.exists()) {
		loadProperties(new FileInputStream(f));
		propertiesLoaded = true;
	    }

	    System.setProperty(opennmsHomeProperty,
			       parent.getPath());
	}

	String dir = System.getProperty("opennms.classpath");
	if (dir == null) {
	    dir = System.getProperty(opennmsHomeProperty) +
		File.separator + "lib" +
		File.pathSeparator +
		System.getProperty(opennmsHomeProperty) +
		File.separator + "etc";
	}
	final ClassLoader cl = Bootstrap.loadClasses(dir, false);
    
	URL url;

	if (System.getProperty("opennms.library.jicmp") == null) {
	    url = cl.getResource(System.mapLibraryName("jicmp"));
	    if (url != null) {
		System.setProperty("opennms.library.jicmp", url.getPath());
	    }
	}

	if (System.getProperty("opennms.library.jrrd") == null) {
	    url = cl.getResource(System.mapLibraryName("jrrd"));
	    if (url != null) {
		System.setProperty("opennms.library.jrrd", url.getPath());
	    }
	}

	if (System.getProperty("log4j.configuration") == null) {
	    System.setProperty("log4j.configuration", "log4j.properties");
	}

	if (System.getProperty("jcifs.properties") == null) {
	    url = cl.getResource("jcifs.properties");
	    if (url != null) {
		System.setProperty("jcifs.properties", url.getPath());
	    }
	}

	if (classToExec != null) {
	    final String className = classToExec;
	    final Class[] classes = new Class[] { classToExecArgs.getClass() };
	    final Object[] methodArgs = new Object[] { classToExecArgs };
            Class c = cl.loadClass(className);
            final Method method = c.getMethod(classToExecMethod, classes);

	    Runnable execer = new Runnable() {
	        public void run() {
	            try {
                        method.invoke(null, methodArgs);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
			System.exit(1);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
			System.exit(1);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
			System.exit(1);
                    }
	        }   
	        
	    };
            Thread bootstrapper = new Thread(execer, "BootStrapper");
            bootstrapper.setContextClassLoader(cl);
            bootstrapper.start();
	}
    }

}
