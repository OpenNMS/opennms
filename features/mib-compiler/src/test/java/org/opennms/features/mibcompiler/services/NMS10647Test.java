/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
