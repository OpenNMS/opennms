/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

public class ConfigUtilsTest {
    private void expectException(final Runnable r) {
        Exception expected = null;
        try {
            r.run();
        } catch (final Exception e) {
            expected = e;
        }
        assertNotNull(expected);
    }

    @Test
    public void testNormalizeString() {
        assertNull(ConfigUtils.normalizeString(null));

        assertNull(ConfigUtils.normalizeString(""));
    }

    @Test
    public void testNormalizeAndTrimString() {
        assertNull(ConfigUtils.normalizeAndTrimString(null));

        assertNull(ConfigUtils.normalizeAndTrimString(""));

        final String value = ConfigUtils.normalizeAndTrimString("blah");
        assertEquals("blah", value);
    }

    @Test
    public void testNormalizeAndInternString() {
        assertNull(ConfigUtils.normalizeAndInternString(null));

        assertNull(ConfigUtils.normalizeAndInternString(""));

        final String value = ConfigUtils.normalizeAndInternString("blah");
        assertEquals("blah", value);
    }

    @Test
    public void testAssertNotNull() {
        expectException(() -> {
            ConfigUtils.assertNotNull(null, "name");
        });

        String value = ConfigUtils.assertNotNull("foo", "name");
        assertEquals("foo", value);

        value = ConfigUtils.assertNotNull("", "name");
        assertEquals("", value);
    }

    @Test
    public void testAssertNotEmpty() {
        expectException(() -> {
            ConfigUtils.assertNotEmpty(null, "name");
        });

        expectException(() -> {
            ConfigUtils.assertNotEmpty("", "name");
        });

        final String value = ConfigUtils.assertNotEmpty("foo", "name");
        assertEquals("foo", value);
    }

    @Test
    public void testAssertMinimumInclusive() {
        ConfigUtils.assertMinimumInclusive(null, 1, "name");

        expectException(() -> {
            ConfigUtils.assertMinimumInclusive(0, 1, "name");
        });

        ConfigUtils.assertMinimumInclusive(1, 1, "name");
    }

    @Test
    public void testAssertMinimumSize() {
        ConfigUtils.assertMinimumSize(null, 1, "name");

        expectException(() -> {
            ConfigUtils.assertMinimumSize(Arrays.asList(), 1, "name");
        });

        ConfigUtils.assertMinimumSize(Arrays.asList("foo", "bar"), 1, "name");
    }

    @Test
    public void testAssertOnlyContains() {
        final List<String> acceptable = Arrays.asList("foo", "bar", "baz");

        ConfigUtils.assertOnlyContains(null, acceptable, "name");
        ConfigUtils.assertOnlyContains("foo", acceptable, "name");
        ConfigUtils.assertOnlyContains("bar", acceptable, "name");

        expectException(() -> {
            ConfigUtils.assertOnlyContains("", acceptable, "name");
        });

        expectException(() -> {
            ConfigUtils.assertOnlyContains("blah", acceptable, "name");
        });
    }

    @Test
    public void testAssertMatches() {
        ConfigUtils.assertMatches(null, Pattern.compile("foo"), "name");
        ConfigUtils.assertMatches("foo", Pattern.compile("foo"), "name");
        ConfigUtils.assertMatches("foo", Pattern.compile(".*o.*"), "name");
        expectException(() -> {
            ConfigUtils.assertMatches("foo", Pattern.compile(".*a.*"), "name");
        });
    }
}
