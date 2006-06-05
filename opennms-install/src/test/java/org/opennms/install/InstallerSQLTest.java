//
//  $Id$
//

package org.opennms.install;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.PrintStream;

import org.opennms.test.ThrowableAnticipator;

import junit.framework.TestCase;

public class InstallerSQLTest extends TestCase {
    private Installer m_installer;
    
    private static String s_bug1455CreateSQL =
        "create table category_node (\n"
        + "                categoryId              integer,\n"
        + "                nodeId                  integer,\n"
        + "\n"
        + "                constraint categoryid_fkey1 foreign key (categoryId) references categories(categoryId) ON DELETE CASCADE,\n"
        + "                constraint nodeid_fkey1 foreign key (nodeId) references node ON DELETE CASCADE\n"
        + ");\n";


    protected void setUp() throws Exception {
        m_installer = new Installer();
        // Create a ByteArrayOutputSteam to effectively throw away output.
        m_installer.m_out = new PrintStream(new ByteArrayOutputStream());
    }

    public void tearDown() throws Exception {
    }

    public void testBug1455BadSQL() throws Exception {
        m_installer.readTables(new StringReader(s_bug1455CreateSQL));
        
        ThrowableAnticipator anticipator = new ThrowableAnticipator();
        anticipator.anticipate(new Exception("Cannot parse constraint: constraint categoryid_fkey1 foreign key (categoryId) references categories(categoryId) ON DELETE CASCADE"));
        try {
            m_installer.getForeignKeyConstraints();
        } catch (Throwable t) {
            anticipator.throwableReceived(t);
        }
        anticipator.verifyAnticipated();
    }
    
    public void testBug1455CorrectSQL() throws Exception {
        String modified =
            s_bug1455CreateSQL.replaceFirst("categories\\(categoryId\\)",
                                            "categories (categoryId)");
        m_installer.readTables(new StringReader(modified));
        
        ThrowableAnticipator anticipator = new ThrowableAnticipator();
        try {
            m_installer.getForeignKeyConstraints();
        } catch (Throwable t) {
            anticipator.throwableReceived(t);
        }
        anticipator.verifyAnticipated();
    }
}
