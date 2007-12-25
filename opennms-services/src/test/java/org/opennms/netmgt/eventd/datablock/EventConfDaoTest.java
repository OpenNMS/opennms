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
// Modifications:
//
// 2007 Dec 24: Indent, move config file to external file, organize
//              imports. - dj@opennms.org
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
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.Base64;
import org.opennms.netmgt.mock.EventConfWrapper;
import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.test.mock.MockLogAppender;

public class EventConfDaoTest extends TestCase {

    private EventConfDao m_eventConfDao;

    protected void setUp() throws Exception {
        MockLogAppender.setupLogging(false);

        File targetDir = new File("target");
        File configFile = File.createTempFile("eventConf", "xml", targetDir);
        configFile.deleteOnExit();

        String resource = "eventconf.xml";
        URL url = getClass().getResource(resource);
        assertNotNull("Could not get resource for " + resource, url);
        
        FileUtils.copyURLToFile(url, configFile);
        
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