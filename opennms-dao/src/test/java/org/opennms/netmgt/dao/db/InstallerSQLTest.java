//
//  $Id$
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
