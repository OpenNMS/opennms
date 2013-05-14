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
import java.io.FileInputStream;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * File anticipator Junit test.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class FileAnticipatorTest extends TestCase {
    private FileAnticipator m_anticipator;
    
    @Override
    public void setUp() throws Exception {
        m_anticipator = new FileAnticipator();
    }
    
    @Override
    public void tearDown() {
        m_anticipator.tearDown();
    }
    
    public void testConstructor() {
        // Empty... this effectively tests that setUp() works.
        assertTrue("anticipator should be initialized, but said it wasn't", m_anticipator.isInitialized());
    }
    
    public void testConstructorNoInitialize() throws Exception {
        FileAnticipator fa = new FileAnticipator(false);
        assertFalse("anticipator should not be initialized, but said it was", fa.isInitialized());
    }
    
    public void testExpecting() {
        String file = "/FileAnticipatorTest_bogus_" + System.currentTimeMillis();
        m_anticipator.expecting(file);
    }
    
    public void testExpectingWithParent() throws Exception {
        File parent = m_anticipator.tempDir("parent");
        String file = "/FileAnticipatorTest_bogus_" + System.currentTimeMillis();
        m_anticipator.expecting(parent, file);
    }
    
    public void testDeleteExpected() {
        m_anticipator.deleteExpected();
    }
    
    public void testExpectingDeleteExpected() throws Exception {
        String file = "FileAnticipatorTest_" + System.currentTimeMillis();
        File tempFile = m_anticipator.expecting(file);
        assertTrue("createNewFile: " + tempFile.getAbsolutePath(),
                   tempFile.createNewFile());
        m_anticipator.deleteExpected();
    }
    
    public void testExpectingDeleteExpectedBogus() {
        String file = "FileAnticipatorTest_bogus_" + System.currentTimeMillis();

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError("Errors occurred inside FileAnticipator:\nExpected file that needs to be deleted does not exist: " + m_anticipator.getTempDir() + File.separator + file));

        m_anticipator.expecting(file);

        try {
            m_anticipator.deleteExpected();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();

    }
    
    public void testTempDir() throws Exception {
        String file = "FileAnticipatorTest_tempDir_" + System.currentTimeMillis();
        File f = m_anticipator.tempDir(file);
        assertEquals("temporary directory name", m_anticipator.getTempDir() + File.separator + file, f.getAbsolutePath());
        assertTrue("temporary directory should exist at " + f.getAbsolutePath(), f.isDirectory());
        m_anticipator.deleteExpected();
    }
    
    public void testTempDirNullName() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("name argument cannot be null"));
        
        try {
            m_anticipator.tempDir(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testTempDirWithParent() throws Exception {
        File parent = m_anticipator.tempDir("parent"); 
        
        String file = "FileAnticipatorTest_tempDir_" + System.currentTimeMillis();
        File f = m_anticipator.tempDir(parent, file);
        assertEquals("temporary directory name", m_anticipator.getTempDir() + File.separator + "parent" + File.separator + file, f.getAbsolutePath());
        assertTrue("temporary directory should exist at " + f.getAbsolutePath(), f.isDirectory());
        m_anticipator.deleteExpected();
    }
    
    public void testTempFile() throws Exception {
        String file = "FileAnticipatorTest_tempFile_" + System.currentTimeMillis();
        File f = m_anticipator.tempFile(file);
        assertEquals("temporary file name", m_anticipator.getTempDir() + File.separator + file, f.getAbsolutePath());
        assertTrue("temporary file should exist at " + f.getAbsolutePath(), f.isFile());
        m_anticipator.deleteExpected();
    }
    
    public void testTempFileNullName() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("name argument cannot be null"));
        
        try {
            m_anticipator.tempFile(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testTempFileWithParent() throws Exception {
        File parent = m_anticipator.tempDir("parent"); 
        
        String file = "FileAnticipatorTest_tempFile_" + System.currentTimeMillis();
        File f = m_anticipator.tempFile(parent, file);
        assertEquals("temporary file name", m_anticipator.getTempDir() + File.separator + "parent" + File.separator + file, f.getAbsolutePath());
        assertTrue("temporary file should exist at " + f.getAbsolutePath(), f.isFile());
        m_anticipator.deleteExpected();
    }
    
    
    public void testTempFileWithParentNullParent() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("parent argument cannot be null"));
        
        try {
            m_anticipator.tempFile((File) null, "child");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testTempFileWithParentNullName() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("name argument cannot be null"));
        
        try {
            m_anticipator.tempFile(new File("parent"), null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testTempFileWithContents() throws Exception {
        String file = "FileAnticipatorTest_tempFile_" + System.currentTimeMillis();
        String contents = "yay!";
        File f = m_anticipator.tempFile(file, contents);
        assertEquals("temporary file name", m_anticipator.getTempDir() + File.separator + file, f.getAbsolutePath());
        assertTrue("temporary file should exist at " + f.getAbsolutePath(), f.isFile());
        
        StringBuffer b = new StringBuffer();
        FileInputStream is = new FileInputStream(f);
        int i;
        while ((i = is.read()) != -1) {
            b.append(new Character((char) i));
        }
        is.close();
        
        assertEquals("file contents", contents, b.toString());
        
        m_anticipator.deleteExpected();
    }
    
    public void testTempFileWithContentsNullName() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("name argument cannot be null"));
        
        try {
            m_anticipator.tempFile((String) null, "name");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testTempFileWithContentsNullContents() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("contents argument cannot be null"));
        
        try {
            m_anticipator.tempFile("name", null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testTempFileWithContentsAndParent() throws Exception {
        File parent = m_anticipator.tempDir("parent"); 
        
        String file = "FileAnticipatorTest_tempFile_" + System.currentTimeMillis();
        String contents = "yay!";
        File f = m_anticipator.tempFile(parent, file, contents);
        assertEquals("temporary file name", m_anticipator.getTempDir() + File.separator + "parent" + File.separator + file, f.getAbsolutePath());
        assertTrue("temporary file should exist at " + f.getAbsolutePath(), f.isFile());
        
        StringBuffer b = new StringBuffer();
        FileInputStream is = new FileInputStream(f);
        int i;
        while ((i = is.read()) != -1) {
            b.append(new Character((char) i));
        }
        is.close();
        
        assertEquals("file contents", contents, b.toString());
        
        m_anticipator.deleteExpected();
    }
    

    public void testTempFileWithContentsAndParentNullParent() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("parent argument cannot be null"));
        
        try {
            m_anticipator.tempFile((File) null, "name", "contents");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public void testTempFileWithContentsAndParentNullName() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("name argument cannot be null"));
        
        try {
            m_anticipator.tempFile(new File("parent"), (String) null, "contents");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testTempFileWithContentsAndParentNullContents() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("contents argument cannot be null"));
        
        try {
            m_anticipator.tempFile(new File("parent"), "name", null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testNotInitializedThenInitialize() throws Exception {
        FileAnticipator fa = new FileAnticipator(false);
        fa.initialize();
        assertTrue("anticipator should be initialized, but said it wasn't", m_anticipator.isInitialized());
    }
    
    public void testNotInitializedThenGetTempDir() throws Exception {
        FileAnticipator fa = new FileAnticipator(false);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("not initialized"));
        try {
            fa.getTempDir();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testNotInitializedThenTempFile() throws Exception {
        FileAnticipator fa = new FileAnticipator(false);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("not initialized"));
        try {
            fa.tempFile(new File("parent"), "file_child");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    public void testNotInitializedThenTempFileWithContents() throws Exception {
        FileAnticipator fa = new FileAnticipator(false);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("not initialized"));
        try {
            fa.tempFile(new File("parent"), "file_child", "child contents");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testNotInitializedThenTempDir() throws Exception {
        FileAnticipator fa = new FileAnticipator(false);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("not initialized"));
        try {
            fa.tempDir(new File("parent"), "dir_child");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testNotInitializedThenExpecting() throws Exception {
        FileAnticipator fa = new FileAnticipator(false);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("not initialized"));
        try {
            fa.expecting(new File("parent"), "expecting_child");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testNotInitializedThenDeletedExpected() throws Exception {
        FileAnticipator fa = new FileAnticipator(false);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("not initialized"));
        try {
            fa.deleteExpected();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    

    public void testNotInitializedThenTearDown() throws Exception {
        FileAnticipator fa = new FileAnticipator(false);
        fa.tearDown();
    }
    
    public void testGenerateRandomHexString() {
        /*
         * Generate a very long string, in hopes that one of the bytes will
         * have a value < 16 so that we can test the "02" part of the format
         * string.
         */
        final int length = 1024;
        
        String s = FileAnticipator.generateRandomHexString(length);
        assertNotNull("random hex string should not be null", s);
        assertEquals("random hex string length", length * 2, s.length());
    }
    
    public void testGenerateRandomHexStringNoDuplication() {
        // This should be long enough to have a very low change of dups
        final int length = 8;
        
        String s1 = FileAnticipator.generateRandomHexString(length);
        assertNotNull("random hex string s1 should not be null", s1);
        assertEquals("random hex string s1 length", length * 2, s1.length());
        
        String s2 = FileAnticipator.generateRandomHexString(length);
        assertNotNull("random hex string s2 should not be null", s2);
        assertEquals("random hex string s2 length", length * 2, s2.length());
        
        assertNotSame("random hex strings s1 and s2 should not be equal", s1, s2);
    }
}
