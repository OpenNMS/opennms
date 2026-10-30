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
package org.opennms.features.mibcompiler.services;

import java.io.File;
import java.util.regex.Pattern;

import org.jsmiparser.parser.SmiDefaultParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.mibcompiler.api.MibParser;

/**
 * The Test Class for NMS-10647</a>
 *
 * @author <a href="mailto:christian@opennms.org">Christian Pape</a>
 */public class NMS10647Test {

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

    @Test
    public void testMibExtraction() {
        final OnmsProblemEventHandler onmsProblemEventHandler = new OnmsProblemEventHandler(new SmiDefaultParser());

        final String validUnix = "/dev/opennms/develop/features/mib-compiler/src/test/resources/IF-MIB.txt:1116:11:snmpTraps";
        final String invalidUnix = "/dev/opennms/develop/features/mib-compiler/src/test/resources/G6-NOTIFICATION-MIB_0.20.mib : unexpected char: '='";
        final String validWindows = "C:\\dev\\opennms\\develop\\features\\mib-compiler\\src\\test\\resources\\IF-MIB.txt:1116:11:snmpTraps";
        final String invalidWindows = "C:\\dev\\opennms\\develop\\features\\mib-compiler\\src\\test\\resources\\G6-NOTIFICATION-MIB_0.20.mib : unexpected char: '='";

        Assert.assertEquals("snmpTraps", onmsProblemEventHandler.getMibFromSource(validUnix, '/'));
        Assert.assertNull(onmsProblemEventHandler.getMibFromSource(invalidUnix, '/'));
        Assert.assertEquals("snmpTraps", onmsProblemEventHandler.getMibFromSource(validWindows, '\\'));
        Assert.assertNull(onmsProblemEventHandler.getMibFromSource(invalidWindows, '\\'));

        final String kickIt = "C:\\something\\very:wrong\\this:should:be:null";
        final String beforeYou = "/this/should/also/be/null";
        final String lickIt = "/this:is:also:null:yeah";

        Assert.assertNull(onmsProblemEventHandler.getMibFromSource(kickIt,'\\'));
        Assert.assertNull(onmsProblemEventHandler.getMibFromSource(beforeYou,'/'));
        Assert.assertNull(onmsProblemEventHandler.getMibFromSource(lickIt,'/'));
    }

    /**
     * Test custom parse.
     */
    @Test
    public void testParsingError() {
        final String regex = "ERROR: Lex error at: file://.*/G6-NOTIFICATION-MIB_0\\.20\\.mib : unexpected char: '=', Source: G6-NOTIFICATION-MIB_0\\.20\\.mib.*";
        final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        parser.parseMib(new File(MIB_DIR, "G6-NOTIFICATION-MIB_0.20.mib"));
        Assert.assertTrue(pattern.matcher(parser.getFormattedErrors()).find());
    }
}
