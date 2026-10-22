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
package org.opennms.netmgt.collection.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.rrd.RrdRepository;

/**
 * Verifies that {@link CollectCommand} always builds a usable {@link RrdRepository}
 * when persisting: the step and heartbeat must be positive or rrdtool refuses to
 * create the files ("failed to parse data source 0:U:U: value must be positive").
 */
public class CollectCommandTest {

    private static final String OPENNMS_HOME = "opennms.home";

    private String previousOpennmsHome;

    private CollectCommand command;

    @Before
    public void setUp() {
        previousOpennmsHome = System.getProperty(OPENNMS_HOME);
        System.setProperty(OPENNMS_HOME, "/opt/opennms");
        command = new CollectCommand();
    }

    @After
    public void tearDown() {
        if (previousOpennmsHome == null) {
            System.clearProperty(OPENNMS_HOME);
        } else {
            System.setProperty(OPENNMS_HOME, previousOpennmsHome);
        }
    }

    @Test
    public void usesRepositoryFromCollectorConfiguration() {
        final RrdRepository configured = new RrdRepository();
        configured.setStep(60);
        configured.setHeartBeat(120);
        configured.setRraList(Arrays.asList("RRA:AVERAGE:0.5:1:100"));
        configured.setRrdBaseDir(new File("/data/rrd/jmx"));

        final ServiceCollector collector = mock(ServiceCollector.class);
        when(collector.getRrdRepository("mine")).thenReturn(configured);

        final ServiceParameters params = new ServiceParameters(Map.of("collection", "mine"));
        final RrdRepository repository = command.buildRrdRepository(collector, params);

        assertEquals(60, repository.getStep());
        assertEquals(120, repository.getHeartBeat());
        assertEquals(Arrays.asList("RRA:AVERAGE:0.5:1:100"), repository.getRraList());
        assertEquals(new File("/data/rrd/jmx"), repository.getRrdBaseDir());
    }

    @Test
    public void fallsBackToDefaultsWhenCollectorReturnsEmptyRepository() {
        // This is what the command used to do on its own: an RrdRepository with step 0
        // and heartbeat 0, which makes rrdtool fail when the files do not exist yet.
        final ServiceCollector collector = mock(ServiceCollector.class);
        when(collector.getRrdRepository(anyString())).thenReturn(new RrdRepository());

        final ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        final RrdRepository repository = command.buildRrdRepository(collector, params);

        assertEquals(CollectCommand.DEFAULT_STEP, repository.getStep());
        assertEquals(2 * CollectCommand.DEFAULT_STEP, repository.getHeartBeat());
        assertEquals(CollectCommand.DEFAULT_RRA, repository.getRraList());
        assertEquals(new File("/opt/opennms/share/rrd/snmp"), repository.getRrdBaseDir());
    }

    @Test
    public void fallsBackToDefaultsWhenCollectorReturnsNullOrThrows() {
        final ServiceCollector nullCollector = mock(ServiceCollector.class);
        when(nullCollector.getRrdRepository(anyString())).thenReturn(null);
        assertDefaults(command.buildRrdRepository(nullCollector, new ServiceParameters(Collections.emptyMap())));

        final ServiceCollector throwingCollector = mock(ServiceCollector.class);
        when(throwingCollector.getRrdRepository(anyString())).thenThrow(new IllegalStateException("not initialized"));
        assertDefaults(command.buildRrdRepository(throwingCollector, new ServiceParameters(Collections.emptyMap())));
    }

    @Test
    public void derivesHeartbeatFromStepWhenMissing() {
        final RrdRepository configured = new RrdRepository();
        configured.setStep(30);

        final ServiceCollector collector = mock(ServiceCollector.class);
        when(collector.getRrdRepository(anyString())).thenReturn(configured);

        final RrdRepository repository = command.buildRrdRepository(collector, new ServiceParameters(Collections.emptyMap()));

        assertEquals(30, repository.getStep());
        assertEquals(60, repository.getHeartBeat());
    }

    @Test
    public void commandLineRrasOverrideConfiguredRras() {
        final RrdRepository configured = new RrdRepository();
        configured.setStep(300);
        configured.setHeartBeat(600);
        configured.setRraList(Arrays.asList("RRA:AVERAGE:0.5:1:100"));

        final ServiceCollector collector = mock(ServiceCollector.class);
        when(collector.getRrdRepository(anyString())).thenReturn(configured);

        command.rras = Arrays.asList("RRA:MAX:0.5:1:10", "RRA:MIN:0.5:1:10");
        final RrdRepository repository = command.buildRrdRepository(collector, new ServiceParameters(Collections.emptyMap()));

        assertEquals(Arrays.asList("RRA:MAX:0.5:1:10", "RRA:MIN:0.5:1:10"), repository.getRraList());
    }

    private static void assertDefaults(final RrdRepository repository) {
        assertNotNull(repository);
        assertTrue(repository.getStep() > 0);
        assertTrue(repository.getHeartBeat() > 0);
        assertEquals(CollectCommand.DEFAULT_STEP, repository.getStep());
        assertEquals(2 * CollectCommand.DEFAULT_STEP, repository.getHeartBeat());
        assertEquals(CollectCommand.DEFAULT_RRA, repository.getRraList());
        assertEquals(new File("/opt/opennms/share/rrd/snmp"), repository.getRrdBaseDir());
    }
}
