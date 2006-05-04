package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

public class DataCollectionConfigFactoryTest extends TestCase {

    private String m_xml = "<?xml version=\"1.0\"?>\n" + 
            "<datacollection-config\n" + 
            "   rrdRepository = \"/wonka/rrd/snmp/\">\n" + 
            "   <snmp-collection name=\"default\"\n" + 
            "       maxVarsPerPdu = \"10\"\n" + 
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
    
    public void testSetInstance() throws MarshalException, ValidationException, IOException {
        DataCollectionConfigFactory.setInstance(new DataCollectionConfigFactory(new StringReader(m_xml)));
        DataCollectionConfigFactory.init();
        assertEquals(10, DataCollectionConfigFactory.getInstance().getMaxVarsPerPdu("default"));
        assertEquals("/wonka/rrd/snmp", DataCollectionConfigFactory.getInstance().getRrdPath());
    }

//    public void testInit() {
//        fail("Not yet implemented");
//    }
//
//    public void testReload() {
//        fail("Not yet implemented");
//    }

}
