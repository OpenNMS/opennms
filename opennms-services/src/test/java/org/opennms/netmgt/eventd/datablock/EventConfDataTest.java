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

package org.opennms.netmgt.eventd.datablock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.Base64;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.mock.EventConfWrapper;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.springframework.core.io.FileSystemResource;

public class EventConfDataTest {
    
    DefaultEventConfDao eventConfDao;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(false);

        eventConfDao = new DefaultEventConfDao();
        eventConfDao.setConfigResource(new FileSystemResource(ConfigurationTestUtils.getFileForResource(this, "eventconf.xml")));
        eventConfDao.afterPropertiesSet();
    }


    public void finishUp() {
    }

    @After
    public void tearDown() {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void testEventValuePassesMaskValueExactFail() {
        assertFalse(EventConfData.eventValuePassesMaskValue("George Clinton, father of funk", Collections.singletonList("George Clinton, teh father of funk")));
    }
    
    @Test
    public void testEventValuePassesMaskValueExactPass() {
        assertTrue(EventConfData.eventValuePassesMaskValue("George Clinton, father of funk", Collections.singletonList("George Clinton, father of funk")));
    }
    
    @Test
    public void testEventValuePassesMaskValueSubStringFail() {
        assertFalse(EventConfData.eventValuePassesMaskValue("George Clinton, teh father of funk", Collections.singletonList("George Clinton, father of %")));
    }
    
    @Test
    public void testEventValuePassesMaskValueSubStringPass() {
        assertTrue(EventConfData.eventValuePassesMaskValue("George Clinton, father of funk", Collections.singletonList("George Clinton, father of %")));
    }

    @Test
    public void testEventValuePassesMaskValueSubStringPassEmpty() {
        assertTrue(EventConfData.eventValuePassesMaskValue("", Collections.singletonList("%")));
    }

    @Test
    public void testEventValuePassesMaskValueRegexAnchoredFail() {
        assertFalse(EventConfData.eventValuePassesMaskValue("George Clinton, father of funk", Collections.singletonList("~^Bill.*Clinton.*funk$")));
    }
    
    @Test
    public void testEventValuePassesMaskValueRegexAnchoredPass() {
        assertTrue(EventConfData.eventValuePassesMaskValue("George Clinton, father of funk", Collections.singletonList("~^George.*Clinton.*funk$")));
    }
    
    @Test
    public void testEventValuePassesMaskValueRegexUnanchoredPass() {
        assertTrue(EventConfData.eventValuePassesMaskValue("Is FooBar On Air", Collections.singletonList("~.*Foo[Bb]ar.*")));
    }

    @Test
    public void testV1TrapNewSuspect() throws Exception {
        anticipateAndSend(null, "v1", null, 6, 1);
    }

    @Test
    public void testV2TrapNewSuspect() throws Exception {
        anticipateAndSend(null, "v2c", null, 6, 1);
    }

    @Test
    public void testV1EnterpriseIdAndGenericMatch() throws Exception {
        anticipateAndSend("uei.opennms.org/IETF/BGP/traps/bgpEstablished", "v1",
                ".1.3.6.1.2.1.15.7", 6, 1);
    }

    @Test
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

    @Test
    public void testV2EnterpriseIdAndGenericAndSpecificMissWithExtraZeros() throws Exception {
        anticipateAndSend(null, "v2c",
                ".1.3.6.1.2.1.15.7.0.0", 6, 1);
    }

    @Test
    public void testV1EnterpriseIdAndGenericAndSpecificMissWithWrongGeneric() throws Exception {
        anticipateAndSend(null, "v1",
                ".1.3.6.1.2.1.15.7", 5, 1);
    }

    @Test
    public void testV1EnterpriseIdAndGenericAndSpecificMissWithWrongSpecific() throws Exception {
        anticipateAndSend(null, "v1",
                ".1.3.6.1.2.1.15.7", 6, 50);
    }

    @Test
    public void testV1GenericMatch() throws Exception {
        anticipateAndSend("uei.opennms.org/generic/traps/SNMP_Cold_Start",
                "v1", null, 0, 0);
    }

    @Test
    public void testV2GenericMatch() throws Exception {
        anticipateAndSend("uei.opennms.org/generic/traps/SNMP_Cold_Start",
                "v2c", ".1.3.6.1.6.3.1.1.5.1", 0, 0);
    }

    @Test
    public void testV1TrapDroppedEvent() throws Exception {
        anticipateAndSend(null, "v1", ".1.3.6.1.2.1.15.7", 6, 2);
    }

    @Test
    public void testV2TrapDroppedEvent() throws Exception {
        anticipateAndSend(null, "v2c", ".1.3.6.1.2.1.15.7", 6, 2);
    }

    @Test
    public void testV1TrapDefaultEvent() throws Exception {
        anticipateAndSend(null, "v1", null, 6, 1);
    }

    @Test
    public void testV2TrapDefaultEvent() throws Exception {
        anticipateAndSend(null, "v2c", null, 6, 1);
    }

    @Test
    public void testV1TrapDroppedIPEvent() throws Exception {
        anticipateAndSend(null, "v1", null, 0, 0, "192.168.1.1");
    }

    @Test
    public void testV1TrapNotDroppedIPOffEvent() throws Exception {
        anticipateAndSend("uei.opennms.org/generic/traps/SNMP_Cold_Start",
                "v1", null, 0, 0, "192.168.1.2");
    }

    @Test
    public void testV1TrapDroppedNetwork1Event() throws Exception {
        anticipateAndSend(null, "v1", ".1.3.6.1.2.1.15.7", 6, 1, "192.168.1.1");
    }

    @Test
    public void testV1TrapDroppedNetwork2Event() throws Exception {
        anticipateAndSend(null,
                "v1", ".1.3.6.1.2.1.15.7", 6, 1, "192.168.1.2");
    }

    @Test
    public void testV1TrapNotDroppedNetworkOffEvent() throws Exception {
        anticipateAndSend("uei.opennms.org/IETF/BGP/traps/bgpEstablished",
                "v1", ".1.3.6.1.2.1.15.7", 6, 1, "192.168.2.1");
    }

    @Test
    @Ignore("This is a test for unwritten functionality.. see comment in test")
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

        anticipateAndSend("uei.opennms.org/vendor/cisco/bsnAPNoiseProfileUpdatedToPass",
                "v1", ".1.3.6.1.4.1.14179.2.6.3", 6, 38, "192.168.2.1", varbinds);
    }

    private EventBuilder createEventBuilder(String version, String enterprise, int generic, int specific) {
        EventBuilder bldr = new EventBuilder(null, "EventConfDataTest");

        bldr.setInterface(addr("127.0.0.1"));
        bldr.setNodeid(0);
        bldr.setSnmpVersion(version);
        bldr.setEnterpriseId(enterprise);
        bldr.setGeneric(generic);
        bldr.setSpecific(specific);
        bldr.setSnmpHost("127.0.0.1");

        /* System.out.println("Event created: " + new EventWrapper(event)); */

        return bldr;
    }

    public EventBuilder createEventBuilder(String version, String enterprise, int generic, int specific, String snmphost) {
        EventBuilder bldr = createEventBuilder(version, enterprise, generic, specific);

        bldr.setSnmpHost(snmphost);

        return bldr;
    }

    private EventBuilder createEventBuilder(String version, String enterprise, int generic, int specific, String snmphost, LinkedHashMap<String, String> varbinds) {
        EventBuilder blder = createEventBuilder(version, enterprise, generic, specific, snmphost);

        for(Map.Entry<String, String> entry : varbinds.entrySet()) {
            blder.addParam(entry.getKey(), entry.getValue());
        }

        return blder;
    }


    private void anticipateAndSend(String event, String version, String enterprise, int generic, int specific, String snmphost, LinkedHashMap<String, String> varbinds) {
        EventBuilder snmp = createEventBuilder(version, enterprise, generic, specific, snmphost, varbinds);
        anticipateAndSend(event, snmp.getEvent());
    }

    public void anticipateAndSend(String event,
            String version, String enterprise, int generic, int specific) throws Exception {
        EventBuilder snmp = createEventBuilder(version, enterprise, generic, specific);
        anticipateAndSend(event, snmp.getEvent());
    }

    public void anticipateAndSend(String event,
            String version, String enterprise, int generic, int specific, String snmphost)
    throws Exception {
        EventBuilder snmp = createEventBuilder(version, enterprise, generic, specific, snmphost);
        anticipateAndSend(event, snmp.getEvent());
    }

    public void anticipateAndSend(String event, Event snmp) {
        /*
		if (event != null) {
			createEvent(event);
		}
         */

        org.opennms.netmgt.xml.eventconf.Event econf = eventConfDao.findByEvent(snmp);

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