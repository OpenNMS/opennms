package org.opennms.netmgt.dao.db;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PopulatedTemporaryDatabaseTestCase extends
        TemporaryDatabaseTestCase {
    
    private InstallerDb installerDb;
    
    private ByteArrayOutputStream m_outputStream;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        initializeDatabase();
    }

    protected void initializeDatabase() throws Exception {
        if (!isEnabled()) {
            return;
        }
        
        installerDb = new InstallerDb();

        // Create a ByteArrayOutputSteam to effectively throw away output.
        resetOutputStream();
        installerDb.setDatabaseName(getTestDatabase());
        installerDb.setDataSource(getDataSource());
        
        installerDb.setCreateSqlLocation(
            "../opennms-daemon/src/main/filtered/etc/create.sql");

        installerDb.setStoredProcedureDirectory(
            "../opennms-daemon/src/main/filtered/etc");

        //installerDb.setDebug(true);

        installerDb.readTables();
        
        installerDb.createSequences();
        installerDb.updatePlPgsql();
        installerDb.addStoredProcedures();
        
        installerDb.createTables();

        installerDb.closeConnection();
    }

    public ByteArrayOutputStream getOutputStream() {
        return m_outputStream;
    }
    
    public void resetOutputStream() {
        m_outputStream = new ByteArrayOutputStream();
        installerDb.setOutputStream(new PrintStream(m_outputStream));
    }
}
