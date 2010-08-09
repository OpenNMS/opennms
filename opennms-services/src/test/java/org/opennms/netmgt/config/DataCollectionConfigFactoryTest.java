//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Test;
import org.opennms.netmgt.dao.castor.DefaultDataCollectionConfigDao;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.core.io.ByteArrayResource;

public class DataCollectionConfigFactoryTest {

    private static String m_xml = "<?xml version=\"1.0\"?>\n" + 
            "<datacollection-config\n" + 
            "   rrdRepository = \"/wonka/rrd/snmp/\">\n" + 
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

    private String m_brocadeXmlFragment = 
    "       <resourceType name=\"brocadeIndex\" label=\"Brocade Switches\">\n" +
    "         <persistenceSelectorStrategy class=\"foo\"/>\n" +
    "         <storageStrategy class=\"foo\"/>\n" +
    "       </resourceType>\n";

    @Test
    public void testSetInstance() throws MarshalException, ValidationException, IOException {
        initDataCollectionFactory(m_xml);
        assertEquals("/wonka/rrd/snmp", DataCollectionConfigFactory.getInstance().getRrdPath());
    }
    
    @Test
    public void testValidResourceType() throws MarshalException, ValidationException, IOException {
    	String modifiedXml = m_xml.replaceFirst("ifIndex", "brocadeIndex").replaceFirst("<groups", m_brocadeXmlFragment + "<groups");
        initDataCollectionFactory(modifiedXml);
    }
    
    @Test
    public void testInvalidResourceType() throws MarshalException, ValidationException, IOException {
        String modifiedXml = m_xml.replaceFirst("ifIndex", "brocadeIndex");
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("instance 'brocadeIndex' invalid in mibObj definition for OID '.1.3.6.1.2.1.2.2.1.10' in collection 'default' for group 'mib2-interfaces'.  Allowable instance values: any positive number, 'ifIndex', or any of the configured resourceTypes: (none)"));

        try {
            initDataCollectionFactory(modifiedXml);
            DataCollectionConfigFactory.getInstance().getConfiguredResourceTypes();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    private void initDataCollectionFactory(String xmlConfig) {
        DefaultDataCollectionConfigDao dataCollectionDao = new DefaultDataCollectionConfigDao();
        dataCollectionDao.setConfigResource(new ByteArrayResource(xmlConfig.getBytes()));
        dataCollectionDao.afterPropertiesSet();
        DataCollectionConfigFactory.setInstance(dataCollectionDao);
    }

}
