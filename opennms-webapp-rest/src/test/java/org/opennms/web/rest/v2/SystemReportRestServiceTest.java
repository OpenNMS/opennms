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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
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

    private SystemReportFormatter streamingFormatter(final String name, final String ext, final String contentType) {
        final SystemReportFormatter f = formatter(name, name, ext, true);
        when(f.needsOutputStream()).thenReturn(true);
        when(f.getContentType()).thenReturn(contentType);
        return f;
    }

    private SystemReportRestService.GenerateRequest request(final String formatter, final List<String> plugins, final String output) {
        final SystemReportRestService.GenerateRequest r = new SystemReportRestService.GenerateRequest();
        r.setFormatter(formatter);
        r.setPlugins(plugins);
        r.setOutput(output);
        return r;
    }

    private int statusOf(final Runnable r) {
        try {
            r.run();
            fail("expected a WebApplicationException");
            return -1;
        } catch (final WebApplicationException e) {
            return e.getResponse().getStatus();
        }
    }

    @Test
    public void generateStreamsOnlySelectedPluginsInOrderWithAttachmentHeader() throws Exception {
        final SystemReportPlugin java = plugin("Java", "d", true);
        final SystemReportPlugin os = plugin("OS", "d", true);
        when(m_systemReport.getPlugins()).thenReturn(Arrays.asList(java, os));
        final SystemReportFormatter text = streamingFormatter("text", "txt", "text/plain");
        when(m_systemReport.getFormatters()).thenReturn(Collections.singletonList(text));

        final Response resp = m_service.generate(request("text", Collections.singletonList("Java"), null));

        assertEquals(200, resp.getStatus());
        assertEquals("attachment; filename=\"opennms-system-report.txt\"", resp.getHeaderString("Content-Disposition"));

        // the entity is a StreamingOutput; running it drives the formatter
        ((StreamingOutput) resp.getEntity()).write(new ByteArrayOutputStream());
        final InOrder order = inOrder(text);
        order.verify(text).begin();
        order.verify(text).write(java);
        order.verify(text).end();
        verify(text, never()).write(os);
    }

    @Test
    public void generateSanitizesTheRequestedFilenameToItsBasename() throws Exception {
        final SystemReportPlugin java = plugin("Java", "d", true);
        when(m_systemReport.getPlugins()).thenReturn(Collections.singletonList(java));
        final SystemReportFormatter text = streamingFormatter("text", "txt", "text/plain");
        when(m_systemReport.getFormatters()).thenReturn(Collections.singletonList(text));

        final Response resp = m_service.generate(request("text", Collections.singletonList("Java"), "/etc/my report.txt"));

        assertEquals("attachment; filename=\"myreport.txt\"", resp.getHeaderString("Content-Disposition"));
    }

    @Test
    public void generateRejectsUnknownFormatter() {
        when(m_systemReport.getFormatters()).thenReturn(Collections.emptyList());
        assertEquals(400, statusOf(() -> m_service.generate(request("nope", Collections.singletonList("Java"), null))));
    }

    @Test
    public void generateRejectsWhenNoPluginsSelected() {
        final SystemReportPlugin java = plugin("Java", "d", true);
        when(m_systemReport.getPlugins()).thenReturn(Collections.singletonList(java));
        final SystemReportFormatter text = streamingFormatter("text", "txt", "text/plain");
        when(m_systemReport.getFormatters()).thenReturn(Collections.singletonList(text));

        assertEquals(400, statusOf(() -> m_service.generate(request("text", Collections.emptyList(), null))));
    }

    @Test
    public void generateRejectsANonStreamingFormatter() {
        final SystemReportPlugin java = plugin("Java", "d", true);
        when(m_systemReport.getPlugins()).thenReturn(Collections.singletonList(java));
        // visible but does not stream (e.g. FTP upload): not downloadable
        final SystemReportFormatter ftp = formatter("ftp", "ftp", "ftp", true);
        when(ftp.needsOutputStream()).thenReturn(false);
        when(m_systemReport.getFormatters()).thenReturn(Collections.singletonList(ftp));

        assertEquals(400, statusOf(() -> m_service.generate(request("ftp", Collections.singletonList("Java"), null))));
    }
}
