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

import org.jsmiparser.parser.SmiDefaultParser;
import org.jsmiparser.smi.SmiMib;
import org.jsmiparser.smi.SmiModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.mibcompiler.api.MibParser;
import org.opennms.features.mibcompiler.services.JsmiMibParser;
import org.opennms.features.namecutter.NameCutter;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.xml.eventconf.Events;

import java.io.File;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * The Test Class for NMS-14314</a>
 */
public class NMS14314Test {

    /**
     * The Constant MIB_DIR.
     */
    protected static final File MIB_DIR = new File("src/test/resources");

    /**
     * The parser.
     */
    protected MibParser parser;
    private List<Group> groups;

    /**
     * Sets the up.
     */
    @Before
    public void setUp() {
        parser = new JsmiMibParser();
        parser.setMibDirectory(MIB_DIR);
    }

    /**
     * Test custom parse.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCustomParseForIpAddress() throws Exception {
        if (parser.parseMib(new File(MIB_DIR, "RFC1213-MIB.mib"))) {
            Assert.assertTrue(parser.getMissingDependencies().isEmpty());
            Assert.assertNull(parser.getFormattedErrors());
            final NameCutter cutter = new NameCutter();

            final List<String> matches = new ArrayList<String>(List.of(
                    "ipAdEntNetMask", "ipRouteDest",
                    "ipRouteNextHop", "ipRouteMask",
                    "ipNetToMediaNetAddress", "tcpConnLocalAddress",
                    "tcpConnRemAddress", "udpLocalAddress",
                    "egpNeighAddr", "ipAdEntAddr"));
            final Set<String> results = new HashSet<>();
            for (int i = 0; i < matches.size(); i++) {
                matches.set(i, cutter.trimByCamelCase(matches.get(i), 19));
            }
            for (Group g : parser.getDataCollection().getGroups()) {
                for (MibObj mibObj : g.getMibObjs()) {
                    if (matches.contains(mibObj.getAlias())) {
                        Assert.assertEquals("string", mibObj.getType());
                        results.add(mibObj.getAlias());
                    }
                }
            }
            Assert.assertTrue(matches.containsAll(results));
        } else {
            Assert.fail("The RFC1213-MIB couldn't be compiled");
        }
    }


}
