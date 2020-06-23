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
import static org.junit.Assert.assertNotNull;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRQuery;
import net.sf.jasperreports.engine.JRQueryChunk;
import net.sf.jasperreports.engine.base.JRBaseQuery;
import net.sf.jasperreports.engine.design.JRDesignDataset;

import org.junit.Test;

public class ResourceQueryFieldsProviderTest {
    
    private class TestDatasetImpl extends JRDesignDataset{
        
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public TestDatasetImpl() {
            super(true);
        }

        private String m_queryText = "";
        
        public JRQuery getQuery() {
            JRQuery query = new JRBaseQuery() {

                /**
                 * 
                 */
                private static final long serialVersionUID = 1L;

                public JRQueryChunk[] getChunks() {
                    return null;
                }

                public String getLanguage() {
                    return "resourceQuery";
                }

                public String getText() {
                    return getQueryText();
                }
            };
            return query;
        }

        public String getQueryText() {
            return m_queryText;
        }

        public void setQueryText(String queryText) {
            m_queryText = queryText;
        }
        
    }
    
    @Test
    public void testQueryWithDsNames() throws UnsupportedOperationException, JRException {
        TestDatasetImpl reportDataset = new TestDatasetImpl();
        reportDataset.setQueryText("--rrdDir share/rrd/snmp --nodeId 47 --resourceName opennms-jvm --dsName TotalMemory");
        
        ResourceQueryFieldsProvider provider = new ResourceQueryFieldsProvider();
        JRField[] fields = provider.getFields(null, reportDataset, null);
        
        assertNotNull(fields);
        assertEquals(2, fields.length);
        assertEquals("Path", fields[0].getName());
        assertEquals("TotalMemory", fields[1].getName());
    }
    
    @Test
    public void testQueryWithManyDsNames() throws UnsupportedOperationException, JRException {
        TestDatasetImpl reportDataset = new TestDatasetImpl();
        reportDataset.setQueryText("--rrdDir share/rrd/snmp --nodeId 47 --resourceName opennms-jvm --dsName TotalMemory,DsName1,DsName2,DsName3");
        
        ResourceQueryFieldsProvider provider = new ResourceQueryFieldsProvider();
        JRField[] fields = provider.getFields(null, reportDataset, null);
        
        assertNotNull(fields);
        assertEquals(5, fields.length);
        assertEquals("Path", fields[0].getName());
        assertEquals("TotalMemory", fields[1].getName());
        assertEquals("DsName1", fields[2].getName());
        assertEquals("DsName2", fields[3].getName());
        assertEquals("DsName3", fields[4].getName());
        
    }
    
    @Test
    public void testQueryWithStringProperties() throws UnsupportedOperationException, JRException {
        TestDatasetImpl reportDataset = new TestDatasetImpl();
        reportDataset.setQueryText("--rrdDir share/rrd/snmp --nodeId 47 --resourceName opennms-jvm --dsName TotalMemory,DsName1,DsName2,DsName3 --string nsVpnMonVpnName");
        
        ResourceQueryFieldsProvider provider = new ResourceQueryFieldsProvider();
        JRField[] fields = provider.getFields(null, reportDataset, null);
        
        assertNotNull(fields);
        assertEquals(6, fields.length);
        assertEquals("Path", fields[0].getName());
        assertEquals("TotalMemory", fields[1].getName());
        assertEquals("DsName1", fields[2].getName());
        assertEquals("DsName2", fields[3].getName());
        assertEquals("DsName3", fields[4].getName());
        assertEquals("nsVpnMonVpnName", fields[5].getName());
    }
    
    @Test
    public void testQueryWithMultipleStringProperties() throws UnsupportedOperationException, JRException {
        TestDatasetImpl reportDataset = new TestDatasetImpl();
        reportDataset.setQueryText("--rrdDir share/rrd/snmp --nodeId 47 --resourceName opennms-jvm --dsName TotalMemory,DsName1,DsName2,DsName3 --string nsVpnMonVpnName,name2,name3");
        
        ResourceQueryFieldsProvider provider = new ResourceQueryFieldsProvider();
        JRField[] fields = provider.getFields(null, reportDataset, null);
        
        assertNotNull(fields);
        assertEquals(8, fields.length);
        assertEquals("Path", fields[0].getName());
        assertEquals("TotalMemory", fields[1].getName());
        assertEquals("DsName1", fields[2].getName());
        assertEquals("DsName2", fields[3].getName());
        assertEquals("DsName3", fields[4].getName());
        assertEquals("nsVpnMonVpnName", fields[5].getName());
        assertEquals("name2", fields[6].getName());
        assertEquals("name3", fields[7].getName());
    }
    
    @Test
    public void testQueryWithoutDsNames() throws UnsupportedOperationException, JRException {
        TestDatasetImpl reportDataset = new TestDatasetImpl();
        reportDataset.setQueryText("--rrdDir share/rrd/snmp --nodeId 47 --resourceName opennms-jvm");
        
        ResourceQueryFieldsProvider provider = new ResourceQueryFieldsProvider();
        JRField[] fields = provider.getFields(null, reportDataset, null);
        
        assertNotNull(fields);
        assertEquals(1, fields.length);
        assertEquals("Path", fields[0].getName());
    }
    
    

}
