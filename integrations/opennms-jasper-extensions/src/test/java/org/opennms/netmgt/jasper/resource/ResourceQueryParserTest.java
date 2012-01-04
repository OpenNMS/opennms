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
        assertEquals("src/test/resources/share/rrd/snmp", rQuery.getRrdDir());
        assertEquals("10", rQuery.getNodeId());
        assertEquals("nsVpnMonitor", rQuery.getResourceName());
    }
    
    @Test
    public void testCommandParsingWithFilter() {
        ResourceQueryCommandParser parser = new ResourceQueryCommandParser();
        ResourceQuery rQuery = parser.parseQueryCommand(getResourceQueryWithFilter());
        
        assertNotNull(rQuery);
        assertEquals("src/test/resources/share/rrd/snmp", rQuery.getRrdDir());
        assertEquals("10", rQuery.getNodeId());
        assertEquals("nsVpnMonitor", rQuery.getResourceName());
        
        String[] filters = rQuery.getFilters();
        assertEquals(2, filters.length);
        assertEquals("http.dump", filters[0]);
        assertEquals("icmp.jrb", filters[1]);
        
    }
    
    private String getResourceQuery() {
        return "-rrdDir src/test/resources/share/rrd/snmp  -nodeid 10 -resourceName nsVpnMonitor";
    }
    
    private String getResourceQueryWithFilter() {
        return "-rrdDir src/test/resources/share/rrd/snmp -nodeid 10 -resourceName nsVpnMonitor -filters http.dump;icmp.jrb";
    }

}
