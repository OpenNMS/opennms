package org.opennms.install;

import java.io.IOException;
import java.sql.SQLException;

import junit.framework.TestCase;

public class InstallerJniTest extends TestCase {

    private Installer m_installer;

    protected void setUp() throws SQLException {
        m_installer = new Installer();
    }
    
    public void XXXtestPingLocalhost() throws IOException {
        System.setProperty("opennms.library.jicmp", "/Users/dgregor/opennms/trunk/opennms/target/opennms-1.3.2-SNAPSHOT/lib/libjicmp.jnilib");
        m_installer.pingLocalhost();
    }
}
