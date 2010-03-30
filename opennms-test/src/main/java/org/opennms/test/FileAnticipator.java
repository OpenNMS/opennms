/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.
 * All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included
 * code and modified code that was published under the GNU General Public
 * License. Copyrights for modified and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 05: Improve reliability and errors from tearDown().  Update use
 *              of tearDown() in docs. - dj@opennms.org
 *
 * Copyright (C) 2006 DJ Gregor.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.opennms.core.utils.ProcessExec;

/**
 * File anticipator.
 * 
 * Example usage with late initialization:
 * <pre>
 * private FileAnticipator m_fileAnticipator;
 *
 * @Override
 * protected void setUp() throws Exception {
 *     super.setUp();
 *       
 *     // Don't initialize by default since not all tests need it.
 *     m_fileAnticipator = new FileAnticipator(false);
 *
 *     ...
 * }
 *    
 * @Override
 * protected void runTest() throws Throwable {
 *     super.runTest();
 *
 *     if (m_fileAnticipator.isInitialized()) {
 *         m_fileAnticipator.deleteExpected();
 *     }
 * }
 *  
 * @Override
 * protected void tearDown() throws Exception {
 *     m_fileAnticipator.tearDown();
 *     
 *     super.tearDown();
 * }
 * </pre>
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class FileAnticipator extends Assert {
    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    
    private LinkedList<File> m_expecting = new LinkedList<File>();
    private LinkedList<File> m_deleteMe = new LinkedList<File>();
    private File m_tempDir = null;
    private boolean m_initialized = false;
    
    public FileAnticipator() throws IOException {
        this(true);
    }
    
    public FileAnticipator(boolean initialize) throws IOException {
        if (initialize) {
            initialize();
        }
    }
    
    @Override
    protected void finalize() {
        tearDown();
    }

    public void tearDown() {
        if (!isInitialized()) {
            return;
        }
        
        try {
            deleteExpected(true);
            
            for (ListIterator<File> i = m_deleteMe.listIterator(m_deleteMe.size()); i.hasPrevious(); ) {
                File f = i.previous();
                if (!f.delete()) {
                    StringBuffer b = new StringBuffer();
                    b.append("Could not delete " + f.getAbsolutePath() + ": is it a non-empty directory?");
                    b.append("\nDirectory listing:");
                    for (File file : f.listFiles()) {
                        b.append("\n\t");
                        b.append(file.getName());
                    }
                    fail(b.toString());
                }
            }
            if (m_tempDir != null) {
                assertFalse(m_tempDir + " exists", m_tempDir.exists());
            }
        } catch (Throwable t) {
            if (m_tempDir != null && m_tempDir.exists()) {
                ProcessExec ex = new ProcessExec(System.out, System.err);
                String[] cmd = new String[3];
                cmd[0] = "rm";
                cmd[1] = "-r";
                cmd[2] = m_tempDir.getAbsolutePath();
                
                try {
                    ex.exec(cmd);
                } catch (Throwable innerThrowable) {
                    StringBuffer command = new StringBuffer();
                    command.append(cmd[0]);
                    for (int i = 1; i < cmd.length; i++) {
                        command.append(" ");
                        command.append(cmd[i]);
                    }
                    System.err.println("Got throwable while forcibly removing temporary directory " + m_tempDir + " with '" + command + "': " + innerThrowable);
                    innerThrowable.printStackTrace();
                }
            }
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t);
            }
        }
    }
    
    public void initialize() throws IOException {
        if (m_initialized) {
            return;
        }
        
        String systemTempDir = System.getProperty(JAVA_IO_TMPDIR);
        assertNotNull(JAVA_IO_TMPDIR + " system property is not set, but must be", systemTempDir);
        
        File f = new File(systemTempDir); 
        assertTrue("path specified in system property " + JAVA_IO_TMPDIR + ", \"" +
                 systemTempDir + "\" is not a directory", f.isDirectory());

        String tempFileName = "FileAnticipator_temp_" + System.currentTimeMillis() + "_" + generateRandomHexString(8);
        m_tempDir = internalTempDir(f, tempFileName);
        
        m_initialized = true;
    }

    protected static String generateRandomHexString(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length argument is " + length + " and cannot be below zero");
        }
        
        Random random=new Random();
        /*
        SecureRandom sometimes gets tied up in knots in testing (the test process goes off into lala land and never returns from .nextBytes)
        Slow debugging (with pauses) seems to work most of the time, but manual Thread.sleeps doesn't
        Using Random instead of SecureRandom (which should be fine in this context) works much better.  Go figure
        
        SecureRandom random = null;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            fail("Could not initialize SecureRandom: " + e);
        }*/
        
        byte bytes[] = new byte[length];
        random.nextBytes(bytes);
        
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    public File getTempDir() {
        assertInitialized();
        
        return m_tempDir;
    }
    
    private void assertInitialized() {
        if (!isInitialized()) {
            throw new IllegalStateException("not initialized");
        }
    }

    public File tempFile(String name) throws IOException {
        if (name == null) {
            throw new IllegalArgumentException("name argument cannot be null");
        }

        assertInitialized();

        return internalTempFile(m_tempDir, name);
    }
    
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
        PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        w.print(contents);
        w.close();
        return f;
    }

    public File tempDir(String name) throws IOException {
        if (name == null) {
            throw new IllegalArgumentException("name argument cannot be null");
        }
        
        return tempDir(m_tempDir, name);
    }
    
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
    
    public File expecting(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name argument cannot be null");
        }
        assertInitialized();
        
        return internalExpecting(m_tempDir, name);
    }
    
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
     * @param ignoreNonExistantFiles if true, non-existant files will be
     *      ignored and will not throw an AssertionFailedError
     * @throws AssertionFailedError if ignoreNonExistantFiles is false
     *      and an expected file does not exist, or if a file cannot be deleted
     */
    public void deleteExpected(boolean ignoreNonExistantFiles) {
        assertInitialized();

        for (ListIterator<File> i = m_expecting.listIterator(m_expecting.size()); i.hasPrevious(); ) {
            File f = i.previous();
            if (!f.exists()) {
                if (!ignoreNonExistantFiles) {
                    fail("Expected file that needs to be deleted does not exist: " + f.getAbsolutePath());
                }
            } else {
                assertTrue("Could not delete expected file: " + f.getAbsolutePath(), f.delete());
            }
            i.remove();
        }
        assertEquals("No expected files left over", m_expecting.size(), 0);
    }

    public boolean isInitialized() {
        return m_initialized;
    }
    
}
