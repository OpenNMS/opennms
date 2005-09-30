package org.opennms.bootstrap;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.IOException;

import java.lang.reflect.Method;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;

public class Bootstrap {

    public static ClassLoader loadClasses(String dirStr)
		throws MalformedURLException {
	LinkedList urls = new LinkedList();

	StringTokenizer toke = new StringTokenizer(dirStr, File.pathSeparator);
	while (toke.hasMoreTokens()) {
	    String token = (String) toke.nextToken();
	    loadClasses(new File(token), urls);
	}

	return newClassLoader(urls);
    }

    public static ClassLoader loadClasses(File dir)
		throws MalformedURLException {
	LinkedList urls = new LinkedList();
	loadClasses(dir, urls);
	return newClassLoader(urls);
    }

    public static ClassLoader newClassLoader(LinkedList urls) {
	URL[] urlsArray = (URL[]) urls.toArray(new URL[0]);

        URLClassLoader UrlCL = URLClassLoader.newInstance(urlsArray);

	return UrlCL;
    }

    public static void loadClasses(File dir, LinkedList urls)
		throws MalformedURLException {
        FileFilter dirFilter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };

	//System.out.println("adding directory: " + dir.toURL());
	urls.add(dir.toURL());

        File[] dirlist = dir.listFiles(dirFilter);
	if (dirlist == null) {
	    //	    System.out.println("File.listFiles returned null: " + dir);
	    return;
	}
        for (int i = 0; i < dirlist.length; i++) {
            //ClassPather.loadClasses(dirlist[i], urls);
        }

        // It is also possible to filter the list of returned files.
        // This example does not return any files that ends with `.jar'.
        FilenameFilter jarFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        };

        File[] children = dir.listFiles(jarFilter);
        for (int i = 0; i < children.length; i++) {
	    //System.out.println("adding jar: " + children[i].toURL());
	    urls.add(children[i].toURL());
	}
    }

    public static File findOpenNMSHome() {
	ClassLoader l = Thread.currentThread().getContextClassLoader();

	try {
	    String classFile = Bootstrap.class.getName().replace('.', '/') +
		".class";
	    //System.out.println("classFile: " + classFile);
	    URL url = l.getResource(classFile);
	    //System.out.println("url: " + url);
	    if (url.getProtocol().equals("jar")) {
		URL subUrl = new URL(url.getFile());
		//System.out.println("subUrl: " + subUrl);
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

    public static void main(String[] args) throws Exception {
	final String bootPropertiesName = "bootstrap.properties";
	final String opennmsHomeProperty = "opennms.home";

	String classToExec = null;
	String classToExecMethod = null;
	String[] classToExecArgs = null;

	classToExec = "org.opennms.netmgt.vmmgr.Manager";
	classToExecMethod = "main";
	classToExecArgs = args;

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

	if (!propertiesLoaded) {
	    System.err.println("Warning: Could not find boot properties file.");
	}

	String dir = System.getProperty("opennms.classpath");
	if (dir == null) {
	    dir = System.getProperty(opennmsHomeProperty) +
		File.separator + "lib" +
		File.pathSeparator +
		System.getProperty(opennmsHomeProperty) +
		File.separator + "etc";
	}
	ClassLoader cl = Bootstrap.loadClasses(dir);
    
	String path;

	if (System.getProperty("opennms.library.jicmp") == null) {
	    path = cl.getResource(System.mapLibraryName("jicmp")).getPath();
	    if (path != null) {
		System.setProperty("opennms.library.jicmp", path);
	    }
	}

	if (System.getProperty("opennms.library.jrrd") == null) {
	    path = cl.getResource(System.mapLibraryName("jrrd")).getPath();
	    if (path != null) {
		System.setProperty("opennms.library.jrrd", path);
	    }
	}

	if (classToExec != null) {
	    String className = classToExec;
	    Class[] classes = new Class[] { classToExecArgs.getClass() };
	    Object[] methodArgs = new Object[] { classToExecArgs };

	    Class c = cl.loadClass(className);
	    Method method = c.getMethod(classToExecMethod, classes);
	    method.invoke(null, methodArgs);
	}
    }

}
