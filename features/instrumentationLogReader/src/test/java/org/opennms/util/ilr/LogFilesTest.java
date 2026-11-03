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
package org.opennms.util.ilr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LogFilesTest {

    @Rule
    public TemporaryFolder m_folder = new TemporaryFolder();

    @Test
    public void testIsInstrumentationLogFile() {
        assertTrue(Collector.isInstrumentationLogFile("instrumentation.log"));
        assertTrue(Collector.isInstrumentationLogFile("instrumentation.1.log"));
        assertTrue(Collector.isInstrumentationLogFile("instrumentation.12.log"));

        assertFalse(Collector.isInstrumentationLogFile(null));
        assertFalse(Collector.isInstrumentationLogFile("instrumentation.log.bak"));
        assertFalse(Collector.isInstrumentationLogFile("instrumentation-foo.log"));
        assertFalse(Collector.isInstrumentationLogFile("instrumentation.1.log.gz"));
        assertFalse(Collector.isInstrumentationLogFile("instrumentation..log"));
        assertFalse(Collector.isInstrumentationLogFile("instrumentation.a.log"));
        assertFalse(Collector.isInstrumentationLogFile("collectd.log"));
    }

    @Test
    public void testGetInstrumentationLogFilesOldestFirst() throws IOException {
        for (final String name : new String[] { "instrumentation.log", "instrumentation.1.log", "instrumentation.10.log",
                "instrumentation.2.log", "instrumentation.log.bak", "instrumentation-foo.log", "poller.log" }) {
            m_folder.newFile(name);
        }

        final List<String> names = new ArrayList<>();
        for (final File file : Collector.getInstrumentationLogFiles(m_folder.getRoot())) {
            names.add(file.getName());
        }
        assertEquals(List.of("instrumentation.10.log", "instrumentation.2.log", "instrumentation.1.log", "instrumentation.log"), names);
    }

    @Test
    public void testGetInstrumentationLogFilesMissingDirectory() {
        assertTrue(Collector.getInstrumentationLogFiles(new File(m_folder.getRoot(), "missing")).isEmpty());
    }
}
