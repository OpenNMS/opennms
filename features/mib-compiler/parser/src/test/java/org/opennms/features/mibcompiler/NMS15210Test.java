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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.mibcompiler.api.MibParser;
import org.opennms.features.mibcompiler.services.JsmiMibParser;
import org.opennms.features.namecutter.NameCutter;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.MibObj;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Test Class for NMS-14314</a>
 */
public class NMS15210Test {

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
    public void testCustomParseForCounterBasedGauge64() throws Exception {

        if (parser.parseMib(new File(MIB_DIR, "JUNIPER-IF-MIB.mib"))) {
            Assert.assertTrue(parser.getMissingDependencies().isEmpty());
            Assert.assertNull(parser.getFormattedErrors());
            final NameCutter cutter = new NameCutter();

            final List<String> matches = new ArrayList<String>(List.of(
                    "ifHCIn1SecRate", "ifHCOut1SecRate"));
            final Set<String> results = new HashSet<>();
            for (int i = 0; i < matches.size(); i++) {
                matches.set(i, cutter.trimByCamelCase(matches.get(i), 19));
            }
            for (Group g : parser.getDataCollection().getGroups()) {
                for (MibObj mibObj : g.getMibObjs()) {
                    if (matches.contains(mibObj.getAlias())) {
                        Assert.assertEquals("gauge", mibObj.getType());
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
