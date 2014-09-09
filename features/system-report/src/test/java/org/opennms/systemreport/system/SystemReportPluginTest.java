/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.systemreport.system;

import static org.junit.Assert.assertTrue;

import java.util.TreeMap;

import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.systemreport.SystemReportPlugin;
import org.springframework.core.io.ByteArrayResource;

public class SystemReportPluginTest {
    private SystemReportPlugin m_javaReportPlugin = new JavaReportPlugin();
    private SystemReportPlugin m_osReportPlugin = new OSReportPlugin();

    public SystemReportPluginTest() {
        MockLogAppender.setupLogging(false, "ERROR");
    }

    @Test
    public void testJavaReportPlugin() {
        final TreeMap<String, org.springframework.core.io.Resource> entries = m_javaReportPlugin.getEntries();
        final float classVer = Float.valueOf(getResourceText(entries.get("Class Version")));
        assertTrue(classVer >= 49.0);
    }

    @Test
    public void testOSPlugin() {
        final TreeMap<String, org.springframework.core.io.Resource> entries = m_osReportPlugin.getEntries();
        assertTrue(entries.containsKey("Architecture"));
        assertTrue(entries.containsKey("Name"));
        assertTrue(entries.containsKey("Distribution"));
    }
    
    private String getResourceText(final org.springframework.core.io.Resource r) {
        if (r instanceof ByteArrayResource) {
            return new String(((ByteArrayResource) r).getByteArray());
        }
        return "Not a string resource.";
    }
}
