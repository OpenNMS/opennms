/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.mibcompiler;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsmiparser.parser.SmiDefaultParser;
import org.jsmiparser.smi.SmiMib;
import org.jsmiparser.smi.SmiModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.mibcompiler.api.MibParser;
import org.opennms.features.mibcompiler.services.JsmiMibParser;
import org.opennms.netmgt.xml.eventconf.Events;

/**
 * The Test Class for NMS-9718</a>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class NMS9718Test {

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
     * Test standard parse.
     * <p>This test is to verify that the problem is not JsmiParser.</p>
     * 
     * @throws Exception the exception
     */
    @Test
    public void testStandardParse() throws Exception {
        SmiDefaultParser parser = new SmiDefaultParser();
        List<URL> inputUrls = new ArrayList<>();
        try {
            inputUrls.add(new File(MIB_DIR, "SNMPv2-SMI.txt").toURI().toURL());
            inputUrls.add(new File(MIB_DIR, "RFC1155-SMI.txt").toURI().toURL());
            inputUrls.add(new File(MIB_DIR, "RFC-1212.txt").toURI().toURL());
            inputUrls.add(new File(MIB_DIR, "RFC-1215.txt").toURI().toURL());
            inputUrls.add(new File(MIB_DIR, "RFC1213-MIB.txt").toURI().toURL());
            inputUrls.add(new File(MIB_DIR, "GWMONITOR-MIB.mib").toURI().toURL());
        } catch (Exception e) {
            Assert.fail();
        }
        parser.getFileParserPhase().setInputUrls(inputUrls);
        SmiMib mib = parser.parse();
        if (parser.getProblemEventHandler().isOk()) {
            Assert.assertNotNull(mib);
            boolean found = false;
            for (SmiModule m : mib.getModules()) {
                if (m.getId().equals("GWMONITOR-MIB"))
                    found = true;
            }
            Assert.assertTrue(found);
        } else {
            Assert.fail("The GWMONITOR-MIB couldn't be compiled: " + parser.getProblemEventHandler().getTotalCount() + " problems encountered");
        }
    }

    /**
     * Test custom parse.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCustomParse() throws Exception {
        if (parser.parseMib(new File(MIB_DIR, "GWMONITOR-MIB.mib"))) {
            Assert.assertTrue(parser.getMissingDependencies().isEmpty());
            Assert.assertNull(parser.getFormattedErrors());
        } else {
            Assert.fail("The GWMONITOR-MIB couldn't be compiled");
        }
    }

    /**
     * Test trap-OIDs in events from GWMONITOR-MIB notifications.
     *
     * @throws Exception the exception
     */
    @Test
    public void testMibTraps() throws Exception {
        if (! parser.parseMib(new File(MIB_DIR, "GWMONITOR-MIB.mib"))) {
            Assert.fail("The GWMONITOR-MIB must parse successfully");
        } else {
            Assert.assertTrue(parser.getMissingDependencies().isEmpty());
        }
        Events events = parser.getEvents("uei.opennms.org/GWMONITOR-MIB/");
        assertEquals(6, events.getEvents().size());
    }

}
