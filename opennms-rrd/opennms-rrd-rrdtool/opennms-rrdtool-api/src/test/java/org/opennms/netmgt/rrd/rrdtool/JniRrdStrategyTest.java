/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Jun 23: Add comments about why the tests are disabled and eliminate
 *              warnings. - dj@opennms.org
 * 2007 Mar 19: Add a test for creating a graph.  Doesn't seem to work yet, though. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.rrd.rrdtool;

import java.io.File;
import java.io.FileFilter;

import junit.framework.TestCase;

import org.opennms.test.mock.MockLogAppender;
import org.springframework.util.StringUtils;

/**
 * Unit tests for the JniRrdStrategy.  This requires that the shared object
 * for JNI rrdtool support can be found and linked (see findJrrdLibrary).
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class JniRrdStrategyTest extends TestCase {
    
    private JniRrdStrategy m_strategy;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockLogAppender.setupLogging();
        
        // FIXME: This is disabled.  See testGraph for details.
        if (false) {
            System.setProperty("opennms.library.jrrd", findJrrdLibrary().getAbsolutePath());
            
            m_strategy = new JniRrdStrategy();
            m_strategy.initialize();
        }
    }
    
    public void testInitialize() {
        // Do nothing; just checking to see if setUp() worked.
    }
    
    /*
     * FIXME: This is disabled since the test doesn't work if building from
     * scratch.  This should likely be moved into the platform modules.
     */
    public void testGraph() throws Exception {
        long end = System.currentTimeMillis();
        long start = end - (24 * 60 * 60 * 1000);
        String[] command = new String[] {
                "rrdtool",
                "graph", 
                "-",
                "--start=" + start,
                "--end=" + end,
                "CDEF:a=1",
                "GPRINT:a:AVERAGE:\"%8.2lf\\n\""
        };
        
        m_strategy.createGraph(StringUtils.arrayToDelimitedString(command, " "), new File(""));
    }

    private File findJrrdLibrary() {
        File parentDir = new File("..");
        assertTrue("parent directory exists at ..: " + parentDir.getAbsolutePath(), parentDir.exists());
        
        File parentPomXml = new File(parentDir, "pom.xml");
        assertTrue("parent directory's pom.xml exists at ../pom.xml: " + parentPomXml.getAbsolutePath(), parentPomXml.exists());
        
        File jniDir = new File(parentDir, "opennms-rrdtool-jni");
        assertTrue("opennms-rrdtool-jni directory exists at ../opennms-rrdtool-jni: " + jniDir.getAbsolutePath(), jniDir.exists());
        
        File[] jniPlatformDirs = jniDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.getName().matches("opennms-rrdtool-jni-.*") && file.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        assertTrue("expecting at least one opennms opennms-rrdtool-jni platform directory in " + jniDir.getAbsolutePath() + "; got: " + StringUtils.arrayToDelimitedString(jniPlatformDirs, ", "), jniPlatformDirs.length > 0);

        File jniFile = null;
        for (File jniPlatformDir : jniPlatformDirs) {
            assertTrue("opennms-rrdtool-jni platform directory does not exist but was listed in directory listing: " + jniPlatformDir.getAbsolutePath(), jniPlatformDir.exists());
            
            File jniTargetDir = new File(jniPlatformDir, "target");
            if (!jniTargetDir.exists() || !jniTargetDir.isDirectory()) {
                // Skip this one
                continue;
            }
          
            File[] jniFiles = jniTargetDir.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    if (file.isFile()
                        && (file.getName().matches("opennms-rrdtool-jni-.*\\.so")
                            || file.getName().matches("opennms-rrdtool-jni-.*\\.jnilib"))) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            assertFalse("expecting zero or one opennms-rrdtool-jni file in " + jniTargetDir.getAbsolutePath() + "; got: " + StringUtils.arrayToDelimitedString(jniFiles, ", "), jniFiles.length > 1);
            
            if (jniFiles.length == 1) {
                jniFile = jniFiles[0];
            }
            
        }
        
        assertNotNull("Could not find opennms-rrdtool-jni shared object in a target directory in any of these directories: " + StringUtils.arrayToDelimitedString(jniPlatformDirs, ", "), jniFile);
        
        return jniFile;
    }
}
