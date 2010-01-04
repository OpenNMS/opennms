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
//
//  $Id: InstallerSQLTest.java 5380 2007-01-05 15:36:05Z brozow $
//

package org.opennms.netmgt.dao.db;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.PrintStream;

import org.opennms.netmgt.dao.db.InstallerDb;

import junit.framework.TestCase;

public class InstallerSQLTest extends TestCase {
    private InstallerDb m_installerDb;
    
    private static String s_bug1455CreateSQL =
        "create table category_node (\n"
        + "                categoryId              integer,\n"
        + "                nodeId                  integer,\n"
        + "\n"
        + "                constraint categoryid_fkey1 foreign key (categoryId) references categories(categoryId) ON DELETE CASCADE,\n"
        + "                constraint nodeid_fkey1 foreign key (nodeId) references node ON DELETE CASCADE\n"
        + ");\n";


    protected void setUp() throws Exception {
        m_installerDb = new InstallerDb();

        // Throw away installer output
        m_installerDb.setOutputStream(new PrintStream(new ByteArrayOutputStream()));
    }

    public void tearDown() throws Exception {
    }

    public void testBug1455NoSpace() throws Exception {
        m_installerDb.readTables(new StringReader(s_bug1455CreateSQL));
        
        m_installerDb.getForeignKeyConstraints();
    }
    
    public void testBug1455WithSpace() throws Exception {
        String modified =
            s_bug1455CreateSQL.replaceFirst("categories\\(categoryId\\)",
                                            "categories (categoryId)");
        m_installerDb.readTables(new StringReader(modified));
        m_installerDb.getForeignKeyConstraints();
    }
}
