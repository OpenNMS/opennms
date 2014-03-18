/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import junit.framework.AssertionFailedError;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File anticipator.
 *
 * Example usage with late initialization:
 * <pre>
 * private FileAnticipator m_fileAnticipator;
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class FileAnticipator extends Assert {
	
	private static final Logger LOG = LoggerFactory.getLogger(FileAnticipator.class);

    private LinkedList<File> m_expecting = new LinkedList<File>();
    private LinkedList<File> m_deleteMe = new LinkedList<File>();
    private File m_tempDir = null;
    private boolean m_initialized = false;
    
    /**
     * <p>Constructor for FileAnticipator.</p>
     *
     * @throws java.io.IOException if any.
     */
    public FileAnticipator() throws IOException {
        this(true);
    }
    
    /**
     * <p>Constructor for FileAnticipator.</p>
     *
     * @param initialize a boolean.
     * @throws java.io.IOException if any.
     */
    public FileAnticipator(boolean initialize) throws IOException {
        if (initialize) {
            initialize();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected void finalize() {
        tearDown();
    }

    /**
     * <p>tearDown</p>
     */
    public void tearDown() {
        if (!isInitialized()) {
            return;
        }
        
        try {
        	// Windows is really picky about filehandle reaping, triggering a GC
        	// keeps it from holding on to files when we're trying to delete them
            deleteExpected(true);
            
            for (ListIterator<File> i = m_deleteMe.listIterator(m_deleteMe.size()); i.hasPrevious(); ) {
                File f = i.previous();
                if (!f.exists()) continue;
                if (!FileUtils.deleteQuietly(f)) {
                    final StringBuffer b = new StringBuffer();
                    b.append("Could not delete " + f.getAbsolutePath() + ": is it a non-empty directory?");
                    b.append("\nDirectory listing:");
                    if (f.listFiles() != null) {
	                    for (File file : f.listFiles()) {
	                        b.append("\n\t");
	                        b.append(file.getName());
	                    }
                    }
                    fail(b.toString());
                }
            }
            if (m_tempDir != null) {
                assertFalse(m_tempDir + " exists", m_tempDir.exists());
            }
        } catch (Throwable t) {
            if (m_tempDir != null && m_tempDir.exists()) {
            	try {
            		FileUtils.forceDelete(m_tempDir);
            		return;
            	} catch (final IOException innerThrowable) {
                    LOG.warn("an error occurred while forcibly removing temporary directory {}", m_tempDir, innerThrowable);
                }
            } else {
            	LOG.warn("does not exist? {}", m_tempDir, t);
            }
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t);
            }
        }
    }
    
    /**
     * Create a temp directory where anticipated files will be stored for the
     * duration of the unit test.
     *
     * @throws java.io.IOException if any.
     */
    public void initialize() throws IOException {
        if (m_initialized) {
            return;
        }
        
        // Create a temp file
        m_tempDir = File.createTempFile("FileAnticipatorTemp","");
        // Delete the empty file that is created
        m_tempDir.delete();
        // Make a directory at the location
        m_tempDir.mkdir();
        // Mark it as deleteOnExit
        m_tempDir.deleteOnExit();
        
        //m_expecting.add(m_tempDir);
        
        m_initialized = true;
    }

    /**
     * <p>getTempDir</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getTempDir() {
        assertInitialized();
        
        return m_tempDir;
    }
    
    private void assertInitialized() {
        if (!isInitialized()) {
            throw new IllegalStateException("not initialized");
        }
    }

    /**
     * <p>tempFile</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     */
    public File tempFile(String name) throws IOException {
        if (name == null) {
            throw new IllegalArgumentException("name argument cannot be null");
        }

        assertInitialized();

        return internalTempFile(m_tempDir, name);
    }
    
    /**
     * <p>tempFile</p>
     *
     * @param parent a {@link java.io.File} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     */
    public File tempFile(File parent, String name) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("parent argument cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name argument cannot be null");
        }
        
        assertInitialized();
        
        return internalTempFile(parent, name);
    }
    
    /**
     * <p>tempFile</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param contents a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     */
    public File tempFile(String name, String contents) throws IOException {
        if (name == null) {
            throw new IllegalArgumentException("name argument cannot be null");
        }
        if (contents == null) {
            throw new IllegalArgumentException("contents argument cannot be null");
        }
        
        assertInitialized();
        
        return internalTempFile(m_tempDir, name, contents);
    }
    
    /**
     * <p>tempFile</p>
     *
     * @param parent a {@link java.io.File} object.
     * @param name a {@link java.lang.String} object.
     * @param contents a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     */
    public File tempFile(File parent, String name, String contents) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("parent argument cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name argument cannot be null");
        }
        if (contents == null) {
            throw new IllegalArgumentException("contents argument cannot be null");
        }
        
        assertInitialized();
        
        return internalTempFile(parent, name, contents);
    }

    /**
     * Non-asserting version of tempDir that can be used in initialize()
     * 
     * @param parent
     * @param name
     * @return object representing the newly created temporary directory
     * @throws IOException
     */
    private File internalTempDir(File parent, String name) throws IOException {
        File f = new File(parent, name);
        assertFalse("temporary directory exists but it shouldn't: " + f.getAbsolutePath(), f.exists());
        assertTrue("could not create temporary directory: " + f.getAbsolutePath(), f.mkdir());
        m_deleteMe.add(f);
        return f;
    }

    private File internalTempFile(File parent, String name) throws IOException {
        File f = new File(parent, name);
        assertFalse("temporary file exists but it shouldn't: " + f.getAbsolutePath(), f.exists());
        assertTrue("createNewFile: " + f.getAbsolutePath(), f.createNewFile());
        m_deleteMe.add(f);
        return f;
    }

    private File internalTempFile(File parent, String name, String contents) throws IOException {
        File f = internalTempFile(parent, name);
        FileOutputStream out = null;
        Writer writer = null;
        PrintWriter printWriter = null;
        try {
        	out = new FileOutputStream(f);
        	writer = new OutputStreamWriter(out, "UTF-8");
        	printWriter = new PrintWriter(writer);
        	printWriter.print(contents);
        } finally {
        	IOUtils.closeQuietly(printWriter);
        	IOUtils.closeQuietly(writer);
        	IOUtils.closeQuietly(out);
        }
        return f;
    }

    /**
     * <p>tempDir</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     */
    public File tempDir(String name) throws IOException {
        if (name == null) {
            throw new IllegalArgumentException("name argument cannot be null");
        }
        
        return tempDir(m_tempDir, name);
    }
    
    /**
     * <p>tempDir</p>
     *
     * @param parent a {@link java.io.File} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     */
    public File tempDir(File parent, String name) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("parent argument cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name argument cannot be null");
        }
        
        assertInitialized();
        
        return internalTempDir(parent, name);
    }
    
    /**
     * <p>expecting</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     */
    public File expecting(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name argument cannot be null");
        }
        assertInitialized();
        
        return internalExpecting(m_tempDir, name);
    }
    
    /**
     * <p>expecting</p>
     *
     * @param parent a {@link java.io.File} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     */
    public File expecting(File parent, String name) {
        if (parent == null) {
            throw new IllegalArgumentException("parent argument cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name argument cannot be null");
        }
        
        assertInitialized();

        return internalExpecting(parent, name);
    }
    
    private File internalExpecting(File parent, String name) {
        File f = new File(parent, name);
        m_expecting.add(f);
        return f;
    }
    
    /**
     * Delete expected files, throwing an AssertionFailedError if any of
     * the expected files don't exist.
     */
    public void deleteExpected() {
        deleteExpected(false);
    }
    
    /**
     * Delete expected files, throwing an AssertionFailedError if any of
     * the expected files don't exist.
     *
     * @param ignoreNonExistantFiles if true, non-existent files will be
     *      ignored and will not throw an AssertionFailedError
     * @throws AssertionFailedError if ignoreNonExistantFiles is false
     *      and an expected file does not exist, or if a file cannot be deleted
     */
    public void deleteExpected(boolean ignoreNonExistantFiles) {
        assertInitialized();

        Collections.sort(m_expecting, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return a.getAbsolutePath().compareTo(b.getAbsolutePath());
            }
        });
        
        final List<String> errors = new ArrayList<String>();

        for (ListIterator<File> i = m_expecting.listIterator(m_expecting.size()); i.hasPrevious(); ) {
            File f = i.previous();
            if (!f.exists()) {
                if (!ignoreNonExistantFiles) {
                    errors.add("Expected file that needs to be deleted does not exist: " + f.getAbsolutePath());
                }
            } else {
                if (f.isDirectory()) {
                    String[] files = f.list();
                    if (files.length > 0) {
                        StringBuffer fileList = new StringBuffer("{ ");
                        fileList.append(files[0]);
                        for (int j = 1; j < files.length; j++) {
                            fileList.append(", ").append(files[j]);
                        }
                        fileList.append(" }");
                        errors.add("Directory was not empty: " + f.getAbsolutePath() + ": " + fileList.toString());
                    } else {
                        if (!f.delete()) errors.add("Could not delete directory: " + f.getAbsolutePath());
                    }
                } else {
                    if (!f.delete()) errors.add("Could not delete expected file: " + f.getAbsolutePath());
                }
            }
            i.remove();
        }
        assertEquals("No expected files left over", m_expecting.size(), 0);
        if (errors.size() > 0) {
            StringBuffer errorString = new StringBuffer();
            for (String error : errors) {
                errorString.append(error).append("\n");
            }
            fail("Errors occurred inside FileAnticipator:\n" + errorString.toString().trim());
        }
    }

    /**
     * <p>isInitialized</p>
     *
     * @return a boolean.
     */
    public boolean isInitialized() {
        return m_initialized;
    }
    
}
