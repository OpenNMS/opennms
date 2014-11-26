/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jasper.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JRDesignField;

import org.junit.Before;
import org.junit.Test;

public class ResourceCommandTest {
    
    @Before
    public void setUp() throws IOException {
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
        assertNotNull(pathVal);
        assertTrue(String.format("Path does not match: %s", pathVal), pathVal.matches(".*src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_1"));
        
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
        filterField.setName("nsVpnMonBytesIn");
        
        String dsFieldValue = (String) dataSource.getFieldValue(filterField);
        assertNotNull(dsFieldValue);
        assertTrue(dsFieldValue.matches(".*src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_1/nsVpnMonBytesIn.jrb"));
    }
    
    
    @Test
    public void testDataSourceWithStringProperties() throws JRException {
        JRDataSource dataSource = new ResourceQueryCommand().executeCommand("--rrdDir src/test/resources/share/rrd/snmp" +
                " --nodeId 10" +
                " --resourceType nsVpnMonitor" +
                " --dsName nsVpnMonBytesIn" +
                " --string nsVpnMonVpnName");
        assertNotNull(dataSource);
        assertTrue(dataSource.next());
        
        JRDesignField pathField = new JRDesignField();
        pathField.setName("path");
        String path = (String) dataSource.getFieldValue(pathField);
        System.out.println("path retVal: " + path);
        assertTrue(path.matches(".*src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_1"));
        
        JRDesignField filterField = new JRDesignField();
        filterField.setName("nsVpnMonBytesIn");
        
        String dsFieldValue = (String) dataSource.getFieldValue(filterField);
        assertNotNull(dsFieldValue);
        assertTrue(dsFieldValue.matches(".*src/test/resources/share/rrd/snmp/10/nsVpnMonitor/tun_id_1/nsVpnMonBytesIn.jrb"));
        
        JRDesignField stringPropField = new JRDesignField();
        stringPropField.setName("nsVpnMonVpnName");
        String stringPropFieldVal = (String) dataSource.getFieldValue(stringPropField);
        assertNotNull(stringPropFieldVal);
        assertEquals("998r3-irving-tx-primary", stringPropFieldVal);
        
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
        return "--rrdDir src/test/resources/share/rrd/snmp  --nodeid 10 --resourceType nsVpnMonitor --dsName bogus";
    }

    private String getCommand() {
        return "--rrdDir src/test/resources/share/rrd/snmp  --nodeid 10 --resourceType nsVpnMonitor";
    }
    
    private String getCommandWithFilter() {
        return "--rrdDir src/test/resources/share/rrd/snmp  --nodeid 10 --resourceType nsVpnMonitor --dsName nsVpnMonBytesIn";
    }

}
