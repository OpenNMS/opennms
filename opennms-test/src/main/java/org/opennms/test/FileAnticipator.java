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
import java.util.LinkedList;
import java.util.ListIterator;

import org.opennms.core.utils.ProcessExec;

import junit.framework.Assert;

public class FileAnticipator extends Assert {
    private LinkedList m_expecting = new LinkedList();
    private LinkedList m_deleteMe = new LinkedList();
    private File m_tempDir = null;
    
    public FileAnticipator() throws IOException {
        createTempDir();
    }
    
    public void tearDown() {
        try {
            for (ListIterator i = m_deleteMe.listIterator(m_deleteMe.size());
                 i.hasPrevious(); ) {
                File f = (File) i.previous();
                if (!f.delete()) {
                    fail("Could not delete " + f.getAbsolutePath()
                         + ": is it a non-empty directory?");
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
    
    private void createTempDir() throws IOException {
        String systemTempDir = System.getProperty("java.io.tmpdir");
        File f = new File(systemTempDir); 
        if (!f.isDirectory()) {
            fail("path specified in system property " +
                 "java.io.tmpdir, \"" +
                 systemTempDir + "\" is not a directory");
        }

        m_tempDir = tempDir(f, "FileAnticipator_temp_"
                            + System.currentTimeMillis());
    }
    
    public File getTempDir() {
        return m_tempDir;
    }
    
    public File tempFile(String name) throws IOException {
        return tempFile(m_tempDir, name);
    }
    
    public File tempFile(String name, String contents) throws IOException {
        return tempFile(m_tempDir, name, contents);
    }
    
    public File tempFile(File parent, String name) throws IOException {
        String path;
        if (parent != null) {
            path = parent.getAbsolutePath() + File.separator + name;
        } else {
            path = name;
        }
        
        File f = new File(path);
        assertTrue("createNewFile: " + f.getAbsolutePath(), f.createNewFile());
        m_deleteMe.add(f);
        return f;
    }
    
    public File tempFile(File parent, String name, String contents)
        throws IOException {
        File f = tempFile(parent, name);
        PrintWriter w = new PrintWriter(new FileWriter(f));
        w.print(contents);
        w.close();
        return f;
    }

    public File tempDir(String name) throws IOException {
        return tempDir(m_tempDir, name);
    }
    
    public File tempDir(File parent, String name) throws IOException {
        String path;
        if (parent != null) {
            path = parent.getAbsolutePath() + File.separator + name;
        } else {
            path = name;
        }
        
        File f = new File(path);
        assertTrue("mkdir: " + f.getAbsolutePath(), f.mkdir());
        m_deleteMe.add(f);
        return f;
    }
    
    public File expecting(File parent, String name) {
        String path;
        if (parent != null) {
            path = parent.getAbsolutePath() + File.separator + name;
        } else {
            path = name;
        }
        
        File f = new File(path);
        m_expecting.add(f);
        return f;
    }
    
    public void deleteExpected() {
        for (ListIterator i = m_expecting.listIterator(m_expecting.size());
             i.hasPrevious(); ) {
            File f = (File) i.previous();
            assertTrue("\"" + f.getAbsolutePath() + "\" deleted", f.delete());
            i.remove();
        }
        assertEquals("No expected files left over", m_expecting.size(), 0);
    }
    
}
