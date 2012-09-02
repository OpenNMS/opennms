/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.regex.Matcher;


import junit.framework.Assert;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

/**
 * <p>ConfigurationTestUtils class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class ConfigurationTestUtils extends Assert {
    private static final String POM_FILE = "pom.xml";
    // TODO: rename this constant
    private static final String DAEMON_DIRECTORY = "opennms-base-assembly";

    /**
     * <p>getUrlForResource</p>
     *
     * @param obj a {@link java.lang.Object} object.
     * @param resource a {@link java.lang.String} object.
     * @return a {@link java.net.URL} object.
     */
    public static URL getUrlForResource(Object obj, String resource) {
        URL url = getClass(obj).getResource(resource);
        assertNotNull("could not get resource '" + resource + "' as a URL", url);
        return url;
    }

    private static Class<? extends Object> getClass(Object obj) {
        return (obj != null) ? obj.getClass() : ConfigurationTestUtils.class;
    }
    
    /**
     * <p>getSpringResourceForResource</p>
     *
     * @param obj a {@link java.lang.Object} object.
     * @param resource a {@link java.lang.String} object.
     * @return a {@link org.springframework.core.io.Resource} object.
     */
    public static Resource getSpringResourceForResource(Object obj, String resource) {
        try {
            return new FileSystemResource(getFileForResource(obj, resource));
        } catch (Throwable t) {
            return new InputStreamResource(getInputStreamForResource(obj, resource));
        }
    }
    
    public static Resource getSpringResourceForResourceWithReplacements(final Object obj, final String resource, final String[] ... replacements) throws IOException {
        try {
        	String config = getConfigForResourceWithReplacements(obj, resource, replacements);
        	File tmp = File.createTempFile("testConfigFile", ".xml");
        	tmp.deleteOnExit();
        	FileWriter fw = new FileWriter(tmp);
        	fw.write(config);
        	fw.close();
            return new FileSystemResource(tmp);
        } catch (final Throwable t) {
            return new InputStreamResource(getInputStreamForResourceWithReplacements(obj, resource, replacements));
        }
    }
    
    /**
     * <p>getFileForResource</p>
     *
     * @param obj a {@link java.lang.Object} object.
     * @param resource a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     */
    public static File getFileForResource(Object obj, String resource) {
        URL url = getUrlForResource(obj, resource);
        
        String path = url.getFile();
        assertNotNull("could not get resource '" + resource + "' as a file", path);
        
        File file = new  File(path);
        assertTrue("could not get resource '" + resource + "' as a file--the file at path '" + path + "' does not exist", file.exists());
        
        return file;
    }

    /**
     * @deprecated Use getInputStreamForResource instead.
     *
     * @param obj a {@link java.lang.Object} object.
     * @param resource a {@link java.lang.String} object.
     * @return a {@link java.io.Reader} object.
     */
    public static Reader getReaderForResource(Object obj, String resource) {
        Reader retval = null;
        try {
            retval = new InputStreamReader(getInputStreamForResource(obj, resource), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            fail("Your JVM doesn't support UTF-8 encoding, which is pretty much impossible.");
        }
        return retval;
    }

    /**
     * <p>getInputStreamForResource</p>
     *
     * @param obj a {@link java.lang.Object} object.
     * @param resource a {@link java.lang.String} object.
     * @return a {@link java.io.InputStream} object.
     */
    public static InputStream getInputStreamForResource(Object obj, String resource) {
        assertFalse("obj should not be an instance of java.lang.Class; you usually want to use 'this'", obj instanceof Class<?>);
        InputStream is = getClass(obj).getResourceAsStream(resource);
        assertNotNull("could not get resource '" + resource + "' as an input stream", is);
        return is;
    }
    
    /**
     * <p>getReaderForResourceWithReplacements</p>
     *
     * @param obj a {@link java.lang.Object} object.
     * @param resource a {@link java.lang.String} object.
     * @param replacements an array of {@link java.lang.String} objects.
     * @return a {@link java.io.Reader} object.
     * @throws java.io.IOException if any.
     */
    public static Reader getReaderForResourceWithReplacements(Object obj,
            String resource, String[] ... replacements) throws IOException {
        String newConfig = getConfigForResourceWithReplacements(obj, resource,
                                                                replacements);
        return new StringReader(newConfig);
    }
    
    
    /**
     * <p>getInputStreamForResourceWithReplacements</p>
     *
     * @param obj a {@link java.lang.Object} object.
     * @param resource a {@link java.lang.String} object.
     * @param replacements an array of {@link java.lang.String} objects.
     * @return a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     */
    public static InputStream getInputStreamForResourceWithReplacements(Object obj,
            String resource, String[] ... replacements) throws IOException {
        String newConfig = getConfigForResourceWithReplacements(obj, resource,
                                                                replacements);
        return new ByteArrayInputStream(newConfig.getBytes());
    }
    
    
    /**
     * <p>getConfigForResourceWithReplacements</p>
     *
     * @param obj a {@link java.lang.Object} object.
     * @param resource a {@link java.lang.String} object.
     * @param replacements an array of {@link java.lang.String} objects.
     * @return a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public static String getConfigForResourceWithReplacements(Object obj,
            String resource, String[] ... replacements) throws IOException {

        Reader inputReader = getReaderForResource(obj, resource);
        BufferedReader bufferedReader = new BufferedReader(inputReader);
        
        StringBuffer buffer = new StringBuffer();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            buffer.append(line);
            buffer.append("\n");
        }
    
        String newConfig = buffer.toString();
        for (String[] replacement : replacements) {
            // The quoting around the replacement is necessary for file paths to work
            // correctly on Windows.
            // @see http://issues.opennms.org/browse/NMS-4853
            newConfig = newConfig.replaceAll(replacement[0], Matcher.quoteReplacement(replacement[1]));
        }
    
        return newConfig;
    }

    /**
     * Use getInputStreamForConfigFile instead.
     *
     * @param configFile a {@link java.lang.String} object.
     * @return a {@link java.io.Reader} object.
     * @throws java.io.FileNotFoundException if any.
     */
    public static Reader getReaderForConfigFile(String configFile) throws FileNotFoundException {
        Reader retval = null;
        try {
            retval = new InputStreamReader(getInputStreamForConfigFile(configFile), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            fail("Your JVM doesn't support UTF-8 encoding, which is pretty much impossible.");
        }
        return retval;
    }

    /**
     * <p>getInputStreamForConfigFile</p>
     *
     * @param configFile a {@link java.lang.String} object.
     * @return a {@link java.io.InputStream} object.
     * @throws java.io.FileNotFoundException if any.
     */
    public static InputStream getInputStreamForConfigFile(String configFile) throws FileNotFoundException {
        return new FileInputStream(getFileForConfigFile(configFile));
    }

    /**
     * <p>getFileForConfigFile</p>
     *
     * @param configFile a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     */
    public static File getFileForConfigFile(String configFile) {
        File file = new File(getDaemonEtcDirectory(), configFile);
        assertTrue("configuration file '" + configFile + "' does not exist at " + file.getAbsolutePath(), file.exists());
        return file;
    }

    /**
     * <p>getDaemonEtcDirectory</p>
     *
     * @return a {@link java.io.File} object.
     */
    public static File getDaemonEtcDirectory() {
        String etcPath = 
            "src"+File.separator+
            "main"+File.separator+
            "filtered"+File.separator+
            "etc";
        return new File(getDaemonProjectDirectory(), etcPath);
    }
    
    /**
     * <p>setRelativeHomeDirectory</p>
     *
     * @param relativeHomeDirectory a {@link java.lang.String} object.
     */
    public static void setRelativeHomeDirectory(String relativeHomeDirectory) {
        setAbsoluteHomeDirectory(new File(getCurrentDirectory().getAbsolutePath(), relativeHomeDirectory).getAbsolutePath());
    }

    /**
     * <p>setAbsoluteHomeDirectory</p>
     *
     * @param absoluteHomeDirectory a {@link java.lang.String} object.
     */
    public static void setAbsoluteHomeDirectory(final String absoluteHomeDirectory) {
        System.setProperty("opennms.home", absoluteHomeDirectory);
    }

    /**
     * <p>getTopProjectDirectory</p>
     *
     * @return a {@link java.io.File} object.
     */
    public static File getTopProjectDirectory() {
        File currentDirectory = getCurrentDirectory();

        File pomFile = new File(currentDirectory, POM_FILE);
        assertTrue("pom.xml in current directory should exist: " + pomFile.getAbsolutePath(), pomFile.exists());
        
        return findTopProjectDirectory(currentDirectory);
    }

    private static File getCurrentDirectory() {
        File currentDirectory = new File(System.getProperty("user.dir"));
        assertTrue("current directory should exist: " + currentDirectory.getAbsolutePath(), currentDirectory.exists());
        assertTrue("current directory should be a directory: " + currentDirectory.getAbsolutePath(), currentDirectory.isDirectory());
        return currentDirectory;
    }

    /**
     * <p>getDaemonProjectDirectory</p>
     *
     * @return a {@link java.io.File} object.
     */
    public static File getDaemonProjectDirectory() {
        File topLevelDirectory = getTopProjectDirectory();
        File daemonDirectory = new File(topLevelDirectory, DAEMON_DIRECTORY);
        if (!daemonDirectory.exists()) {
            throw new IllegalStateException("Could not find a " + DAEMON_DIRECTORY + " in the location top-level directory: " + topLevelDirectory);
        }
        
        File pomFile = new File(daemonDirectory, POM_FILE);
        assertTrue("pom.xml in " + DAEMON_DIRECTORY + " directory should exist: " + pomFile.getAbsolutePath(), pomFile.exists());
        
        return daemonDirectory;
    }

    private static File findTopProjectDirectory(File currentDirectory) {
        File buildFile = new File(currentDirectory, "compile.pl");
        if (buildFile.exists()) {
            File pomFile = new File(currentDirectory, POM_FILE);
            assertTrue("pom.xml in " + DAEMON_DIRECTORY + " directory should exist: " + pomFile.getAbsolutePath(), pomFile.exists());
            
            return currentDirectory;
        } else {
            File parentDirectory = currentDirectory.getParentFile();
            
            if (parentDirectory == null || parentDirectory == currentDirectory) {
                return null;
            } else {
                return findTopProjectDirectory(parentDirectory);
            }
        }
    }

    /**
     * <p>setRrdBinary</p>
     *
     * @param path a {@link java.lang.String} object.
     */
    public static void setRrdBinary(String path) {
        System.setProperty("rrd.binary", path);
    }

    /**
     * <p>setRelativeRrdBaseDirectory</p>
     *
     * @param relativePath a {@link java.lang.String} object.
     */
    public static void setRelativeRrdBaseDirectory(String relativePath) {
        File rrdDir = new File(getCurrentDirectory(), relativePath);
        if (!rrdDir.exists()) {
            rrdDir.mkdirs();
        }
        System.setProperty("rrd.base.dir", rrdDir.getAbsolutePath());
    }

    /**
     * <p>setRelativeImporterDirectory</p>
     *
     * @param relativeImporterDirectory a {@link java.lang.String} object.
     */
    public static void setRelativeImporterDirectory(String relativeImporterDirectory) {
        File cacheDir = new File(getCurrentDirectory(), relativeImporterDirectory);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        System.setProperty("importer.requisition.dir", cacheDir.getAbsolutePath());
    }

    /**
     * <p>setRelativeForeignSourceDirectory</p>
     *
     * @param relativeForeignSourceDirectory a {@link java.lang.String} object.
     */
    public static void setRelativeForeignSourceDirectory(String relativeForeignSourceDirectory) {
            File xmlDir = new File(getCurrentDirectory(), relativeForeignSourceDirectory);
            if (!xmlDir.exists()) {
                xmlDir.mkdirs();
            }
            System.setProperty("importer.foreign-source.dir", xmlDir.getAbsolutePath());
    }

}
