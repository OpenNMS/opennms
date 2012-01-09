package org.opennms.netmgt.jasper.resource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;

import org.junit.Test;

public class ResourceCommandTest {

    @Test
    public void test() throws JRException {
        JRDataSource dataSource = new ResourceQueryCommand().executeCommand(getCommand());
        assertNotNull(dataSource);
        assertTrue(dataSource.next());
    }

    private String getCommand() {
        return "-rrdDir src/test/resources/share/rrd/snmp  -nodeid 10 -resourceName nsVpnMonitor";
    }
    

}
