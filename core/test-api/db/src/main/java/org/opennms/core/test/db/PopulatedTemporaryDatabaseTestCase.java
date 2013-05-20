/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.core.test.db;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;

import org.opennms.core.db.install.InstallerDb;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.springframework.util.StringUtils;

/**
 * @deprecated Use an annotation-based temporary database with {@link JUnitTemporaryDatabase} and autowire a 
 * DatabasePopulator to insert a standard set of content into the database. The context that contains the 
 * DatabasePopulator is <code>classpath:/META-INF/opennms/applicationContext-databasePopulator.xml</code>.
 */
public class PopulatedTemporaryDatabaseTestCase extends
        TemporaryDatabaseTestCase {
    
    private InstallerDb m_installerDb = new InstallerDb();
    
    private ByteArrayOutputStream m_outputStream;

    private boolean m_setupIpLike = false;

    private boolean m_insertData = false;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        try {
            initializeDatabase();
        } finally {
            m_installerDb.closeConnection();
        }
    }

    protected void initializeDatabase() throws Exception {
        if (!isEnabled()) {
            return;
        }

        // Create a ByteArrayOutputSteam to effectively throw away output.
        resetOutputStream();
        m_installerDb.setDatabaseName(getTestDatabase());
        m_installerDb.setDataSource(getDataSource());

        m_installerDb.setAdminDataSource(getAdminDataSource());
        m_installerDb.setPostgresOpennmsUser(getAdminUser());

        m_installerDb.setCreateSqlLocation(ConfigurationTestUtils.getFileForConfigFile("create.sql").getAbsolutePath());
        m_installerDb.setStoredProcedureDirectory(ConfigurationTestUtils.getFileForConfigFile("getPercentAvailabilityInWindow.sql").getParentFile().getAbsolutePath());

        //m_installerDb.setDebug(true);

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
            m_installerDb.setPostgresIpLikeLocation(null);
            m_installerDb.updateIplike();
        }

        m_installerDb.createTables();
        
        if(m_insertData) {
            m_installerDb.insertData();
        }
        
    }

    protected File findIpLikeLibrary() {
        File topDir = ConfigurationTestUtils.getTopProjectDirectory();
        
        File ipLikeDir = new File(topDir, "opennms-iplike");
        assertTrue("iplike directory exists at ../opennms-iplike: " + ipLikeDir.getAbsolutePath(), ipLikeDir.exists());
        
        File[] ipLikePlatformDirs = ipLikeDir.listFiles(new FileFilter() {
            @Override
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
                @Override
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
    
    public void setInsertData(boolean insertData) throws Exception {
        m_insertData = insertData;
    }
    
    public void setSetupIpLike(boolean setupIpLike) {
        m_setupIpLike = setupIpLike;
    }
    
    protected InstallerDb getInstallerDb() {
        return m_installerDb;
    }
}
