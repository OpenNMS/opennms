package org.opennms.netmgt.jasper.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JRDesignField;

import org.junit.Before;
import org.junit.Test;

public class ResourceCommandTest {
    
    @Before
    public void setUp() {
        System.setProperty("org.opennms.rrd.storeByGroup", "False");
    }
    
    @Test
    public void testDatasourceWithNoFilters() throws JRException {
        JRDataSource dataSource = new ResourceQueryCommand().executeCommand(getCommand());
        assertNotNull(dataSource);
        assertTrue(dataSource.next());
        
        JRDesignField pathField = new JRDesignField();
        pathField.setName("path");
        String pathVal = (String) dataSource.getFieldValue(pathField);
        assertNotNull("", pathVal);
        assertEquals("/Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_1", pathVal);
        
        JRDesignField filterField = new JRDesignField();
        filterField.setName("icmp");
        String val = (String) dataSource.getFieldValue(filterField);
        assertNull(val);
        
    }
    
    @Test
    public void testDatasourceWithFilters() throws JRException {
        JRDataSource dataSource = new ResourceQueryCommand().executeCommand(getCommandWithFilter());
        assertNotNull(dataSource);
        assertTrue(dataSource.next());
        
        JRDesignField pathField = new JRDesignField();
        pathField.setName("path");
        assertNotNull("", dataSource.getFieldValue(pathField));
        
        JRDesignField filterField = new JRDesignField();
        filterField.setName("icmp");
        
        String dsFieldValue = (String) dataSource.getFieldValue(filterField);
        assertNotNull(dsFieldValue);
        assertTrue(dsFieldValue.matches(".*src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_1/icmp.jrb"));
    }
    
    @Test
    public void testLocalDatasourceWithFilters() throws JRException {
        System.setProperty("org.opennms.rrd.storeByGroup", "True");
        JRDataSource dataSource = new ResourceQueryCommand().executeCommand("--rrdDir /Users/thedesloge/git/opennms/target/opennms-1.8.17-SNAPSHOT/share/rrd/snmp" +
        		" --nodeId 47" +
        		" --resourceName opennms-jvm" +
        		" --dsNames TotalMemory");
        assertNotNull(dataSource);
        assertTrue(dataSource.next());
        
        JRDesignField pathField = new JRDesignField();
        pathField.setName("path");
        String path = (String) dataSource.getFieldValue(pathField);
        assertEquals("/Users/thedesloge/git/opennms/target/opennms-1.8.17-SNAPSHOT/share/rrd/snmp/47/opennms-jvm", dataSource.getFieldValue(pathField));
        
        JRDesignField filterField = new JRDesignField();
        filterField.setName("TotalMemory");
        
        String dsFieldValue = (String) dataSource.getFieldValue(filterField);
        assertNotNull(dsFieldValue);
        assertEquals("/Users/thedesloge/git/opennms/target/opennms-1.8.17-SNAPSHOT/share/rrd/snmp/47/opennms-jvm/java_lang_type_OperatingSystem.jrb", dsFieldValue);
        
        assertFalse(dataSource.next());
    }
    
    @Test
    public void testLocalNode9() throws JRException {
        JRDataSource dataSource = new ResourceQueryCommand().executeCommand("--rrdDir /Users/thedesloge/git/opennms/target/opennms-1.8.17-SNAPSHOT/share/rrd/snmp" +
                " --nodeId 9" +
                " --resourceName opennms-jvm" +
                " --dsNames TotalMemory");
        assertNotNull(dataSource);
        assertTrue(dataSource.next());
        
        JRDesignField pathField = new JRDesignField();
        pathField.setName("path");
        String path = (String) dataSource.getFieldValue(pathField);
        assertEquals("/Users/thedesloge/git/opennms/target/opennms-1.8.17-SNAPSHOT/share/rrd/snmp/9/opennms-jvm", dataSource.getFieldValue(pathField));
        
        JRDesignField filterField = new JRDesignField();
        filterField.setName("TotalMemory");
        
        String dsFieldValue = (String) dataSource.getFieldValue(filterField);
        assertNotNull(dsFieldValue);
        assertEquals("/Users/thedesloge/git/opennms/target/opennms-1.8.17-SNAPSHOT/share/rrd/snmp/9/opennms-jvm/java_lang_type_OperatingSystem.jrb", dsFieldValue);
        
        assertFalse(dataSource.next());
    }
    
    @Test
    public void testNoDatasourceWithFilters() throws JRException {
        JRDataSource dataSource = new ResourceQueryCommand().executeCommand(getCommandWithBogusFilter());
        assertNotNull(dataSource);
        boolean next = dataSource.next();
        
        assertFalse(next);
    }
    
    
    private String getCommandWithBogusFilter() {
        return "--rrdDir src/test/resources/share/rrd/snmp  --nodeid 10 --resourceName nsVpnMonitor --dsNames bogus";
    }

    private String getCommand() {
        return "--rrdDir src/test/resources/share/rrd/snmp  --nodeid 10 --resourceName nsVpnMonitor";
    }
    
    private String getCommandWithFilter() {
        return "--rrdDir src/test/resources/share/rrd/snmp  --nodeid 10 --resourceName nsVpnMonitor --dsNames icmp";
    }

}
