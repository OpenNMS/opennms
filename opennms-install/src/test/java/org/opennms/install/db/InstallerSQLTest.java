/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.install.db;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import junit.framework.TestCase;

import org.opennms.core.db.install.InstallerDb;

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
