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
package org.opennms.systemreport.formatters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.springframework.core.io.ByteArrayResource;

/** The registered formatters are singletons, so each must produce a complete report every time it is reused. */
public class FormatterReuseTest {

    private static SystemReportPlugin plugin() {
        final SystemReportPlugin p = mock(SystemReportPlugin.class);
        when(p.getName()).thenReturn("Java");
        when(p.getDescription()).thenReturn("Java and JVM information");
        when(p.defaultFormat()).thenReturn(".txt");
        when(p.getEntries()).thenReturn(Map.of("Version", new ByteArrayResource("21".getBytes())));
        return p;
    }

    private static byte[] run(final SystemReportFormatter formatter) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        formatter.setOutputStream(out);
        formatter.begin();
        formatter.write(plugin());
        formatter.end();
        return out.toByteArray();
    }

    private static List<String> zipEntries(final byte[] zip) throws Exception {
        final List<String> names = new ArrayList<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (ZipEntry e = in.getNextEntry(); e != null; e = in.getNextEntry()) {
                names.add(e.getName());
            }
        }
        return names;
    }

    @Test
    public void zipFormatterProducesTheSameEntriesOnReuse() throws Exception {
        final ZipSystemReportFormatter zip = new ZipSystemReportFormatter();
        final List<String> first = zipEntries(run(zip));
        assertTrue(first.toString(), first.contains("opennms-system-report//"));
        assertEquals(first, zipEntries(run(zip)));
    }

    @Test
    public void xmlFormatterWritesEachReportToItsOwnStream() {
        final XmlSystemReportFormatter xml = new XmlSystemReportFormatter();
        final String first = new String(run(xml));
        final String second = new String(run(xml));
        assertTrue(first, first.contains("<systemReportPlugins>"));
        assertEquals(first, second);
    }
}
