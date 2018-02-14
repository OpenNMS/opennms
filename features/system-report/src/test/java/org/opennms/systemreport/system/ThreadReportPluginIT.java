/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.systemreport.system;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.systemreport.SystemReportPlugin;

public class ThreadReportPluginIT extends ReportPluginITCase {
    @Resource(name="threadReportPlugin")
    private SystemReportPlugin m_threadReportPlugin;

    public ThreadReportPluginIT() {
        MockLogAppender.setupLogging(false, "ERROR");
    }

    @Test
    public void testThreadReportPlugin() throws IOException {
        assertTrue(listContains(ThreadReportPlugin.class));
        final Map<String, org.springframework.core.io.Resource> entries = m_threadReportPlugin.getEntries();
        final org.springframework.core.io.Resource resource = entries.get("ThreadDump.txt");
        final String contents = IOUtils.toString(resource.getInputStream());
        assertTrue(contents.contains("at sun.management.ThreadImpl.dumpAllThreads"));
    }
}
