/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2026 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2026 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.config.kscReports.Report;

public class KSC_PerformanceReportFactoryTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void shouldDecodeLegacyTitle() {
        assertEquals("Bob's \"A\" & <B>", KSC_PerformanceReportFactory.decodeLegacyTitle("Bob&#39;s &#34;A&#34; &amp; &lt;B&gt;"));
        assertEquals("&lt;", KSC_PerformanceReportFactory.decodeLegacyTitle("&amp;lt;"));
        assertEquals("Plain title", KSC_PerformanceReportFactory.decodeLegacyTitle("Plain title"));
        assertEquals("A & B", KSC_PerformanceReportFactory.decodeLegacyTitle("A & B"));
        assertNull(KSC_PerformanceReportFactory.decodeLegacyTitle(null));
    }

    @Test
    public void shouldDecodeLegacyTitlesOnLoad() throws Exception {
        // The stored titles are "A &amp; B" and "Bob&#39;s graph", as older versions of the web UI saved them.
        final File configFile = tempFolder.newFile("ksc-performance-reports.xml");
        Files.write(configFile.toPath(), ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<ReportsList>\n"
                + "  <Report id=\"1\" title=\"A &amp;amp; B\" show_timespan_button=\"true\" show_graphtype_button=\"true\" graphs_per_line=\"1\">\n"
                + "    <Graph title=\"Bob&amp;#39;s graph\" resourceId=\"node[1].interfaceSnmp[eth0]\" timespan=\"1_day\" graphtype=\"mib2.HCbits\"/>\n"
                + "  </Report>\n"
                + "</ReportsList>\n").getBytes(StandardCharsets.UTF_8));

        KSC_PerformanceReportFactory.setConfigFile(configFile);
        KSC_PerformanceReportFactory.init();
        KSC_PerformanceReportFactory.getInstance().reload();

        final Report report = KSC_PerformanceReportFactory.getInstance().getReportByIndex(1);
        assertEquals("A & B", report.getTitle());
        assertEquals("Bob's graph", report.getGraphs().get(0).getTitle());

        // The decoded titles are saved raw and do not change on the next load.
        KSC_PerformanceReportFactory.getInstance().saveCurrent();
        KSC_PerformanceReportFactory.getInstance().reload();

        final Report reloaded = KSC_PerformanceReportFactory.getInstance().getReportByIndex(1);
        assertEquals("A & B", reloaded.getTitle());
        assertEquals("Bob's graph", reloaded.getGraphs().get(0).getTitle());
    }
}
