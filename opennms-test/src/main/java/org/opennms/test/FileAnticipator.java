//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.ListIterator;

import junit.framework.Assert;

import org.opennms.core.utils.ProcessExec;

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
            for (ListIterator<File> i = m_deleteMe.listIterator(m_deleteMe.size()); i.hasPrevious(); ) {
                File f = i.previous();
                if (!f.delete()) {
                    StringBuffer b = new StringBuffer();
                    b.append("Could not delete " + f.getAbsolutePath() + ": is it a non-empty directory?  Output from 'ls -l':\n");
                    fail(b.toString());
                }
            }
            if (m_tempDir != null) {
                assertFalse(m_tempDir + " exists", m_tempDir.exists());
            }
        } catch (UndeclaredThrowableException e) {
            if (m_tempDir != null && m_tempDir.exists()) {
                ProcessExec ex = new ProcessExec(System.out, System.err);
                String[] cmd = new String[3];
                cmd[0] = "rm";
                cmd[1] = "-r";
                cmd[2] = m_tempDir.getAbsolutePath();
                try {
                    ex.exec(cmd);
                } catch (Throwable t) {
                    // ignore
                }
            }
            throw e;
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
        
        SecureRandom random = null;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            fail("Could not initialize SecureRandom: " + e);
        }
        
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
        PrintWriter w = new PrintWriter(new FileWriter(f));
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
    
    public void deleteExpected() {
        assertInitialized();

        for (ListIterator<File> i = m_expecting.listIterator(m_expecting.size()); i.hasPrevious(); ) {
            File f = i.previous();
            assertTrue("\"" + f.getAbsolutePath() + "\" deleted", f.delete());
            i.remove();
        }
        assertEquals("No expected files left over", m_expecting.size(), 0);
    }

    public boolean isInitialized() {
        return m_initialized;
    }
    
}
