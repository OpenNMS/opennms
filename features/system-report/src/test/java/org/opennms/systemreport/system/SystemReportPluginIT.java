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

import java.util.Map;

import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.systemreport.SystemReportPlugin;
import org.springframework.core.io.ByteArrayResource;

public class SystemReportPluginIT {
    private SystemReportPlugin m_javaReportPlugin = new JavaReportPlugin();
    private SystemReportPlugin m_osReportPlugin = new OSReportPlugin();

    public SystemReportPluginIT() {
        MockLogAppender.setupLogging(false, "ERROR");
    }

    @Test
    public void testJavaReportPlugin() {
        final Map<String, org.springframework.core.io.Resource> entries = m_javaReportPlugin.getEntries();
        final float classVer = Float.valueOf(getResourceText(entries.get("Class Version")));
        assertTrue(classVer >= 49.0);
    }

    @Test
    public void testOSPlugin() {
        final Map<String, org.springframework.core.io.Resource> entries = m_osReportPlugin.getEntries();
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
