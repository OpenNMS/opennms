/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Test;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.core.io.ByteArrayResource;

public class DataCollectionConfigFactoryTest {
	private static final File m_rrdRepository = new File(System.getProperty("java.io.tmpdir") + File.separator + "wonka" + File.separator + "rrd" + File.separator + "snmp");

    private static final String m_xml = "<?xml version=\"1.0\"?>\n" + 
            "<datacollection-config\n" + 
            "   rrdRepository = \"" + m_rrdRepository.getAbsolutePath() + File.separator + "\">\n" + 
            "   <snmp-collection name=\"default\"\n" + 
            "       snmpStorageFlag = \"select\">\n" + 
            "       <rrd step = \"300\">\n" + 
            "           <rra>RRA:AVERAGE:0.5:1:8928</rra>\n" + 
            "           <rra>RRA:AVERAGE:0.5:12:8784</rra>\n" + 
            "           <rra>RRA:MIN:0.5:12:8784</rra>\n" + 
            "           <rra>RRA:MAX:0.5:12:8784</rra>\n" + 
            "       </rrd>\n" + 
            "       <groups>\n" + 
            "           <!-- data from standard (mib-2) sources -->\n" + 
            "           <group  name = \"mib2-interfaces\" ifType = \"all\">\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.2.2.1.10\" instance=\"ifIndex\" alias=\"ifInOctets\"    type=\"counter\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.2.2.1.11\" instance=\"ifIndex\" alias=\"ifInUcastpkts\"   type=\"counter\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.2.2.1.12\" instance=\"ifIndex\" alias=\"ifInNUcastpkts\"  type=\"counter\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.2.2.1.13\" instance=\"ifIndex\" alias=\"ifInDiscards\"  type=\"counter\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.2.2.1.14\" instance=\"ifIndex\" alias=\"ifInErrors\"    type=\"counter\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.2.2.1.16\" instance=\"ifIndex\" alias=\"ifOutOctets\"   type=\"counter\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.2.2.1.17\" instance=\"ifIndex\" alias=\"ifOutUcastPkts\"   type=\"counter\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.2.2.1.18\" instance=\"ifIndex\" alias=\"ifOutNUcastPkts\" type=\"counter\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.2.2.1.19\" instance=\"ifIndex\" alias=\"ifOutDiscards\"   type=\"counter\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.2.2.1.20\" instance=\"ifIndex\" alias=\"ifOutErrors\"   type=\"counter\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.31.1.1.1.6\" instance=\"ifIndex\" alias=\"ifHCInOctets\"  type=\"counter\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.31.1.1.1.10\" instance=\"ifIndex\" alias=\"ifHCOutOctets\" type=\"counter\"/>\n" + 
            "           </group>\n" +
            "           <group name=\"mib2-tcp\" ifType=\"ignore\">\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.6.5\" instance=\"0\" alias=\"tcpActiveOpens\" type=\"Counter32\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.6.6\" instance=\"0\" alias=\"tcpPassiveOpens\" type=\"Counter32\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.6.7\" instance=\"0\" alias=\"tcpAttemptFails\" type=\"Counter32\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.6.8\" instance=\"0\" alias=\"tcpEstabResets\" type=\"Counter32\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.6.9\" instance=\"0\" alias=\"tcpCurrEstab\" type=\"Gauge32\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.6.10\" instance=\"0\" alias=\"tcpInSegs\" type=\"Counter32\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.6.11\" instance=\"0\" alias=\"tcpOutSegs\" type=\"Counter32\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.6.12\" instance=\"0\" alias=\"tcpRetransSegs\" type=\"Counter32\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.6.14\" instance=\"0\" alias=\"tcpInErrors\" type=\"Counter32\"/>\n" + 
            "             <mibObj oid=\".1.3.6.1.2.1.6.15\" instance=\"0\" alias=\"tcpOutRsts\" type=\"Counter32\"/>\n" + 
            "           </group>\n" + 
            "       </groups>\n" + 
            "       <systems>\n" + 
            "           <systemDef name = \"Enterprise\">\n" + 
            "               <sysoidMask>.1.3.6.1.4.1.</sysoidMask>\n" + 
            "               <collect>\n" + 
            "                   <includeGroup>mib2-interfaces</includeGroup>\n" + 
            "                   <includeGroup>mib2-tcp</includeGroup>\n" + 
            "               </collect>\n" + 
            "           </systemDef>        \n" + 
            "\n" + 
            "       </systems>\n" + 
            "   </snmp-collection>\n" + 
            "</datacollection-config>\n" + 
            "";

    private static final String m_brocadeXmlFragment = 
    "       <resourceType name=\"brocadeIndex\" label=\"Brocade Switches\">\n" +
    "         <persistenceSelectorStrategy class=\"foo\"/>\n" +
    "         <storageStrategy class=\"foo\"/>\n" +
    "       </resourceType>\n";

    @Test
    public void testSetInstance() throws MarshalException, ValidationException, IOException {
        initDataCollectionFactory(m_xml);
        assertEquals(m_rrdRepository.getAbsolutePath(), DataCollectionConfigFactory.getInstance().getRrdPath());
        assertEquals(0, DataCollectionConfigFactory.getInstance().getMibObjectList("default", ".1.9.9.9.9", "127.0.0.1", 0).size());
        for (MibObject object : DataCollectionConfigFactory.getInstance().getMibObjectList("default", ".1.3.6.1.4.1.200", "127.0.0.1", 0)) {
            assertEquals("Invalid MibObject: " + object, "ifIndex", object.getInstance());
        }
        assertArrayEquals(new String[0], DataCollectionConfigFactory.getInstance().getConfiguredResourceTypes().keySet().toArray(new String[0]));
    }
    
    @Test
    public void testValidResourceType() throws MarshalException, ValidationException, IOException {
    	String modifiedXml = m_xml.replaceFirst("ifIndex", "brocadeIndex").replaceFirst("<groups", m_brocadeXmlFragment + "<groups");
        initDataCollectionFactory(modifiedXml);
        assertEquals(m_rrdRepository.getAbsolutePath(), DataCollectionConfigFactory.getInstance().getRrdPath());
        assertEquals(0, DataCollectionConfigFactory.getInstance().getMibObjectList("default", ".1.9.9.9.9", "127.0.0.1", 0).size());
        List<MibObject> mibObjects = DataCollectionConfigFactory.getInstance().getMibObjectList("default", ".1.3.6.1.4.1.200", "127.0.0.1", 0);
        // Make sure that the first value was edited as intended
        MibObject first = mibObjects.remove(0);
        assertEquals("Invalid MibObject: " + first, "brocadeIndex", first.getInstance());
        for (MibObject object : mibObjects) {
            assertEquals("Invalid MibObject: " + object, "ifIndex", object.getInstance());
        }
        assertArrayEquals(new String[] {"brocadeIndex"}, DataCollectionConfigFactory.getInstance().getConfiguredResourceTypes().keySet().toArray(new String[0]));
    }
    
    @Test
    public void testInvalidResourceType() throws MarshalException, ValidationException, IOException {
        String modifiedXml = m_xml.replaceFirst("ifIndex", "brocadeIndex");
        ThrowableAnticipator ta = new ThrowableAnticipator();
//        ta.anticipate(new DataAccessResourceFailureException("Instance 'brocadeIndex' invalid in mibObj definition for OID '.1.3.6.1.2.1.2.2.1.10' for group 'mib2-interfaces'. Allowable instance values: any positive number, 'ifIndex', or any of the custom resourceTypes."));
        ta.anticipate(new IllegalArgumentException("instance 'brocadeIndex' invalid in mibObj definition for OID '.1.3.6.1.2.1.2.2.1.10' in collection 'default' for group 'mib2-interfaces'.  Allowable instance values: any positive number, 'ifIndex', or any of the configured resourceTypes: (none)"));

        try {
            initDataCollectionFactory(modifiedXml);
            assertArrayEquals(new String[0], DataCollectionConfigFactory.getInstance().getConfiguredResourceTypes().keySet().toArray(new String[0]));
            DataCollectionConfigFactory.getInstance().getConfiguredResourceTypes();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    private static void initDataCollectionFactory(String xmlConfig) {
        DefaultDataCollectionConfigDao dataCollectionDao = new DefaultDataCollectionConfigDao();
        dataCollectionDao.setConfigResource(new ByteArrayResource(xmlConfig.getBytes()));
        // Set the config directory to a blank value so that it doesn't pull in any extra config files
        dataCollectionDao.setConfigDirectory("");
        dataCollectionDao.afterPropertiesSet();
        DataCollectionConfigFactory.setInstance(dataCollectionDao);
    }

}
