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
        assertTrue(contents.contains("/sun.management.ThreadImpl.dumpAllThreads"));
    }
}
