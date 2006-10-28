package org.opennms.netmgt.dao.db;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PopulatedTemporaryDatabaseTestCase extends
        TemporaryDatabaseTestCase {
    
    private InstallerDb m_installerDb;
    
    private ByteArrayOutputStream m_outputStream;
    
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
        
        m_installerDb.createTables();

        m_installerDb.closeConnection();
    }

    public ByteArrayOutputStream getOutputStream() {
        return m_outputStream;
    }
    
    public void resetOutputStream() {
        m_outputStream = new ByteArrayOutputStream();
        m_installerDb.setOutputStream(new PrintStream(m_outputStream));
    }
}
