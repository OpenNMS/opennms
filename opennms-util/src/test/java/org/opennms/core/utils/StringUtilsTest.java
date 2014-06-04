/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;

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
    
    private void testCreateCmdArray(String[] expected, String arg) {
        String[] actual = StringUtils.createCommandArray(arg, '@');
        assertArrayEquals(expected, actual);
    }
    
    private void assertArrayEquals(String[] expected, String[] actual) {
        assertEquals(Arrays.asList(expected), Arrays.asList(actual));
    }
}
