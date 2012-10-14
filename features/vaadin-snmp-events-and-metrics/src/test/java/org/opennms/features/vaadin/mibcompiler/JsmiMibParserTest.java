/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.mibcompiler;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.mibcompiler.api.MibParser;
import org.opennms.features.vaadin.mibcompiler.services.JsmiMibParser;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Maskelement;
import org.opennms.netmgt.xml.eventconf.Varbindsdecode;

/**
 * The Test Class for JsmiMibParser.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class JsmiMibParserTest {

    /** The Constant MIB_DIR. */
    protected static final File MIB_DIR = new File("src/test/resources");

    /** The parser. */
    protected MibParser parser;

    /**
     * Sets the up.
     */
    @Before
    public void setUp() {
        parser = new JsmiMibParser();
        parser.setMibDirectory(MIB_DIR);
    }

    /**
     * Test good MIB.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGoodMib() throws Exception {
        if (parser.parseMib(new File(MIB_DIR, "IF-MIB.txt"))) {
            Assert.assertTrue(parser.getMissingDependencies().isEmpty());
            Assert.assertNull(parser.getFormattedErrors());
        } else {
            Assert.fail();
        }
    }

    /**
     * Test bad MIB.
     *
     * @throws Exception the exception
     */
    @Test
    public void testBadMib() throws Exception {
        if (parser.parseMib(new File(MIB_DIR, "SONUS-COMMON-MIB.txt"))) {
            Assert.fail();
        } else {
            List<String> dependencies = parser.getMissingDependencies();
            Assert.assertEquals(2, dependencies.size());
            Assert.assertNotNull(parser.getFormattedErrors());
            Assert.assertEquals("[SONUS-SMI, SONUS-TC]", dependencies.toString());
        }
    }

    /**
     * Test generate events from notifications.
     *
     * @throws Exception the exception
     */
    @Test
    public void testNotifications() throws Exception {
        if (parser.parseMib(new File(MIB_DIR, "IF-MIB.txt"))) {
            Events events = parser.getEvents("uei.opennms.org/traps/ifmib");
            Assert.assertNotNull(events);
            System.out.println(JaxbUtils.marshal(events));
            Assert.assertEquals(2, events.getEventCount());
            Event event  = null;
            for (Event e : events.getEventCollection()) {
                if (e.getUei().contains("linkDown"))
                    event = e;
            }
            Assert.assertNotNull(event);
            Assert.assertNotNull(event.getDescr()); // TODO Must be more specific
            Assert.assertNotNull(event.getLogmsg());
            Assert.assertNotNull(event.getLogmsg().getContent()); // TODO Must be more specific
            Assert.assertEquals("Indeterminate", event.getSeverity());
            Assert.assertEquals("IF-MIB defined trap event: linkDown", event.getEventLabel());
            Assert.assertNotNull(event.getMask());
            for (Maskelement me : event.getMask().getMaskelementCollection()) {
                if (me.getMename().equals("id"))
                    Assert.assertEquals(".1.3.6.1.6.3.1.1.5", me.getMevalueCollection().get(0));
                if (me.getMename().equals("generic"))
                    Assert.assertEquals("6", me.getMevalueCollection().get(0));
                if (me.getMename().equals("specific"))
                    Assert.assertEquals("3", me.getMevalueCollection().get(0));
            }
            Assert.assertEquals(2, event.getVarbindsdecodeCount());
            for (Varbindsdecode vb : event.getVarbindsdecodeCollection()) {
                if (vb.getParmid().equals("parm[#2]"))
                    Assert.assertEquals(3, vb.getDecodeCount());
                if (vb.getParmid().equals("parm[#3]"))
                    Assert.assertEquals(7, vb.getDecodeCount());
            }
        } else {
            Assert.fail();
        }
    }

    /**
     * Test generate events from traps.
     */
    @Test
    public void testTraps() {
        if (parser.parseMib(new File(MIB_DIR, "RFC1269-MIB.txt"))) {
            Assert.assertEquals("RFC1269-MIB", parser.getMibName());
            Events events = parser.getEvents("uei.opennms.org/traps/RFC1269");
            Assert.assertNotNull(events);
            Assert.assertEquals(2, events.getEventCount());
            System.out.println(JaxbUtils.marshal(events));
            Assert.assertEquals(2, events.getEventCount());
            Event event  = null;
            for (Event e : events.getEventCollection()) {
                if (e.getUei().contains("bgpBackwardTransition"))
                    event = e;
            }
            Assert.assertNotNull(event);
            Assert.assertNotNull(event.getDescr()); // TODO Must be more specific
            Assert.assertNotNull(event.getLogmsg());
            Assert.assertNotNull(event.getLogmsg().getContent()); // TODO Must be more specific
            Assert.assertEquals("Indeterminate", event.getSeverity());
            Assert.assertEquals("RFC1269-MIB defined trap event: bgpBackwardTransition", event.getEventLabel());
            Assert.assertNotNull(event.getMask());
            for (Maskelement me : event.getMask().getMaskelementCollection()) {
                if (me.getMename().equals("id"))
                    Assert.assertEquals(".1.3.6.1.2.1.15", me.getMevalueCollection().get(0));
                if (me.getMename().equals("generic"))
                    Assert.assertEquals("6", me.getMevalueCollection().get(0));
                if (me.getMename().equals("specific"))
                    Assert.assertEquals("2", me.getMevalueCollection().get(0));
            }
            Assert.assertEquals(1, event.getVarbindsdecodeCount());
            for (Varbindsdecode vb : event.getVarbindsdecodeCollection()) {
                if (vb.getParmid().equals("parm[#3]"))
                    Assert.assertEquals(6, vb.getDecodeCount());
            }
        } else {
            Assert.fail();
        }
    }

    /**
     * Test generate data collection.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGenerateDataCollection() throws Exception {
        if (parser.parseMib(new File(MIB_DIR, "IF-MIB.txt"))) {
            DatacollectionGroup dcGroup = parser.getDataCollection();
            Assert.assertNotNull(dcGroup);
            System.out.println(JaxbUtils.marshal(dcGroup));
            Assert.assertEquals(5, dcGroup.getResourceTypeCount());
            Assert.assertEquals(7, dcGroup.getGroupCount());
            Group mibGroup = null;
            for (Group g : dcGroup.getGroupCollection()) {
                if (g.getName().equals("ifTable"))
                    mibGroup = g;
            }
            Assert.assertNotNull(mibGroup);
            Assert.assertEquals(22, mibGroup.getMibObjCount());
            for (MibObj mo : mibGroup.getMibObjCollection()) {
                Assert.assertEquals("ifEntry", mo.getInstance());
                Assert.assertTrue(mo.getOid().startsWith(".1.3.6.1.2.1.2.2.1"));
                Assert.assertTrue(mo.getType().matches("^([Ii]nteger|[Gg]auge|[Ss]tring|[Oo]ctet[Ss]tring|[Cc]ounter).*"));
            }
        } else {
            Assert.fail();
        }
    }

}
