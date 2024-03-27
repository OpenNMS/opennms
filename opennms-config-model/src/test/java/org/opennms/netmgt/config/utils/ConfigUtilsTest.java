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
