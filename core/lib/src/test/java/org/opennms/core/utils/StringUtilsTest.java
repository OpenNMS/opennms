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
package org.opennms.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

public class StringUtilsTest {
    
    private String[] m_expected = { "The", "quick", "fox" };

    @Test
    public void testSimpleCommandArray() {
        String arg = "The   quick fox";
        testCreateCmdArray(m_expected, arg);
    }
    
    @Test
    public void testQuotedCommandArray() {
        testCreateCmdArray(m_expected, "\"The\" \"quick\" \"fox\"");
    }

    /**
     * The behavior with \t \n and \r characters inside a quoted
     * segment is odd.
     */
    @Test
    public void testQuotesContainingWhitespace() {
        String arg = "The quick \"brown \t\n\r \" fox";
        String[] actual = StringUtils.createCommandArray(arg);
        assertArrayEquals(new String[]{ "The", "quick", "brown ", " ", "fox" }, actual);
    }

    @Test
    public void testCommandArrayWithSpecialChars() {
        String arg = "The quick fox !@#$%^&*()-+[]{}|;:<>?,./";
        String[] actual = StringUtils.createCommandArray(arg);
        assertArrayEquals(new String[]{
                "The", "quick", "fox", "!@#$%^&*()-+[]{}|;:<>?,./"
                }, actual);
    }

    // Check NMS-6331 for more details.
    @Test
    public void testRrdPathWithSpaces() {
        String arg = "/usr/bin/rrdtool graph - --start 1463938619 --end 1464025019 --title=\"fwdd Uptime\" DEF:time=\"snmp/fs/The OpenNMS Office/Main Router/juniper-fwdd-process.rrd\":junFwddUptime:AVERAGE";
        String[] actual = StringUtils.createCommandArray(arg);
        assertArrayEquals(new String[]{
                "/usr/bin/rrdtool", "graph", "-", "--start", "1463938619", "--end", "1464025019", "--title=fwdd Uptime", "DEF:time=snmp/fs/The OpenNMS Office/Main Router/juniper-fwdd-process.rrd:junFwddUptime:AVERAGE"
                }, actual);
    }

    @Test
    public void testWindowsPaths() {
    	if (File.separatorChar != '\\') return;
    	if (Boolean.getBoolean("java.awt.headless")) return;
    	
    	final String[] trueStrings = new String[] { "C:\\monkey", "C:/monkey", "C:/", "C:\\" };
    	final String[] falseStrings = new String[] { "C:", "foo/bar", "/tmp/blah", "", "/", "blah:baz" };
    	
    	for (final String trueString : trueStrings) {
    		assertTrue(trueString, StringUtils.isLocalWindowsPath(trueString));
    	}
    	for (final String falseString : falseStrings) {
    		assertFalse(falseString, StringUtils.isLocalWindowsPath(falseString));
    	}
    }
    
    @Test
    public void testIso8601OffsetString() {
        assertEquals("1970-01-01T00:00:00Z", StringUtils.iso8601OffsetString(new Date(0), ZoneId.of("Z"), null));
        assertEquals("1970-01-01T00:00:00.001Z", StringUtils.iso8601OffsetString(new Date(1), ZoneId.of("Z"), null));
    }
    
    @Test
    public void testIso8601LocalOffsetString() {
        assertEquals(StringUtils.iso8601OffsetString(new Date(0), ZoneId.systemDefault(), null), StringUtils.iso8601LocalOffsetString(new Date(0)));
    }

    /**
     * Verify that {@link StringUtils#stripExtraQuotes(String)} strips
     * a single pair of matching double quotes from a string.
     */
    @Test
    public void testStripExtraQuotes() {
        assertEquals("\"", StringUtils.stripExtraQuotes("\""));
        assertEquals("", StringUtils.stripExtraQuotes("\"\""));
        assertEquals("\"", StringUtils.stripExtraQuotes("\"\"\""));
        assertEquals("\"\"", StringUtils.stripExtraQuotes("\"\"\"\""));
        assertEquals("abc", StringUtils.stripExtraQuotes("\"abc\""));
        assertEquals("\"abc\"", StringUtils.stripExtraQuotes("\"\"abc\"\""));
    }

    private static interface EqualsTrimmingMatcher {
        boolean equalsTrimmed(String a, String b);
    }

    @Test
    public void testEqualsTrimmed() {
        Map<String, EqualsTrimmingMatcher> impls = new LinkedHashMap<>();
        impls.put("naive", new EqualsTrimmingMatcher() {
            @Override
            public boolean equalsTrimmed(String a, String b) {
                return a != null && a.trim().equals(b);
            }
        });

        impls.put("optimized", new EqualsTrimmingMatcher() {
            @Override
            public boolean equalsTrimmed(String a, String b) {
                return StringUtils.equalsTrimmed(a, b);
            }
        });

        for (Map.Entry<String, EqualsTrimmingMatcher> impl : impls.entrySet()) {
            final String name = impl.getKey();
            final EqualsTrimmingMatcher matcher = impl.getValue(); 
            System.err.printf("Testing %s implementation.\n", name);

            // Negative hits
            assertEquals(name, false, matcher.equalsTrimmed(null, null));
            assertEquals(name, false, matcher.equalsTrimmed(null, "x"));
            assertEquals(name, false, matcher.equalsTrimmed("x", " x"));
            assertEquals(name, false, matcher.equalsTrimmed("xx", "x"));
            assertEquals(name, false, matcher.equalsTrimmed("x", "xx"));
            assertEquals(name, false, matcher.equalsTrimmed("x ", "xx"));
            assertEquals(name, false, matcher.equalsTrimmed("x", "x "));
            assertEquals(name, false, matcher.equalsTrimmed("x", " x "));

            // Positive hits
            assertEquals(name, true, matcher.equalsTrimmed("x", "x"));
            assertEquals(name, true, matcher.equalsTrimmed(" x", "x"));
            assertEquals(name, true, matcher.equalsTrimmed("x ", "x"));
            assertEquals(name, true, matcher.equalsTrimmed(" x ", "x"));
            assertEquals(name, true, matcher.equalsTrimmed(" \t\nx\t\n ", "x"));

            // Increase this when profiling
            final int N = 100000;
            long start = System.currentTimeMillis();
            for (int i = 0; i < N; i++) {
                // Both a negative positive hit
                matcher.equalsTrimmed("x", "xx");
                matcher.equalsTrimmed(" x ", "x");
            }
            long end = System.currentTimeMillis();
            System.err.printf("The %s implementation processed %d matches in %d ms.\n",
                    name, N, end - start);
        }
    }

    private void testCreateCmdArray(String[] expected, String arg) {
        String[] actual = StringUtils.createCommandArray(arg);
        assertArrayEquals(expected, actual);
    }
    
    private void assertArrayEquals(String[] expected, String[] actual) {
        assertEquals(Arrays.asList(expected), Arrays.asList(actual));
    }

    @Test
    public void testParseDecimalInteger() {
        for (String value : new String[] {
            "1234",
            "-444",
            "-0",
            "0",
            "0000",
            String.valueOf(Integer.MAX_VALUE),
            String.valueOf(Integer.MIN_VALUE)
        }) {
            assertEquals(Integer.parseInt(value), StringUtils.parseDecimalInt(value).intValue());
        }
    }

    @Test
    public void testParseDecimalIntegerInvalidValues() {
        for (String value : new String[] {
            "12F4",
            String.valueOf(Integer.MAX_VALUE) + "0",
            String.valueOf(Integer.MIN_VALUE) + "0",
            "-"
        }) {
            try {
                StringUtils.parseDecimalInt(value);
                fail("Value should have thrown NumberFormatException: " + value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    @Ignore("enable to test parsing speed manually")
    public void testParseDecimalIntegerSpeed() {
        final int WARM_UP = 1000000;
        final int TEST_SPEED = 50000000;
        for (int iterations : new int[] {WARM_UP, TEST_SPEED}) {
            for (String value : new String[] {
                    "1234",
                    "-444",
                    String.valueOf(Integer.MAX_VALUE),
                    String.valueOf(Integer.MIN_VALUE)
            }) {
                long start = System.nanoTime();
                for (int i = 0; i < iterations; i++) {
                    Integer.parseInt(value);
                }
                long parseIntTime = System.nanoTime() - start;

                start = System.nanoTime();
                for (int i = 0; i < iterations; i++) {
                    StringUtils.parseDecimalInt(value, false);
                }
                long parseDecimalIntTime = System.nanoTime() - start;

                start = System.nanoTime();
                for (int i = 0; i < iterations; i++) {
                    StringUtils.parseDecimalInt(value, true);
                }
                long parseDecimalIntExceptionTime = System.nanoTime() - start;

                if (iterations == TEST_SPEED) {
                    System.out.println("Results (no exceptions): " + parseDecimalIntTime + " ?<= " + parseIntTime);
                    assertTrue("StringUtils.parseDecimalInt() is slower: " + parseDecimalIntTime + " > " + parseIntTime, parseDecimalIntTime <= parseIntTime);
                    System.out.println("Results (exceptions)   : " + parseDecimalIntExceptionTime + " ?<= " + parseIntTime);
                    assertTrue("StringUtils.parseDecimalInt() is slower: " + parseDecimalIntExceptionTime + " > " + parseIntTime, parseDecimalIntExceptionTime <= parseIntTime);
                }
            }
        }
    }

    @Test
    @Ignore("enable to test parsing speed manually")
    public void testFailToParseDecimalIntegerSpeed() {
        final int WARM_UP = 10000;
        final int TEST_SPEED = 50000;
        for (int iterations : new int[] {WARM_UP, TEST_SPEED}) {
            for (String value : new String[] {
                "12F4",
                String.valueOf(Integer.MAX_VALUE) + "0",
                String.valueOf(Integer.MIN_VALUE) + "0",
                "-"
            }) {
                long start = System.nanoTime();
                for (int i = 0; i < iterations; i++) {
                    try {
                        Integer.parseInt(value);
                    } catch (NumberFormatException e) {}
                }
                long parseIntTime = System.nanoTime() - start;

                start = System.nanoTime();
                for (int i = 0; i < iterations; i++) {
                    StringUtils.parseDecimalInt(value, false);
                }
                long parseDecimalIntTime = System.nanoTime() - start;

                start = System.nanoTime();
                for (int i = 0; i < iterations; i++) {
                    try {
                        StringUtils.parseDecimalInt(value, true);
                    } catch (NumberFormatException e) {}
                }
                long parseDecimalIntExceptionTime = System.nanoTime() - start;

                if (iterations == TEST_SPEED) {
                    System.out.println("Results (no exceptions): " + parseDecimalIntTime + " ?<= " + parseIntTime);
                    assertTrue("StringUtils.parseDecimalInt() is slower: " + parseDecimalIntTime + " > " + parseIntTime, parseDecimalIntTime <= parseIntTime);
                    System.out.println("Results (exceptions)   : " + parseDecimalIntExceptionTime + " ?<= " + parseIntTime);
                    // Don't assert here because our version is much slower
                    // assertTrue("StringUtils.parseDecimalInt() is slower: " + parseDecimalIntExceptionTime + " > " + parseIntTime, parseDecimalIntExceptionTime <= parseIntTime);
                }
            }
        }
    }
}
