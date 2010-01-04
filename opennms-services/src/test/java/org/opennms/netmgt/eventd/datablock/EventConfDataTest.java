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
// 2008 Feb 15: Work with updated dependency injected and Resource-based DAO. - dj@opennms.org
// 2008 Jan 08: Use EventConfDao/EventconfFactory instead of EventConfigurationManager. - dj@opennms.org
// 2007 Dec 25: Use the new EventConfigurationManager.loadConfiguration(File). - dj@opennms.org
// 2007 Dec 24: Pull config file into an external file. - dj@opennms.org
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
//$Id: EventConfDataTest.java 10576 2008-09-18 14:43:43Z rangerrick $
//
package org.opennms.netmgt.eventd.datablock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.opennms.core.utils.Base64;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.config.EventconfFactory;
import org.opennms.netmgt.mock.EventConfWrapper;
import org.opennms.netmgt.mock.EventWrapper;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.FileSystemResource;

public class EventConfDataTest extends TestCase {

    protected void setUp() throws Exception {
        MockLogAppender.setupLogging(false);

        DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
        eventConfDao.setConfigResource(new FileSystemResource(ConfigurationTestUtils.getFileForResource(this, "eventconf.xml")));
        eventConfDao.afterPropertiesSet();
        
        EventconfFactory.setInstance(eventConfDao);
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
        anticipateAndSend(null, "v1", null, 6, 1);
    }

    public void testV2TrapNewSuspect() throws Exception {
        anticipateAndSend(null, "v2c", null, 6, 1);
    }

    public void testV1EnterpriseIdAndGenericMatch() throws Exception {
        anticipateAndSend("uei.opennms.org/IETF/BGP/traps/bgpEstablished", "v1",
                ".1.3.6.1.2.1.15.7", 6, 1);
    }

    public void testV2EnterpriseIdAndGenericAndSpecificMatch() throws Exception {
        anticipateAndSend("uei.opennms.org/IETF/BGP/traps/bgpEstablished", "v2c",
                ".1.3.6.1.2.1.15.7", 6, 1);
    }

    // This test does not work because the extra zero is pulled off by trapd
    /*
	public void testV2EnterpriseIdAndGenericAndSpecificMatchWithZero() throws Exception {
		anticipateAndSend("uei.opennms.org/IETF/BGP/traps/bgpEstablished", "v2c",
				".1.3.6.1.2.1.15.7.0", 6, 1);
	}
     */

    public void testV2EnterpriseIdAndGenericAndSpecificMissWithExtraZeros() throws Exception {
        anticipateAndSend(null, "v2c",
                ".1.3.6.1.2.1.15.7.0.0", 6, 1);
    }

    public void testV1EnterpriseIdAndGenericAndSpecificMissWithWrongGeneric() throws Exception {
        anticipateAndSend(null, "v1",
                ".1.3.6.1.2.1.15.7", 5, 1);
    }

    public void testV1EnterpriseIdAndGenericAndSpecificMissWithWrongSpecific() throws Exception {
        anticipateAndSend(null, "v1",
                ".1.3.6.1.2.1.15.7", 6, 50);
    }

    public void testV1GenericMatch() throws Exception {
        anticipateAndSend("uei.opennms.org/generic/traps/SNMP_Cold_Start",
                "v1", null, 0, 0);
    }

    public void testV2GenericMatch() throws Exception {
        anticipateAndSend("uei.opennms.org/generic/traps/SNMP_Cold_Start",
                "v2c", ".1.3.6.1.6.3.1.1.5.1", 0, 0);
    }

    public void testV1TrapDroppedEvent() throws Exception {
        anticipateAndSend(null, "v1", ".1.3.6.1.2.1.15.7", 6, 2);
    }

    public void testV2TrapDroppedEvent() throws Exception {
        anticipateAndSend(null, "v2c", ".1.3.6.1.2.1.15.7", 6, 2);
    }

    public void testV1TrapDefaultEvent() throws Exception {
        anticipateAndSend(null, "v1", null, 6, 1);
    }

    public void testV2TrapDefaultEvent() throws Exception {
        anticipateAndSend(null, "v2c", null, 6, 1);
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
    public void FIXMEtestV1EnterpriseIdAndGenericAndSpecificAndMatchWithVarbindsAndTC()
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

        anticipateAndSend("uei.opennms.org/vendor/cisco/bsnAPNoiseProfileUpdatedToPass",
                "v1", ".1.3.6.1.4.1.14179.2.6.3", 6, 38, "192.168.2.1", varbinds);
    }

    public Event createEvent(String uei) {
        Event event = new Event();
        event.setInterface("127.0.0.1");
        event.setNodeid(0);
        event.setUei(uei);
        System.out.println("Event created: " + new EventWrapper(event));
        return event;
    }

    public Event createEvent(String version, String enterprise, int generic, int specific) {
        Event event = new Event();

        event.setInterface("127.0.0.1");
        event.setNodeid(0);

        Snmp snmp = new Snmp();
        snmp.setVersion(version);
        snmp.setId(enterprise);
        snmp.setGeneric(generic);
        snmp.setSpecific(specific);
        event.setSnmp(snmp);

        event.setSnmphost("127.0.0.1");

        /* System.out.println("Event created: " + new EventWrapper(event)); */

        return event;
    }

    public Event createEvent(String version, String enterprise, int generic, int specific,
            String snmphost) {
        Event event = createEvent(version, enterprise, generic, specific);

        event.setSnmphost(snmphost);

        return event;
    }

    private Event createEvent(String version, String enterprise, int generic, int specific, String snmphost, LinkedHashMap<String, String> varbinds) {
        Event event = createEvent(version, enterprise, generic, specific, snmphost);

        EventBuilder blder = new EventBuilder(event);
        for(Map.Entry<String, String> entry : varbinds.entrySet()) {
            blder.addParam(entry.getKey(), entry.getValue());
        }

        return blder.getEvent();
    }


    private void anticipateAndSend(String event, String version, String enterprise, int generic, int specific, String snmphost, LinkedHashMap<String, String> varbinds) {
        Event snmp = createEvent(version, enterprise, generic, specific, snmphost, varbinds);
        anticipateAndSend(event, snmp);
    }

    public void anticipateAndSend(String event,
            String version, String enterprise, int generic, int specific) throws Exception {
        Event snmp = createEvent(version, enterprise, generic, specific);
        anticipateAndSend(event, snmp);
    }

    public void anticipateAndSend(String event,
            String version, String enterprise, int generic, int specific, String snmphost)
    throws Exception {
        Event snmp = createEvent(version, enterprise, generic, specific, snmphost);
        anticipateAndSend(event, snmp);
    }

    public void anticipateAndSend(String event, Event snmp) {
        /*
		if (event != null) {
			createEvent(event);
		}
         */

        org.opennms.netmgt.xml.eventconf.Event econf = EventconfFactory.getInstance().findByEvent(snmp);

        System.out.println("Eventconf: " + (econf == null ? null : new EventConfWrapper(econf) ));

        if (event != null) {
            if (econf == null) {
                fail("Was expecting to match an eventconf with a UEI of \"" + event +
                "\", but no matching eventconf was found.");
            } else {
                if (!event.equals(econf.getUei())) {
                    fail("Was expecting to match an eventconf with a UEI of \"" + event +
                            "\", but received an eventconf with a UEI of \"" + econf.getUei() +
                    "\"");
                }
                // everything's fine
            }
        } else {
            if (econf != null) {
                boolean complain = true;

                Logmsg logmsg = econf.getLogmsg();
                if (logmsg != null) {
                    String dest = logmsg.getDest();
                    if ("discardtraps".equals(dest)) {
                        complain = false;
                    }
                }

                if (complain) {
                    fail("Was not expecting an eventconf, but received an eventconf with " +
                            "an UEI of \"" + econf.getUei() + "\"");
                }
            }
            // everything's fine
        }

        finishUp();
    }

}