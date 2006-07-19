//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.capsd;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.StringReader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.CapsdConfigFactory;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.mock.MockPollerConfig;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.poller.Poller;
import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.rrd.RrdUtils;

public class CapsdTest extends OpenNMSTestCase {
    
    private Capsd m_capsd;
    
    private static final String CAPSD_CONFIG = "<?xml version=\"1.0\"?>\n" + 
            "<!-- 24 hours -->\n" + 
            "<capsd-configuration \n" + 
            "   rescan-frequency=\"86400000\" \n" + 
            "   initial-sleep-time=\"300000\" \n" + 
            "   management-policy=\"managed\" \n" + 
            "   max-suspect-thread-pool-size=\"6\"\n" + 
            "    max-rescan-thread-pool-size=\"3\" \n" + 
            "    abort-protocol-scans-if-no-route=\"false\" \n" + 
            "    delete-propagation-enabled=\"true\" \n" + 
            "    xmlrpc=\"false\">\n" + 
            "\n" + 
            "    <protocol-plugin protocol=\"MOCK\" class-name=\"org.opennms.netmgt.capsd.plugins.LdapPlugin\" scan=\"on\" user-defined=\"false\">\n" + 
            "        <property key=\"timeout\" value=\"2000\" />\n" + 
            "        <property key=\"retry\" value=\"2\" />\n" + 
            "    </protocol-plugin>\n"+
            "    <smb-config>\n" + 
            "        <smb-auth user=\"guest\" password=\"guest\" type=\"domain\">WORKGROUP</smb-auth>\n" + 
            "    </smb-config>\n" + 
            "    <ip-management policy=\"managed\">\n" + 
            "        <range begin=\"192.168.0.0\" end=\"192.168.0.255\" />\n" + 
            "    </ip-management>\n" + 
            "    <ip-management policy=\"unmanaged\">\n" + 
            "        <specific>0.0.0.0</specific>\n" + 
            "        <range begin=\"127.0.0.0\" end=\"127.255.255.255\" />\n" + 
            "    </ip-management>\n" + 
            "</capsd-configuration>\n" + 
            ""; 
    
    private static final String SVR_CONFIG = "<local-server server-name=\"nms1\" verify-server=\"false\">\n" + 
            "</local-server>\n" + 
            "";
    
    private static final String POLLER_CONFIG = "<?xml version=\"1.0\"?>\n" + 
            "<?castor class-name=\"org.opennms.netmgt.poller.PollerConfiguration\"?>\n" + 
            "<poller-configuration threads=\"30\" \n" + 
            "        serviceUnresponsiveEnabled=\"false\"\n" + 
            "        nextOutageId=\"SELECT nextval(\'outageNxtId\')\"\n" + 
            "        xmlrpc=\"false\">\n" + 
            "   <node-outage status=\"on\" \n" + 
            "                pollAllIfNoCriticalServiceDefined=\"true\">\n" + 
            "       <critical-service name=\"ICMP\"/>\n" + 
            "   </node-outage>\n" + 
            "   <package name=\"example1\">\n" + 
            "       <filter>IPADDR IPLIKE *.*.*.*</filter>\n" + 
            "       <specific>0.0.0.0</specific>\n" + 
            "       <include-range begin=\"1.1.1.1\" end=\"254.254.254.254\"/>\n" + 
            "                <rrd step = \"300\">\n" + 
            "                        <rra>RRA:AVERAGE:0.5:1:2016</rra>\n" + 
            "                        <rra>RRA:AVERAGE:0.5:12:4464</rra>\n" + 
            "                        <rra>RRA:MIN:0.5:12:4464</rra>\n" + 
            "                        <rra>RRA:MAX:0.5:12:4464</rra>\n" + 
            "                </rrd>\n" + 
            "       <service name=\"MOCK\" interval=\"300000\" user-defined=\"false\" status=\"on\">\n" + 
            "           <parameter key=\"retry\" value=\"2\"/>\n" + 
            "           <parameter key=\"timeout\" value=\"3000\"/>\n" + 
            "           <parameter key=\"ds-name\" value=\"icmp\"/>\n" + 
            "       </service>\n" + 
            "       <outage-calendar>zzz from poll-outages.xml zzz</outage-calendar>\n" + 
            "\n" + 
            "       <downtime interval=\"30000\" begin=\"0\" end=\"300000\"/>       <!-- 30s, 0, 5m -->\n" + 
            "       <downtime interval=\"300000\" begin=\"300000\" end=\"43200000\"/>   <!-- 5m, 5m, 12h -->\n" + 
            "       <downtime interval=\"600000\" begin=\"43200000\" end=\"432000000\"/>    <!-- 10m, 12h, 5d -->\n" + 
            "       <downtime begin=\"432000000\" delete=\"true\"/>         <!-- anything after 5 days delete -->\n" + 
            "\n" + 
            "   </package>\n" + 
            "\n" + 
            "   <monitor service=\"MOCK\"   class-name=\"org.opennms.netmgt.poller.monitors.LdapMonitor\"/>\n" + 
            "</poller-configuration>\n" + 
            "\n" + 
            "";
    
    private final static String DB_SCHEMA_CONFIG = "<?xml version=\"1.0\"?>\n" + 
            "<database-schema>\n" + 
            "   <table name=\"distPoller\" visable=\"false\">\n" + 
            "       <column name=\"dpNumber\"/>\n" + 
            "       <column name=\"dpName\"/>\n" + 
            "       <column name=\"dpIP\"/>\n" + 
            "       <column name=\"dpComment\"/>\n" + 
            "       <column name=\"dpDiscLimit\"/>\n" + 
            "       <column name=\"dpAdminState\"/>\n" + 
            "       <column name=\"dpRunState\"/>\n" + 
            "   </table>\n" + 
            "\n" + 
            "   <table name=\"node\"> \n" + 
            "       <join column=\"nodeID\" table=\"ipInterface\" table-column=\"nodeID\"/>\n" + 
            "       <column name=\"nodeID\"/>\n" + 
            "       <column name=\"dpName\" visable=\"false\"/>\n" + 
            "       <column name=\"nodeCreateTime\"/>\n" + 
            "       <column name=\"nodeParentID\"/>\n" + 
            "       <column name=\"nodeType\"/>\n" + 
            "       <column name=\"nodeSysOID\"/>\n" + 
            "       <column name=\"nodeSysName\"/>\n" + 
            "       <column name=\"nodeSysDescription\"/>\n" + 
            "       <column name=\"nodeSysLocation\"/>\n" + 
            "       <column name=\"nodeSysContact\"/>\n" + 
            "       <column name=\"nodeLabel\"/>\n" + 
            "   </table>\n" + 
            "\n" + 
            "   <table name=\"ipInterface\" key=\"primary\">\n" + 
            "       <column name=\"nodeID\" visable=\"false\"/>\n" + 
            "       <column name=\"ipAddr\"/>\n" + 
            "       <column name=\"ipHostname\"/>\n" + 
            "       <column name=\"isManaged\"/>\n" + 
            "       <column name=\"ipStatus\"/>\n" + 
            "       <column name=\"ipLastCapsdPoll\"/>\n" + 
            "   </table>\n" + 
            "\n" + 
            "   <table name=\"snmpInterface\">\n" + 
            "       <join column=\"ipAddr\" table=\"ipInterface\" table-column=\"ipAddr\"/>\n" + 
            "       <column name=\"nodeID\" visable=\"false\"/>\n" + 
            "       <column name=\"ipAddr\" visable=\"false\"/>\n" + 
            "       <column name=\"snmpIpAdEntNetMask\"/>\n" + 
            "       <column name=\"snmpPhysAddr\"/>\n" + 
            "       <column name=\"snmpIfIndex\"/>\n" + 
            "       <column name=\"snmpIfDescr\"/>\n" + 
            "       <column name=\"snmpIfType\"/>\n" + 
            "       <column name=\"snmpIfSpeed\"/>\n" + 
            "       <column name=\"snmpIfAdminStatus\"/>\n" + 
            "       <column name=\"snmpIfOperStatus\"/>\n" + 
            "   </table>\n" + 
            "   \n" + 
            "   <table name=\"service\">\n" + 
            "       <join column=\"serviceID\" table=\"ifServices\" table-column=\"serviceID\" />\n" + 
            "       <column name=\"serviceID\" visable=\"false\" />\n" + 
            "       <column name=\"serviceName\"/>\n" + 
            "   </table>\n" + 
            "\n" + 
            "   <table name=\"ifServices\">\n" + 
            "       <join column=\"ipAddr\" table=\"ipInterface\" table-column=\"ipAddr\"/>\n" + 
            "       <column name=\"ipAddr\" visable=\"false\"/>\n" + 
            "       <column name=\"serviceID\"/>\n" + 
            "       <column name=\"lastGood\"/>\n" + 
            "       <column name=\"lastFail\"/>\n" + 
            "   </table>\n" + 
            "   <table name=\"serverMap\">\n" + 
            "           <join column=\"ipAddr\" table=\"ipInterface\" table-column=\"ipAddr\"/>\n" + 
            "           <column name=\"ipAddr\" visable=\"false\"/>\n" + 
            "           <column name=\"serverName\"/>\n" + 
            "   </table>\n" + 
            "   <table name=\"serviceMap\">\n" + 
            "           <join column=\"ipAddr\" table=\"ipInterface\" table-column=\"ipAddr\"/>\n" + 
            "           <column name=\"ipAddr\" visable=\"false\"/>\n" + 
            "           <column name=\"serviceMapName\"/>\n" + 
            "   </table>\n" + 
            "   <table name=\"assets\">\n" + 
            "       <join column=\"nodeID\" table=\"ipInterface\" table-column=\"nodeID\"/>\n" + 
            "       <column name=\"nodeID\" visable=\"false\"/>\n" + 
            "       <column name=\"displayCategory\"/>\n" + 
            "       <column name=\"notifyCategory\"/>\n" + 
            "       <column name=\"pollerCategory\"/>\n" + 
            "       <column name=\"thresholdCategory\"/>\n" + 
            "   </table>\n" + 
            "\n" + 
            "\n" + 
            "</database-schema>\n" + 
            "\n" + 
            "";

    private static final String DATACOLLECTION_CONFIG =
"<?xml version=\"1.0\"?>\n" +
"<datacollection-config\n" +
"  rrdRepository = \"/tmp\">\n" +
"  <snmp-collection name=\"default\"\n" +
"    maxVarsPerPdu = \"10\"\n" +
"    snmpStorageFlag = \"select\">\n" +
"    <rrd step = \"300\">\n" +
"      <rra>RRA:AVERAGE:0.5:1:8928</rra>\n" +
"      <rra>RRA:AVERAGE:0.5:12:8784</rra>\n" +
"      <rra>RRA:MIN:0.5:12:8784</rra>\n" +
"      <rra>RRA:MAX:0.5:12:8784</rra>\n" +
"    </rrd>\n" +
"    <groups>\n" +
"      <!-- data from standard (mib-2) sources -->\n" +
"      <group  name = \"mib2-interfaces\" ifType = \"all\">\n" + 
"        <mibObj oid=\".1.3.6.1.2.1.2.2.1.10\" instance=\"ifIndex\"\n" +
"          alias=\"ifInOctets\" type=\"counter\"/>\n" +
"      </group>\n" +
"    </groups>\n" +
"    <systems>\n" +
"      <systemDef name = \"Enterprise\">\n" +
"        <sysoidMask>.1.3.6.1.4.1.</sysoidMask>\n" +
"        <collect>\n" +
"          <includeGroup>mib2-interfaces</includeGroup>\n" +
"        </collect>\n" +
"      </systemDef>\n" +
"    </systems>\n" +
"  </snmp-collection>\n" +
"</datacollection-config>\n";

    private static final String RRD_CONFIG = "org.opennms.rrd.strategyClass=org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy";
    
    private static final String COLLECTD_CONFIG = "<?xml version=\"1.0\"?>\n" + 
            "<?castor class-name=\"org.opennms.netmgt.collectd.CollectdConfiguration\"?>\n" + 
            "<collectd-configuration \n" + 
            "   threads=\"50\">\n" + 
            "   \n" + 
            "   <package name=\"example1\">\n" + 
            "       <filter>IPADDR IPLIKE *.*.*.*</filter>   \n" + 
            "       <specific>0.0.0.0</specific>\n" + 
            "       <include-range begin=\"1.1.1.1\" end=\"254.254.254.254\"/>\n" + 
            "       \n" + 
            "       <service name=\"SNMP\" interval=\"300000\" user-defined=\"false\" status=\"on\">\n" + 
            "           <parameter key=\"collection\" value=\"default\"/>\n" + 
            "           <parameter key=\"port\" value=\"161\"/>\n" + 
            "           <parameter key=\"retry\" value=\"3\"/>\n" + 
            "           <parameter key=\"timeout\" value=\"3000\"/>\n" + 
            "       </service>\n" + 
            "       \n" + 
            "       <outage-calendar>zzz from poll-outages.xml zzz</outage-calendar>\n" + 
            "   </package>\n" + 
            "   \n" + 
            "   <collector service=\"SNMP\"     class-name=\"org.opennms.netmgt.collectd.SnmpCollector\"/>\n" + 
            "</collectd-configuration>\n" + 
            "\n" + 
            "";

    private MockPollerConfig m_pollerConfig;

    private Poller m_poller;

    private PollerConfigFactory m_pollerConfig2;

    protected void setUp() throws Exception {
        super.setUp();
        m_capsd = Capsd.getInstance();
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(new StringReader(DB_SCHEMA_CONFIG)));
        CapsdConfigFactory capsdConfigFactory = new CapsdConfigFactory(new StringReader(CAPSD_CONFIG));
        capsdConfigFactory.setNextSvcIdSql(m_db.getNextServiceIdStatement());
        CapsdConfigFactory.setInstance(capsdConfigFactory);
        
        OpennmsServerConfigFactory onmsSvrConfig = new OpennmsServerConfigFactory(new StringReader(SVR_CONFIG));
        OpennmsServerConfigFactory.setInstance(onmsSvrConfig);
        PollerConfigFactory.setInstance(new PollerConfigFactory(System.currentTimeMillis(), new StringReader(POLLER_CONFIG), onmsSvrConfig.getServerName(), onmsSvrConfig.verifyServer()));

	RrdConfig.loadProperties(new ByteArrayInputStream(RRD_CONFIG.getBytes()));
	// This isn't needed here, but it makes error pop up earlier
	RrdUtils.initialize();

	DataCollectionConfigFactory.setInstance(new DataCollectionConfigFactory(new StringReader(DATACOLLECTION_CONFIG)));

        CollectdConfigFactory.setInstance(new CollectdConfigFactory(new StringReader(COLLECTD_CONFIG), onmsSvrConfig.getServerName(), onmsSvrConfig.verifyServer()));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

/*    public final void testPause() {
        //TODO Implement pause().
    }

    public final void testResume() {
        //TODO Implement resume().
    }

    public final void testStart() {
        m_capsd.start();
    }

    public final void testStop() {
        //TODO Implement stop().
    }
*/
    public final void testStartStop() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.stop();
    }
    

/*    public final void testGetLocalHostAddress() {
        //TODO Implement getLocalHostAddress().
    }

    public final void testScanSuspectInterface() {
        //TODO Implement scanSuspectInterface().
    }

    public final void testRescanInterfaceParent() {
        //TODO Implement rescanInterfaceParent().
    }

    public final void testSetStatus() {
        //TODO Implement setStatus().
    }

    public final void testGetStatus() {
        //TODO Implement getStatus().
    }

    public final void testIsStartPending() {
        //TODO Implement isStartPending().
    }

    public final void testIsRunning() {
        //TODO Implement isRunning().
    }

    public final void testIsPaused() {
        //TODO Implement isPaused().
    }

    public final void testIsStarting() {
        //TODO Implement isStarting().
    }
*/
}
