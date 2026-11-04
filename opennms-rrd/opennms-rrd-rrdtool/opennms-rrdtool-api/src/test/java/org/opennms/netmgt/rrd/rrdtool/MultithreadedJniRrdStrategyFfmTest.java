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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.rrd.RrdAttributeType;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.jrrd2.api.JRrd2;

/**
 * Runs the default RRD strategy end-to-end on the FFM librrd binding,
 * i.e. without libjrrd2 installed.
 */
public class MultithreadedJniRrdStrategyFfmTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private String previousImpl;

    @Before
    public void setUp() {
        previousImpl = System.getProperty(JRrd2Provider.IMPL_PROPERTY);
        System.setProperty(JRrd2Provider.IMPL_PROPERTY, "ffm");
        try {
            JRrd2Provider.newInstance();
        } catch (final Throwable t) {
            Assume.assumeNoException("FFM librrd binding unavailable (needs Java 22+ and librrd): skipping", t);
        }
    }

    @After
    public void tearDown() {
        if (previousImpl == null) {
            System.clearProperty(JRrd2Provider.IMPL_PROPERTY);
        } else {
            System.setProperty(JRrd2Provider.IMPL_PROPERTY, previousImpl);
        }
    }

    @Test
    public void providerReturnsFfmImplementation() {
        final JRrd2 jrrd2 = JRrd2Provider.newInstance();
        assertEquals(JRrd2Provider.FFM_CLASS, jrrd2.getClass().getName());
    }

    @Test
    public void autoModePrefersFfm() {
        System.setProperty(JRrd2Provider.IMPL_PROPERTY, "auto");
        assertEquals(JRrd2Provider.FFM_CLASS, JRrd2Provider.newInstance().getClass().getName());
    }

    @Test
    public void strategyCanCreateUpdateAndFetch() throws Exception {
        final MultithreadedJniRrdStrategy strategy = new MultithreadedJniRrdStrategy();

        final MultithreadedJniRrdStrategy.CreateCommand create = strategy.createDefinition("test",
                tempFolder.getRoot().getAbsolutePath(), "ffm", 1,
                List.of(new RrdDataSource("x", RrdAttributeType.GAUGE, 10, "U", "U")),
                List.of("RRA:AVERAGE:0.5:1:120"));
        assertNotNull(create);
        strategy.createFile(create);

        final String file = new File(tempFolder.getRoot(), "ffm" + strategy.getDefaultFileExtension()).getAbsolutePath();
        assertTrue(new File(file).exists());

        final long now = System.currentTimeMillis() / 1000L;
        final MultithreadedJniRrdStrategy.UpdateCommand update = strategy.openFile(file);
        for (long t = now - 8; t <= now - 2; t++) {
            strategy.updateFile(update, "test", t + ":42");
        }
        strategy.closeFile(update);

        final Double value = strategy.fetchLastValueInRange(file, "x", 1000, 10_000);
        assertNotNull(value);
        assertEquals(42.0, value, 0.0001);
    }
}
