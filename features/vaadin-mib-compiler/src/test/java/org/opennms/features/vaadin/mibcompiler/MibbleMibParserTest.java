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
package org.opennms.features.vaadin.mibcompiler;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.mibcompiler.services.MibbleMibParser;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.xml.eventconf.Events;

/**
 * The Test Class for MibbleMibParser.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class MibbleMibParserTest {

    private static final File MIB_DIR = new File("src/test/resources");

    /** The parser. */
    private MibbleMibParser parser;

    /**
     * Sets the up.
     */
    @Before
    public void setUp() {
        parser = new MibbleMibParser();
        parser.addMibDirectory(MIB_DIR);
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
            Assert.assertEquals(2, parser.getMissingDependencies().size());
            Assert.assertNotNull(parser.getFormattedErrors());
        }
    }

    /**
     * Test generate events.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGenerateEvents() throws Exception {
        if (parser.parseMib(new File(MIB_DIR, "IF-MIB.txt"))) {
            Events events = parser.getEvents("uei.opennms.org/traps/ifmib");
            Assert.assertNotNull(events);
            System.out.println(JaxbUtils.marshal(events));
            Assert.assertEquals(2, events.getEventCount());
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
            DatacollectionGroup group = parser.getDataCollection();
            Assert.assertNotNull(group);
            System.out.println(JaxbUtils.marshal(group));
            Assert.assertEquals(5, group.getResourceTypeCount());
            Assert.assertEquals(7, group.getGroupCount());
        } else {
            Assert.fail();
        }
    }

}
