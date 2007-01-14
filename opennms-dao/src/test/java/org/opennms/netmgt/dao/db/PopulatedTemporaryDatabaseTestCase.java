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
package org.opennms.netmgt.dao.db;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;

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
        
        if (m_setupIpLike) {
            m_installerDb.setPgIpLikeLocation(findIpLikeLibrary().getAbsolutePath());
            m_installerDb.updateIplike();
        }

        m_installerDb.createTables();

        m_installerDb.closeConnection();
    }

    private File findIpLikeLibrary() {
        File topTargetDir = new File("../target");
        assertTrue("top-level target directory exists at ../target", topTargetDir.exists());
        
        File[] distDirs = topTargetDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.getName().matches("opennms-\\d.*") && file.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        assertEquals("expecting exactly one opennms distribution directory", 1, distDirs.length);
        
        File libDir = new File(distDirs[0], "lib");
        assertTrue("lib directory exists", libDir.isDirectory());

        File[] iplikeFiles = libDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.getName().matches("iplike\\..*") && file.isFile()) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        assertEquals("expecting exactly one iplike file", 1, iplikeFiles.length);
        
        return iplikeFiles[0];
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
}
