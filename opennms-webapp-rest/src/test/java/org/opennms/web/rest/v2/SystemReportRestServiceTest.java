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
package org.opennms.web.rest.v2;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.systemreport.SystemReport;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;

public class SystemReportRestServiceTest {

    private SystemReport m_systemReport;
    private SystemReportRestService m_service;

    private SystemReportPlugin plugin(final String name, final String description, final boolean visible) {
        final SystemReportPlugin p = mock(SystemReportPlugin.class);
        when(p.getName()).thenReturn(name);
        when(p.getDescription()).thenReturn(description);
        when(p.isVisible()).thenReturn(visible);
        return p;
    }

    private SystemReportFormatter formatter(final String name, final String description, final String ext, final boolean visible) {
        final SystemReportFormatter f = mock(SystemReportFormatter.class);
        when(f.getName()).thenReturn(name);
        when(f.getDescription()).thenReturn(description);
        when(f.getExtension()).thenReturn(ext);
        when(f.isVisible()).thenReturn(visible);
        return f;
    }

    @Before
    public void setUp() {
        m_systemReport = mock(SystemReport.class);
        m_service = new SystemReportRestService();
        m_service.setSystemReport(m_systemReport);
    }

    @Test
    public void returnsOnlyVisiblePluginsMappedToDtos() {
        // build the mocks first; stubbing them inline inside the outer when() confuses Mockito
        final SystemReportPlugin java = plugin("Java", "Java and JVM information", true);
        final SystemReportPlugin hidden = plugin("Hidden", "should be filtered out", false);
        final SystemReportPlugin os = plugin("OS", "Kernel, OS, and Distribution", true);
        when(m_systemReport.getPlugins()).thenReturn(Arrays.asList(java, hidden, os));

        final List<SystemReportRestService.PluginDTO> result = m_service.getPlugins();

        assertEquals(2, result.size());
        assertEquals("Java", result.get(0).getName());
        assertEquals("Java and JVM information", result.get(0).getDescription());
        assertEquals("OS", result.get(1).getName());
    }

    @Test
    public void returnsOnlyVisibleFormattersWithExtension() {
        final SystemReportFormatter zip = formatter("zip", "Compressed file of all resources", "zip", true);
        final SystemReportFormatter ftp = formatter("ftp", "internal only", "ftp", false);
        final SystemReportFormatter text = formatter("text", "Human-readable text", "txt", true);
        when(m_systemReport.getFormatters()).thenReturn(Arrays.asList(zip, ftp, text));

        final List<SystemReportRestService.FormatterDTO> result = m_service.getFormatters();

        assertEquals(2, result.size());
        assertEquals("zip", result.get(0).getName());
        assertEquals("zip", result.get(0).getExtension());
        assertEquals("text", result.get(1).getName());
        assertEquals("txt", result.get(1).getExtension());
    }
}
