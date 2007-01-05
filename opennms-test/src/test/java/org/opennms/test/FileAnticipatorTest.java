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
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class FileAnticipatorTest extends TestCase {
    private FileAnticipator m_anticipator;
    
    public void setUp() throws IOException {
        m_anticipator = new FileAnticipator();
    }
    
    public void tearDown() {
        m_anticipator.tearDown();
    }
    
    public void testConstructor() {
        // Empty... this effectively tests that setUp() works.
    }
    
    public void testExpecting() {
        String file = "/FileAnticipatorTest_bogus_" + System.currentTimeMillis();
        m_anticipator.expecting(null, file);
    }
    
    public void testDeleteExpected() {
        m_anticipator.deleteExpected();
    }
    
    public void testExpectingDeleteExpected() throws IOException {
        String file = "FileAnticipatorTest_" + System.currentTimeMillis();
        File tempFile = m_anticipator.expecting(null, file);
        assertTrue("createNewFile: " + tempFile.getAbsolutePath(),
                   tempFile.createNewFile());
        m_anticipator.deleteExpected();
    }
    
    public void testExpectingDeleteExpectedBogus() {
        String file = "/FileAnticipatorTest_bogus_" + System.currentTimeMillis();
        String expected = "\"" + file + "\" deleted";
                
        try {
            m_anticipator.expecting(null, file);
            m_anticipator.deleteExpected();
        } catch (Throwable t) {
            if (expected.equals(t.getMessage())) {
                return; // This is the exception we were expecting
            }
            fail("Received unexpected exception.  Was expecting:\n" + expected +
                 "\nReceived:\n" + t.getMessage());
        }
        fail("Did not receive excpected exception");
    }
    
    public void testTempDir() throws IOException {
        String file = "FileAnticipatorTest_tempDir_" + System.currentTimeMillis();
        m_anticipator.tempDir(file);
        m_anticipator.deleteExpected();
    }
    
    public void testTempFile() throws IOException {
        String file = "FileAnticipatorTest_tempFile_" + System.currentTimeMillis();
        m_anticipator.tempFile(file);
        m_anticipator.deleteExpected();
    }
    
    public void testTempFileContents() throws IOException {
        String file = "FileAnticipatorTest_tempFile_" + System.currentTimeMillis();
        String contents = "yay!";
        File yay = m_anticipator.tempFile(file, contents);
        
        StringBuffer b = new StringBuffer();
        FileInputStream is = new FileInputStream(yay);
        int i;
        while ((i = is.read()) != -1) {
            b.append(new Character((char) i));
        }
        is.close();
        
        assertEquals("file contents", contents, b.toString());
        
        m_anticipator.deleteExpected();
    }
}
