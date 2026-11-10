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
package org.opennms.netmgt.rrd.rrdtool;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.opennms.core.test.MockLogAppender;
import org.springframework.util.StringUtils;

/**
 * Unit tests for the JniRrdStrategy.  This requires that the shared object
 * for JNI rrdtool support can be found and linked (see findJrrdLibrary).
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class JniRrdStrategyTest {
    
    private JniRrdStrategy m_strategy;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        String rrdLib = System.getProperty("opennms.library.jrrd");
        if (rrdLib != null && !rrdLib.equals("${opennms.library.jrrd}")) {
            File libFile = new File(rrdLib);
            if (libFile.exists()) {
                m_strategy = new JniRrdStrategy();
            } else {
                System.err.println("System property 'opennms.library.jrrd' points to non-existent file: skipping tests");
            }
        } else {
            System.err.println("System property 'opennms.library.jrrd' not set: skipping tests");
        }
    }

    @Test
    public void testInitialize() {
    }

    @Test
    public void testGraph() throws Exception {
        if (m_strategy != null) {
            String rrdtoolBin = System.getProperty("install.rrdtool.bin");
            if (rrdtoolBin != null) {
                File rrdtoolFile = new File(rrdtoolBin);
                if (!rrdtoolFile.exists()) {
                    System.err.println(rrdtoolBin + " does not exist");
                    return;
                }
            } else {
                System.err.println("System property 'install.rrdtool.bin' not set: skipping test");
                return;
            }

            long end = System.currentTimeMillis();
            long start = end - (24 * 60 * 60 * 1000);
            String[] command = new String[] {
                    rrdtoolBin,
                    "graph", 
                    "-",
                    "--start=" + start,
                    "--end=" + end,
                    "COMMENT:test"
            };
            
            m_strategy.createGraph(StringUtils.arrayToDelimitedString(command, " "), (new File(rrdtoolBin)).getParentFile());
        }
    }
}
