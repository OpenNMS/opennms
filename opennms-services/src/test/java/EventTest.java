import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;


public class EventTest {

    @Before
    public void setUp() throws Exception {
        System.setProperty("opennms.home", "/Users/agalue/Development/opennms/git/hardware-inventory/target/opennms-1.13.5-SNAPSHOT");
        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME);
        DataSourceConfiguration dsc = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(cfgFile);
            dsc = CastorUtils.unmarshal(DataSourceConfiguration.class, fileInputStream);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        } 
        for (JdbcDataSource jds : dsc.getJdbcDataSourceCollection()) {
            if (jds.getName().equals("opennms_hw")) {
                final String url = jds.getUrl();
                final String user = jds.getUserName();
                final String pwd = jds.getPassword();
                DataSourceFactory.setInstance(new DataSource() {
                    public PrintWriter getLogWriter() throws SQLException { return null; }
                    public int getLoginTimeout() throws SQLException { return 0; }
                    public void setLogWriter(PrintWriter pw) throws SQLException {}
                    public void setLoginTimeout(int tm) throws SQLException {}
                    public boolean isWrapperFor(Class<?> arg0) throws SQLException { return false; }
                    public <T> T unwrap(Class<T> arg0) throws SQLException { return null; }
                    public Connection getConnection(String arg0, String arg1) throws SQLException { return null; }
                    public Connection getConnection() throws SQLException {
                        return DriverManager.getConnection(url,user,pwd);
                    }
                    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                        return null;
                    }
                });
            }
        }
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void test() {
        EventBuilder eb = new EventBuilder("uei.opennms.org/hardware/nodeStatus", "Junit");
        eb.setNodeid(2);
        Event e = eb.getEvent();
        String[] params = new String[] { "hardware[Chassis:entPhysicalModelName]", "hardware[9:entPhysicalDescr]" ,"hardware[~^NPE.*:ceExtNVRAMUsed]" };
        for (String parm : params) {
            String value = EventUtil.getValueOfParm(parm, e);
            System.out.println(parm + "=" + value);
        }
    }

}
