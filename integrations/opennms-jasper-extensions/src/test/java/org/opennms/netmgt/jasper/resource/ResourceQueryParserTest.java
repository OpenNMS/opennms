package org.opennms.netmgt.jasper.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ResourceQueryParserTest {
    
    
    @Test
    public void testCommandParsing() {
        ResourceQueryCommandParser parser = new ResourceQueryCommandParser();
        ResourceQuery rQuery = parser.parseQueryCommand(getResourceQuery());
        
        assertNotNull(rQuery);
        //TODO: change the filename assertion to check for not null before adding to 1.8
        assertEquals("/Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp", rQuery.getRrdDir());
        assertEquals("10", rQuery.getNodeId());
        assertEquals("nsVpnMonitor", rQuery.getResourceName());
    }
    
    @Test
    public void testCommandParsingWithFilter() {
        ResourceQueryCommandParser parser = new ResourceQueryCommandParser();
        ResourceQuery rQuery = parser.parseQueryCommand(getResourceQueryWithFilter());
        
        assertNotNull(rQuery);
        //TODO: change the filename assertion to check for not null before adding to 1.8
        assertEquals("/Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp", rQuery.getRrdDir());
        assertEquals("10", rQuery.getNodeId());
        assertEquals("nsVpnMonitor", rQuery.getResourceName());
        
        String[] filters = rQuery.getFilters();
        assertEquals(2, filters.length);
        assertEquals("http.dump", filters[0]);
        assertEquals("icmp.jrb", filters[1]);
        
    }
    
    @Test
    public void testForIReport() {
        ResourceQueryCommandParser parser = new ResourceQueryCommandParser();
        ResourceQuery rQuery = parser.parseQueryCommand("--rrdDir        /Users/thedesloge/git/opennms/target/opennms-1.8.17-SNAPSHOT/share/rrd/snmp  --nodeId 9 --resourceName opennms-jvm");
        
        assertNotNull(rQuery);
        //TODO: change the filename assertion to check for not null before adding to 1.8
        assertEquals("/Users/thedesloge/git/opennms/target/opennms-1.8.17-SNAPSHOT/share/rrd/snmp", rQuery.getRrdDir());
        assertEquals("9", rQuery.getNodeId());
        assertEquals("opennms-jvm", rQuery.getResourceName());
    }
    
    private String getResourceQuery() {
        return "--rrdDir /Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp  --nodeid 10 --resourceName nsVpnMonitor";
    }
    
    private String getResourceQueryWithFilter() {
        return "--rrdDir /Users/thedesloge/git/opennms/integrations/opennms-jasper-extensions/src/test/resources/share/rrd/snmp --nodeid 10 --resourceName nsVpnMonitor --filenames http.dump;icmp.jrb";
    }

}
