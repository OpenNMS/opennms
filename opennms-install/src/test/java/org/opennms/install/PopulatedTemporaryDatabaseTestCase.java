package org.opennms.install;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PopulatedTemporaryDatabaseTestCase extends
        TemporaryDatabaseTestCase {
    
    protected void setUp() throws Exception {
        super.setUp();
        
        initializeDatabase();
    }

    protected void initializeDatabase() throws Exception {
        Installer installer = new Installer();

        // Create a ByteArrayOutputSteam to effectively throw away output.
        installer.m_out = new PrintStream(new ByteArrayOutputStream());
        installer.m_database = getTestDatabase();
        installer.m_pg_driver = getDriver();
        installer.m_pg_url = getUrl();
        installer.m_pg_user = getAdminUser();
        installer.m_pg_pass = getAdminPassword();
        installer.m_user = "opennms";
        
        installer.m_create_sql =
            "../opennms-daemon/src/main/filtered/etc/create.sql";

        installer.m_sql_dir = "../opennms-daemon/src/main/filtered/etc";

        installer.m_debug = false;

        // Read in the table definitions
        installer.readTables();
        
        installer.m_dbconnection = getConnection();

        installer.createSequences();
        installer.updatePlPgsql();
        installer.addStoredProcedures();
        
        installer.createTables();

        installer.m_dbconnection.close();
    }

}
