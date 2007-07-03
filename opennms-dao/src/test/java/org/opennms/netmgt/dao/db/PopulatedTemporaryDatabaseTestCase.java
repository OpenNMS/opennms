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
// Modifications:
//
// 2007 Apr 05: Use ConfigurationFileUtils.getTopProjectDirectory() to get the top-level project directory and merge two regular expressions for matching iplike.{so,dylib} that were nearly identical. - dj@opennms.org
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
package org.opennms.netmgt.dao.db;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;

import org.opennms.test.ConfigurationTestUtils;
import org.springframework.util.StringUtils;

public class PopulatedTemporaryDatabaseTestCase extends
        TemporaryDatabaseTestCase {
    
    private InstallerDb m_installerDb;
    
    private ByteArrayOutputStream m_outputStream;

    private boolean m_setupIpLike = false;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        initializeDatabase();
    }

    protected void initializeDatabase() throws Exception {
        if (!isEnabled()) {
            return;
        }
        
        m_installerDb = new InstallerDb();

        // Create a ByteArrayOutputSteam to effectively throw away output.
        resetOutputStream();
        m_installerDb.setDatabaseName(getTestDatabase());
        m_installerDb.setDataSource(getDataSource());
        
        m_installerDb.setCreateSqlLocation(
            "../opennms-daemon/src/main/filtered/etc/create.sql");

        m_installerDb.setStoredProcedureDirectory(
            "../opennms-daemon/src/main/filtered/etc");

        //installerDb.setDebug(true);

        m_installerDb.readTables();
        
        m_installerDb.createSequences();
        m_installerDb.updatePlPgsql();
        m_installerDb.addStoredProcedures();
        

        /*
         * Here's an example of an iplike function that always returns true.
         * CREATE OR REPLACE FUNCTION iplike(text, text) RETURNS bool AS ' BEGIN RETURN true; END; ' LANGUAGE 'plpgsql';
         * 
         * Found this in BaseIntegrationTestCase.
         */

        if (m_setupIpLike) {
            m_installerDb.setPgIpLikeLocation(null);
            m_installerDb.updateIplike();
        }

        m_installerDb.createTables();

        m_installerDb.closeConnection();
    }

    protected File findIpLikeLibrary() {
        File topDir = ConfigurationTestUtils.getTopProjectDirectory();
        
        File ipLikeDir = new File(topDir, "opennms-iplike");
        assertTrue("iplike directory exists at ../opennms-iplike: " + ipLikeDir.getAbsolutePath(), ipLikeDir.exists());
        
        File[] ipLikePlatformDirs = ipLikeDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.getName().matches("opennms-iplike-.*") && file.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        assertTrue("expecting at least one opennms iplike platform directory in " + ipLikeDir.getAbsolutePath() + "; got: " + StringUtils.arrayToDelimitedString(ipLikePlatformDirs, ", "), ipLikePlatformDirs.length > 0);

        File ipLikeFile = null;
        for (File ipLikePlatformDir : ipLikePlatformDirs) {
            assertTrue("iplike platform directory does not exist but was listed in directory listing: " + ipLikePlatformDir.getAbsolutePath(), ipLikePlatformDir.exists());
            
            File ipLikeTargetDir = new File(ipLikePlatformDir, "target");
            if (!ipLikeTargetDir.exists() || !ipLikeTargetDir.isDirectory()) {
                // Skip this one
                continue;
            }
          
            File[] ipLikeFiles = ipLikeTargetDir.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    if (file.isFile() && file.getName().matches("opennms-iplike-.*\\.(so|dylib)")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            assertFalse("expecting zero or one iplike file in " + ipLikeTargetDir.getAbsolutePath() + "; got: " + StringUtils.arrayToDelimitedString(ipLikeFiles, ", "), ipLikeFiles.length > 1);
            
            if (ipLikeFiles.length == 1) {
                ipLikeFile = ipLikeFiles[0];
            }
            
        }
        
        assertNotNull("Could not find iplike shared object in a target directory in any of these directories: " + StringUtils.arrayToDelimitedString(ipLikePlatformDirs, ", "), ipLikeFile);
        
        return ipLikeFile;
    }

    public ByteArrayOutputStream getOutputStream() {
        return m_outputStream;
    }
    
    public void resetOutputStream() {
        m_outputStream = new ByteArrayOutputStream();
        m_installerDb.setOutputStream(new PrintStream(m_outputStream));
    }
    
    public void setSetupIpLike(boolean setupIpLike) {
        m_setupIpLike = setupIpLike;
    }
    
    protected InstallerDb getInstallerDb() {
        return m_installerDb;
    }
}
