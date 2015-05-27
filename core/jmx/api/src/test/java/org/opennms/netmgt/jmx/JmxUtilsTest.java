/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jmx;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class JmxUtilsTest {

    private static final int MAX_DS_NAME_LENGTH = 19;

    @Test
    public void testConvert() {
        Map<String, Object> input = new HashMap<>();
        input.put("1", "1 Value");
        input.put("2", "2 Value");
        input.put("3", 3);

        Map<String, String> output = JmxUtils.convertToStringMap(input);
        Assert.assertNotNull(output);
        Assert.assertEquals(2, output.size());

        Assert.assertEquals("1 Value", output.get("1"));
        Assert.assertEquals("2 Value", output.get("2"));
        Assert.assertNull(output.get("3"));
    }

    @Test
    public void testNotModifiable() {
        Map<String, Object> input = new HashMap<>();
        input.put("A", "VALUE");

        Map<String, String> output = JmxUtils.convertToStringMap(input);

        try {
            output.put("4", "4 Value");
            Assert.fail("The converted output map should not be modifiable");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    @Test
    public void testNullInput() {
        Map<String, String> output = JmxUtils.convertToStringMap(null);
        Assert.assertNull(output);
    }

    @Test
    public void testGetCollectionDirectory() {
        Map<String, String> input = new HashMap<>();
        input.put("port", "100");

        String collectionDir = JmxUtils.getCollectionDirectory(input, "ulf", "alf");
        Assert.assertEquals("ulf", collectionDir);

        String collectionDir2 = JmxUtils.getCollectionDirectory(input, null, null);
        Assert.assertEquals("100", collectionDir2);

        String collectionDir3 = JmxUtils.getCollectionDirectory(input, null, "alf");
        Assert.assertEquals("alf", collectionDir3);

        String collectionDir4 = JmxUtils.getCollectionDirectory(input, "ulf", null);
        Assert.assertEquals("ulf", collectionDir4);

        try {
            JmxUtils.getCollectionDirectory(null, null, null);
            Assert.fail("NullPointerException should have been thrown.");
        } catch (NullPointerException npe) {

        }

        String collectionDir5 = JmxUtils.getCollectionDirectory(new HashMap<String, String>(), null, null);
        Assert.assertEquals(null, collectionDir5);
    }

    @Test
    public void shouldTrimName() {
        final String shortName = "short";
        final String exactName = "abcdefghijklmnopqrs"; // 19 chars
        final String exceeded = "abcdefghijklmnopqrstuvwxyz"; // 26 chars

        Assert.assertEquals("short", JmxUtils.trimAttributeName(shortName));
        Assert.assertEquals("abcdefghijklmnopqrs", JmxUtils.trimAttributeName(exactName));
        Assert.assertEquals("abcdefghijklmnopqrs", JmxUtils.trimAttributeName(exceeded));
    }

}
