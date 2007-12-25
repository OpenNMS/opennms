//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
//
//$Id: EventConfDataTest.java 5474 2007-01-16 17:36:47Z brozow $
//

package org.opennms.netmgt.eventd.datablock;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.Base64;
import org.opennms.netmgt.eventd.EventConfigurationManager;
import org.opennms.netmgt.mock.EventConfWrapper;
import org.opennms.netmgt.mock.EventWrapper;
import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.test.mock.MockLogAppender;

public class EventConfDaoTest extends TestCase {

    private EventConfDao m_eventConfDao;
    private static final String EVENT_CONF = "<events xmlns=\"http://xmlns.opennms.org/xsd/eventconf\">\n" +
    " <global>\n" +
    "  <security>\n" +
    "   <doNotOverride>logmsg</doNotOverride>\n" +
    "   <doNotOverride>operaction</doNotOverride>\n" +
    "   <doNotOverride>autoaction</doNotOverride>\n" +
    "   <doNotOverride>tticket</doNotOverride>\n" +
    "   <doNotOverride>script</doNotOverride>\n" +
    "  </security>\n" +
    " </global>\n" +
    "\n" +
    " <event>\n" +
    "  <mask>\n" +
    "   <maskelement>\n" +
    "    <mename>id</mename>\n" +
    "    <mevalue>.1.3.6.1.2.1.15.7</mevalue>\n" +
    "   </maskelement>\n" +
    "   <maskelement>\n" +
    "    <mename>generic</mename>\n" +
    "    <mevalue>6</mevalue>\n" +
    "   </maskelement>\n" +
    "   <maskelement>\n" +
    "    <mename>specific</mename>\n" +
    "    <mevalue>1</mevalue>\n" +
    "   </maskelement>\n" +
    "   <maskelement>\n" +
    "    <mename>snmphost</mename>\n" +
    "    <mevalue>192.168.1.%</mevalue>\n" +
    "   </maskelement>\n" +
    "  </mask>\n" +
    "  <uei>uei.opennms.org/IETF/BGP/traps/bgpEstablished</uei>\n" +
    "  <event-label>BGP4-MIB defined trap event: bgpEstablished</event-label>\n" +
    "  <descr>&lt;p&gt;The BGP Established event is generated when\n" +
    "   the BGP FSM enters the ESTABLISHED state.&lt;/p&gt;&lt;table&gt;\n" +
    "   &lt;tr&gt;&lt;td&gt;&lt;b&gt;\n" +
    "   bgpPeerLastError&lt;/b&gt;&lt;/td&gt;&lt;td&gt;%parm[#1]%\n" +
    "   &lt;/td&gt;&lt;td&gt;&lt;p;&gt;&lt;/p&gt;&lt;/td;&gt;&lt;/tr&gt;&lt;tr&gt;&lt;td&gt;&lt;b&gt;\n" +
    "   bgpPeerState&lt;/b&gt;&lt;/td&gt;&lt;td&gt;%parm[#2]%\n" +
    "   &lt;/td&gt;&lt;td&gt;&lt;p;&gt;\n" +
    "   idle(1) connect(2) active(3) opensent(4) openconfirm(5) established(6)&lt;/p&gt;\n" +
    "   &lt;/td;&gt;&lt;/tr&gt;&lt;/table&gt;\n" +
    "  </descr>\n" +
    "  <logmsg dest='discardtraps'>&lt;p&gt;BGP Event: FSM entered connected state.&lt;/p&gt;</logmsg>\n" +
    "  <severity>Normal</severity>\n" +
    " </event>\n" +
    "\n" +
    " <event>\n" +
    "  <mask>\n" +
    "   <maskelement>\n" +
    "    <mename>id</mename>\n" +
    "    <mevalue>.1.3.6.1.2.1.15.7</mevalue>\n" +
    "   </maskelement>\n" +
    "   <maskelement>\n" +
    "    <mename>generic</mename>\n" +
    "    <mevalue>6</mevalue>\n" +
    "   </maskelement>\n" +
    "   <maskelement>\n" +
    "    <mename>specific</mename>\n" +
    "    <mevalue>1</mevalue>\n" +
    "   </maskelement>\n" +
    "  </mask>\n" +
    "  <uei>uei.opennms.org/IETF/BGP/traps/bgpEstablished</uei>\n" +
    "  <event-label>BGP4-MIB defined trap event: bgpEstablished</event-label>\n" +
    "  <descr>&lt;p&gt;The BGP Established event is generated when\n" +
    "   the BGP FSM enters the ESTABLISHED state.&lt;/p&gt;&lt;table&gt;\n" +
    "   &lt;tr&gt;&lt;td&gt;&lt;b&gt;\n" +
    "   bgpPeerLastError&lt;/b&gt;&lt;/td&gt;&lt;td&gt;%parm[#1]%\n" +
    "   &lt;/td&gt;&lt;td&gt;&lt;p;&gt;&lt;/p&gt;&lt;/td;&gt;&lt;/tr&gt;&lt;tr&gt;&lt;td&gt;&lt;b&gt;\n" +
    "   bgpPeerState&lt;/b&gt;&lt;/td&gt;&lt;td&gt;%parm[#2]%\n" +
    "   &lt;/td&gt;&lt;td&gt;&lt;p;&gt;\n" +
    "   idle(1) connect(2) active(3) opensent(4) openconfirm(5) established(6)&lt;/p&gt;\n" +
    "   &lt;/td;&gt;&lt;/tr&gt;&lt;/table&gt;\n" +
    "  </descr>\n" +
    "  <logmsg dest='logndisplay'>&lt;p&gt;BGP Event: FSM entered connected state.&lt;/p&gt;</logmsg>\n" +
    "  <severity>Normal</severity>\n" +
    " </event>\n" +
    "\n" +
    " <event>\n" +
    "  <mask>\n" +
    "   <maskelement>\n" +
    "    <mename>id</mename>\n" +
    "    <mevalue>.1.3.6.1.2.1.15.7</mevalue>\n" +
    "   </maskelement>\n" +
    "   <maskelement>\n" +
    "    <mename>generic</mename>\n" +
    "    <mevalue>6</mevalue>\n" +
    "   </maskelement>\n" +
    "   <maskelement>\n" +
    "    <mename>specific</mename>\n" +
    "    <mevalue>2</mevalue>\n" +
    "   </maskelement>\n" +
    "  </mask>\n" +
    "  <uei>uei.opennms.org/IETF/BGP/traps/bgpBackwardTransition</uei>\n" +
    "  <event-label>BGP4-MIB defined trap event: bgpBackwardTransition</event-label>\n" +
    "  <descr>&lt;p&gt;The BGPBackwardTransition Event is generated\n" +
    "   when the BGP FSM moves from a higher numbered\n" +
    "   state to a lower numbered state.&lt;/p&gt;&lt;table&gt;\n" +
    "   &lt;tr&gt;&lt;td&gt;&lt;b&gt;\n" +
    "   bgpPeerLastError&lt;/b&gt;&lt;/td&gt;&lt;td&gt;%parm[#1]%\n" + 
    "   &lt;/td&gt;&lt;td&gt;&lt;p;&gt;&lt;/p&gt;&lt;/td;&gt;&lt;/tr&gt;&lt;tr&gt;&lt;td&gt;&lt;b&gt;\n" +
    "   bgpPeerState&lt;/b&gt;&lt;/td&gt;&lt;td&gt;%parm[#2]%\n" + 
    "   &lt;/td&gt;&lt;td&gt;&lt;p;&gt;\n" + 
    "   idle(1) connect(2) active(3) opensent(4) openconfirm(5) established(6)&lt;/p&gt;\n" +
    "   &lt;/td;&gt;&lt;/tr&gt;&lt;/table&gt;\n" +
    "  </descr>\n" + 
    "  <logmsg dest='discardtraps'>&lt;p&gt;BGP Event: FSM Backward Transistion.&lt;/p&gt;</logmsg>\n" + 
    "  <severity>Warning</severity>\n" +
    " </event>\n" +
    "\n" +
    " <event>\n" +
    "  <mask>\n" +
    "   <maskelement>\n" +
    "    <mename>generic</mename>\n" +
    "    <mevalue>0</mevalue>\n" +
    "   </maskelement>\n" +
    "   <maskelement>\n" +
    "    <mename>snmphost</mename>\n" +
    "    <mevalue>192.168.1.1</mevalue>\n" +
    "   </maskelement>\n" +
    "  </mask>\n" +
    "  <uei>uei.opennms.org/generic/traps/SNMP_Cold_Start</uei>\n" +
    "  <event-label>OpenNMS-defined trap event: SNMP_Cold_Start</event-label>\n" +
    "  <descr>\n" +
    "	&lt;p&gt;A coldStart trap signifies that the sending\n" +
    "	protocol entity is reinitializing itself such that the\n" +
    "	agent's configuration or the protocol entity implementation\n" +
    "	may be altered.&lt;/p&gt;\n" +
    "  </descr>\n" +
    "  <logmsg dest='discardtraps'>\n" +
    "	Agent Up with Possible Changes (coldStart Trap)\n" +
    "	enterprise:%id% (%id%) args(%parm[##]%):%parm[all]%\n" +
    "  </logmsg>\n" +
    "  <severity>Normal</severity>\n" +
    " </event>\n" +
    "\n" +
    " <event>\n" +
    "  <mask>\n" +
    "   <maskelement>\n" +
    "    <mename>generic</mename>\n" +
    "    <mevalue>0</mevalue>\n" +
    "   </maskelement>\n" +
    "  </mask>\n" +
    "  <uei>uei.opennms.org/generic/traps/SNMP_Cold_Start</uei>\n" +
    "  <event-label>OpenNMS-defined trap event: SNMP_Cold_Start</event-label>\n" +
    "  <descr>\n" +
    "	&lt;p&gt;A coldStart trap signifies that the sending\n" +
    "	protocol entity is reinitializing itself such that the\n" +
    "	agent's configuration or the protocol entity implementation\n" +
    "	may be altered.&lt;/p&gt;\n" +
    "  </descr>\n" +
    "  <logmsg dest='logndisplay'>\n" +
    "	Agent Up with Possible Changes (coldStart Trap)\n" +
    "	enterprise:%id% (%id%) args(%parm[##]%):%parm[all]%\n" +
    "  </logmsg>\n" +
    "  <severity>Normal</severity>\n" +
    " </event>\n" +
    " <event>\n" +
    "  <mask>\n" +
    "   <maskelement>\n" +
    "    <mename>generic</mename>\n" +
    "    <mevalue>1</mevalue>\n" +
    "   </maskelement>\n" +
    "  </mask>\n" +
    "  <uei>uei.opennms.org/generic/traps/SNMP_Warm_Start</uei>\n" +
    "  <event-label>OpenNMS-defined trap event: SNMP_Warm_Start</event-label>\n" +
    "  <descr>\n" +
    "	&lt;p&gt;A warmStart trap signifies that the sending\n" +
    "	protocol entity is reinitializing itself such that the\n" +
    "	agent's configuration or the protocol entity implementation\n" +
    "	may be altered.&lt;/p&gt;\n" +
    "  </descr>\n" +
    "  <logmsg dest='logndisplay'>\n" +
    "	Agent Up with Possible Changes (warmStart Trap)\n" +
    "	enterprise:%id% (%id%) args(%parm[##]%):%parm[all]%\n" +
    "  </logmsg>\n" +
    "  <severity>Normal</severity>\n" +
    " </event>\n" +
    " <event>\n" +
    "  <mask>\n" +
    "   <maskelement>\n" +
    "    <mename>id</mename>\n" +
    "    <mevalue>.1.3.6.1.4.1.14179.2.6.3</mevalue>\n" +
    "   </maskelement>\n" +
    "   <maskelement>\n" +
    "    <mename>generic</mename>\n" +
    "    <mevalue>6</mevalue>\n" +
    "   </maskelement>\n" +
    "   <maskelement>\n" +
    "    <mename>specific</mename>\n" +
    "    <mevalue>8</mevalue>\n" +
    "   </maskelement>\n" +
    "  </mask>\n" +
    "  <uei>uei.opennms.org/vendor/cisco/bsnAPDisassociated</uei>\n" +
    "  <event-label>AIRESPACE-WIRELESS-MIB defined trap event: bsnAPDisassociated</event-label>\n" +
    "  <descr>\n" +
    "   &lt;p&gt;When Airespace AP disassociates from Airespace Switch, AP disassociated notification\n" +
    "   will be sent with dot3 MAC address of Airespace AP\n" +
    "   management system to remove Airespace AP from this Airespace Switch&lt;/p&gt;&lt;table&gt;\n" +
    "   &lt;tr&gt;&lt;td&gt;&lt;b&gt;\n\n" +
    "   bsnAPMacAddrTrapVariable&lt;/b&gt;&lt;/td&gt;&lt;td&gt;\n" +
    "   %parm[#1]%;&lt;/td&gt;&lt;td&gt;&lt;p;&gt;&lt;/p&gt;&lt;/td;&gt;&lt;/tr&gt;&lt;/table&gt;\n" +
    "  </descr>\n" +
    "  <logmsg dest='logndisplay'>&lt;p&gt;\n" +
    "    bsnAPDisassociated trap received\n" +
    "    bsnAPMacAddrTrapVariable=%parm[#1]%&lt;/p&gt;\n" +
    "  </logmsg>\n" +
    "  <severity>Warning</severity>\n" +
    " </event>\n" +
    "</events>";

    protected void setUp() throws Exception {
        MockLogAppender.setupLogging(false);

        File targetDir = new File("target");
        File configFile = File.createTempFile("eventConf", "xml", targetDir);
        configFile.deleteOnExit();

        FileUtils.writeStringToFile(configFile, EVENT_CONF, "ISO_8859-1");

        DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
        eventConfDao.setConfigFile(configFile);
        eventConfDao.afterPropertiesSet();

        m_eventConfDao = eventConfDao;

    }


    public void finishUp() {
    }

    public void tearDown() {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    public void testEventValuePassesMaskValue() {
        List<String> maskList = new ArrayList<String>(1);
        String eventValue = "George Clinton, father of funk";
        maskList.add(0, "~^Bill.*Clinton.*funk$");
        EventConfData ecd = new EventConfData();
        assertFalse(ecd.eventValuePassesMaskValue(eventValue, maskList));
        maskList.set(0, "~^George.*Clinton.*funk$");
        assertTrue(ecd.eventValuePassesMaskValue(eventValue, maskList));
        eventValue = "Is FoxTel On Air";
        maskList.set(0, "~.*Fox[Tt]el.*");
        assertTrue(ecd.eventValuePassesMaskValue(eventValue, maskList));
    }


    public void testV1TrapNewSuspect() throws Exception {
        verifyMatchingEventConf(null, "v1", null, 6, 1);
    }

    public void testV2TrapNewSuspect() throws Exception {
        verifyMatchingEventConf(null, "v2c", null, 6, 1);
    }

    public void testV1EnterpriseIdAndGenericMatch() throws Exception {
        verifyMatchingEventConf("uei.opennms.org/IETF/BGP/traps/bgpEstablished", "v1",
                ".1.3.6.1.2.1.15.7", 6, 1);
    }

    public void testV2EnterpriseIdAndGenericAndSpecificMatch() throws Exception {
        verifyMatchingEventConf("uei.opennms.org/IETF/BGP/traps/bgpEstablished", "v2c",
                ".1.3.6.1.2.1.15.7", 6, 1);
    }


    public void testV2EnterpriseIdAndGenericAndSpecificMissWithExtraZeros() throws Exception {
        verifyMatchingEventConf(null, "v2c",
                ".1.3.6.1.2.1.15.7.0.0", 6, 1);
    }

    public void testV1EnterpriseIdAndGenericAndSpecificMissWithWrongGeneric() throws Exception {
        verifyMatchingEventConf(null, "v1",
                ".1.3.6.1.2.1.15.7", 5, 1);
    }

    public void testV1EnterpriseIdAndGenericAndSpecificMissWithWrongSpecific() throws Exception {
        verifyMatchingEventConf(null, "v1",
                ".1.3.6.1.2.1.15.7", 6, 50);
    }

    public void testV1GenericMatchColdStart() throws Exception {
        verifyMatchingEventConf("uei.opennms.org/generic/traps/SNMP_Cold_Start",
                "v1", null, 0, 0);
    }

    public void testV1GenericMatchWarmStart() throws Exception {
        verifyMatchingEventConf("uei.opennms.org/generic/traps/SNMP_Warm_Start",
                "v1", null, 1, 0);
    }

    public void testV2GenericMatch() throws Exception {
        verifyMatchingEventConf("uei.opennms.org/generic/traps/SNMP_Cold_Start",
                "v2c", ".1.3.6.1.6.3.1.1.5.1", 0, 0);
    }

    public void testV1TrapDroppedEvent() throws Exception {
        verifyMatchingEventConf(null, "v1", ".1.3.6.1.2.1.15.7", 6, 2);
    }

    public void testV2TrapDroppedEvent() throws Exception {
        verifyMatchingEventConf(null, "v2c", ".1.3.6.1.2.1.15.7", 6, 2);
    }

    public void testV1TrapDefaultEvent() throws Exception {
        verifyMatchingEventConf(null, "v1", null, 6, 1);
    }

    public void testV2TrapDefaultEvent() throws Exception {
        verifyMatchingEventConf(null, "v2c", null, 6, 1);
    }

    public void testV1TrapDroppedIPEvent() throws Exception {
        anticipateAndSend(null, "v1", null, 0, 0, "192.168.1.1");
    }

    public void testV1TrapNotDroppedIPOffEvent() throws Exception {
        anticipateAndSend("uei.opennms.org/generic/traps/SNMP_Cold_Start",
                "v1", null, 0, 0, "192.168.1.2");
    }

    public void testV1TrapDroppedNetwork1Event() throws Exception {
        anticipateAndSend(null, "v1", ".1.3.6.1.2.1.15.7", 6, 1, "192.168.1.1");
    }

    public void testV1TrapDroppedNetwork2Event() throws Exception {
        anticipateAndSend(null,
                "v1", ".1.3.6.1.2.1.15.7", 6, 1, "192.168.1.2");
    }

    public void testV1TrapNotDroppedNetworkOffEvent() throws Exception {
        anticipateAndSend("uei.opennms.org/IETF/BGP/traps/bgpEstablished",
                "v1", ".1.3.6.1.2.1.15.7", 6, 1, "192.168.2.1");
    }

    // FIXME
    public void testV1EnterpriseIdAndGenericAndSpecificAndMatchWithVarbindsAndTC()
    throws Exception {

        /*
         * This tests fails because the varbind in this example needs to have a
         * SNMP Textual Convention applied to correctly turn the octects from the
         * varbind into a display string formatted as a macaddress of the form
         * xx:xx:xx:xx:xx:xx per the DISPLAY-HINT in the mib. There are two problems
         * with the current method for doing this:
         * 1.  The resulting octet value is decoded from Base64 and placed into a 
         *     String which consequently get decodec to unicode characters and loses
         *     info.
         * 2.  The DISPLAY-HINT  information that is encoded in the event conf file is 
         *     lost due to the data and code structures of the matching code in 
         *     EventConfData.getEvent() this needs to be enhanced to use a 'wrapped' object
         *     style to allow the matching and decoding to remain close to the data
         *     that can be used to do the the decoding properly
         */

        LinkedHashMap<String, String> varbinds = new LinkedHashMap <String, String>();
        byte[] macAddr = new byte[]{(byte)0x00,(byte)0x14,(byte)0xf1,(byte)0xad,(byte)0xa7,(byte)0x50};

        String encoded = new String(Base64.encodeBase64(macAddr));

        varbinds.put(".1.3.6.1.4.1.14179.2.6.2.20.0", encoded);

        verifyMatchingEventConf("uei.opennms.org/vendor/cisco/bsnAPNoiseProfileUpdatedToPass",
                "v1", ".1.3.6.1.4.1.14179.2.6.3", 6, 38, "192.168.2.1", varbinds);
    }

    public Event createEvent(String version, String enterprise, int generic, int specific, String snmpHost) {
        return createEvent(version, enterprise, generic, specific, snmpHost, new LinkedHashMap<String, String>());
    }

    public Event createEvent(String version, String enterprise, int generic, int specific) {
        return createEvent(version, enterprise, generic, specific, "127.0.0.1");
    }

    private Event createEvent(String version, String enterprise, int generic, int specific, String snmpHost, LinkedHashMap<String, String> varbinds) {
        EventBuilder builder = new EventBuilder("myUei", "Test")
        .setInterface("127.0.0.1")
        .setNodeid(0)
        .setSnmpVersion(version)
        .setEnterpriseId(enterprise)
        .setGeneric(generic)
        .setSpecific(specific)
        .setSnmpHost(snmpHost);

        for(Map.Entry<String, String> entry : varbinds.entrySet()) {
            builder.addParam(entry.getKey(), entry.getValue());
        }

        return builder.getEvent();
    }


    private void verifyMatchingEventConf(String expecteduei, String version, String enterprise, int generic, int specific, String snmphost, LinkedHashMap<String, String> varbinds) {
        verifyMatchingEventConf(expecteduei, createEvent(version, enterprise, generic, specific, snmphost, varbinds));
    }

    public void verifyMatchingEventConf(String expectedUei,
            String version, String enterprise, int generic, int specific) throws Exception {
        verifyMatchingEventConf(expectedUei, createEvent(version, enterprise, generic, specific));
    }

    public void anticipateAndSend(String event,
            String version, String enterprise, int generic, int specific, String snmphost)
    throws Exception {
        Event snmp = createEvent(version, enterprise, generic, specific, snmphost);
        verifyMatchingEventConf(event, snmp);
    }

    public void verifyMatchingEventConf(String expectedUei, Event trapEvent) {

        org.opennms.netmgt.xml.eventconf.Event configuredEvent = m_eventConfDao.getMatchingEventConf(trapEvent);

        System.out.println("Eventconf: " + EventConfWrapper.toString(configuredEvent) );

//      assertMatches("Exepected a match for "+expectedUei+" but received "+EventConfWrapper.toString( configuredEvent ),
//      expectedUei, configuredEvent);

    }

    private void assertMatches(String string, String expectedUei, org.opennms.netmgt.xml.eventconf.Event configuredEvent) {

        if (expectedUei != null) {
            if (configuredEvent == null) {
                fail("Was expecting to match an eventconf with a UEI of \"" + expectedUei +
                "\", but no matching eventconf was found.");
            } else {
                if (!expectedUei.equals(configuredEvent.getUei())) {
                    fail("Was expecting to match an eventconf with a UEI of \"" + expectedUei +
                            "\", but received an eventconf with a UEI of \"" + configuredEvent.getUei() +
                    "\"");
                }
                // everything's fine
            }
        } else {
            if (configuredEvent != null) {
                boolean complain = true;

                Logmsg logmsg = configuredEvent.getLogmsg();
                if (logmsg != null) {
                    String dest = logmsg.getDest();
                    if ("discardtraps".equals(dest)) {
                        complain = false;
                    }
                }

                if (complain) {
                    fail("Was not expecting an eventconf, but received an eventconf with " +
                            "an UEI of \"" + configuredEvent.getUei() + "\"");
                }
            }
            // everything's fine
        }

        finishUp();
    }


}