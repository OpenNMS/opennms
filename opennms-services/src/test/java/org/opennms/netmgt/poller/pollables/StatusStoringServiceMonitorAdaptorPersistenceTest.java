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
package org.opennms.netmgt.poller.pollables;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.collection.persistence.rrd.RrdPersisterFactory;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockPollerConfig;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Verifies that latency samples are properly persisted.
 *
 * @author jwhite
 */
@Transactional
public class StatusStoringServiceMonitorAdaptorPersistenceTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private RrdPersisterFactory persisterFactory;

    private FilesystemResourceStorageDao resourceStorageDao;
    private RrdStrategy<Object, Object> rrdStrategy;

    @Before
    public void setUp() throws Exception {
        System.setProperty("rrd.base.dir", tempFolder.getRoot().getAbsolutePath());

        MockLogAppender.setupLogging();
        this.resourceStorageDao = new FilesystemResourceStorageDao();
        this.resourceStorageDao.setRrdDirectory(this.tempFolder.newFolder("status"));
        this.persisterFactory = new RrdPersisterFactory();
        this.persisterFactory.setResourceStorageDao(this.resourceStorageDao);
        this.rrdStrategy = mock(RrdStrategy.class);
        this.persisterFactory.setRrdStrategy(this.rrdStrategy);
    }

    @After
    public void tearDown() {
        MockLogAppender.assertNoWarningsOrGreater();
        verifyNoMoreInteractions(rrdStrategy);
    }

    @Test
    public void canPersistState() throws Exception {
        final Package pkg = new Package();

        final MockNetwork mockNetwork = new MockNetwork().createStandardNetwork();
        final MockPollerConfig pollerConfig = new MockPollerConfig(mockNetwork);

        final StatusStoringServiceMonitorAdaptor sssma = new StatusStoringServiceMonitorAdaptor(pollerConfig, pkg, persisterFactory);

        final MonitoredService monitoredService = new MockMonitoredService(3, "Firewall", "Default",
                                                                           InetAddress.getByName("192.168.1.5"), "SMTP");

        when(this.rrdStrategy.getDefaultFileExtension()).thenReturn(".jrb");

        when(this.rrdStrategy.createDefinition(eq("192.168.1.5"),
                                                     eq(getStatusRoot().resolve("192.168.1.5").toString()),
                                                     eq("smtp-base"),
                                                     anyInt(),
                                                     anyList(),
                                                     anyList())).thenReturn(null);

        sssma.handlePollResult(monitoredService, Maps.newHashMap(ImmutableMap.<String, Object>builder()
                                                             .put("rrd-repository", getStatusRoot().toString())
                                                             .put("rrd-base-name", "smtp-base")
                                                             .put("rrd-status", "true")
                                                             .build()), PollStatus.available(42.0));

        sssma.handlePollResult(monitoredService, Maps.newHashMap(ImmutableMap.<String, Object>builder()
                                                                             .put("rrd-repository", getStatusRoot().toString())
                                                                             .put("rrd-base-name", "smtp-base")
                                                                             .put("rrd-status", "true")
                                                                             .build()), PollStatus.unavailable(""));

        sssma.handlePollResult(monitoredService, Maps.newHashMap(ImmutableMap.<String, Object>builder()
                                                                             .put("rrd-repository", getStatusRoot().toString())
                                                                             .put("rrd-base-name", "smtp-base")
                                                                             .put("rrd-status", "true")
                                                                             .build()), PollStatus.unresponsive(""));

        sssma.handlePollResult(monitoredService, Maps.newHashMap(ImmutableMap.<String, Object>builder()
                                                                             .put("rrd-repository", getStatusRoot().toString())
                                                                             .put("rrd-base-name", "smtp-base")
                                                                             .put("rrd-status", "true")
                                                                             .build()), PollStatus.unknown(""));

        verify(this.rrdStrategy, atLeastOnce()).getDefaultFileExtension();
        verify(this.rrdStrategy, atLeastOnce()).createDefinition(eq("192.168.1.5"),
                                                                 eq(getStatusRoot().resolve("192.168.1.5").toString()),
                                                                 eq("smtp-base"),
                                                                 anyInt(),
                                                                 anyList(),
                                                                 isNull());

        verify(this.rrdStrategy, atLeastOnce()).createFile(anyObject());
        verify(this.rrdStrategy, atLeastOnce()).openFile(eq(getStatusRoot().resolve("192.168.1.5").resolve("smtp-base.jrb").toString()));
        verify(this.rrdStrategy, atLeastOnce()).updateFile(isNull(), eq("192.168.1.5"), endsWith(":1"));
        verify(this.rrdStrategy, atLeastOnce()).updateFile(isNull(), eq("192.168.1.5"), endsWith(":-1"));
        verify(this.rrdStrategy, atLeastOnce()).updateFile(isNull(), eq("192.168.1.5"), endsWith(":U"));
        verify(this.rrdStrategy, times(1)).updateFile(isNull(), eq("192.168.1.5"), endsWith(":0"));
    }

    @Test
    public void doesNothingWhenDisabled() throws Exception {
        final Package pkg = new Package();

        final MockNetwork mockNetwork = new MockNetwork().createStandardNetwork();
        final MockPollerConfig pollerConfig = new MockPollerConfig(mockNetwork);

        final StatusStoringServiceMonitorAdaptor sssma = new StatusStoringServiceMonitorAdaptor(pollerConfig, pkg, persisterFactory);

        final MonitoredService monitoredService = new MockMonitoredService(3, "Firewall", "Default",
                                                                           InetAddress.getByName("192.168.1.5"), "SMTP");

        sssma.handlePollResult(monitoredService, Maps.newHashMap(ImmutableMap.<String, Object>builder()
                                                             .put("rrd-repository", getStatusRoot().toString())
                                                             .put("rrd-base-name", "smtp-base")
                                                             .put("rrd-status", "false")
                                                             .build()), PollStatus.available(42.0));
    }

    public Path getStatusRoot() {
        return tempFolder.getRoot().toPath().resolve("status").toAbsolutePath();
    }

    @Test
    public void test_NMS15820() throws Exception {
        testParameters("foo", null, "My-Custom-Service", "foo");
        testParameters("foo", "bar", "My-Custom-Service", "foo");
        testParameters(null, "bar", "My-Custom-Service", "bar");
        testParameters(null, null, "My-Custom-Service", "my-custom-service");
    }

    private void testParameters(final String rrdBaseName, final String dsName, final String svcName, final String expectedName) throws Exception {
        final Package pkg = new Package();

        final MockNetwork mockNetwork = new MockNetwork().createStandardNetwork();
        final MockPollerConfig pollerConfig = new MockPollerConfig(mockNetwork);

        final StatusStoringServiceMonitorAdaptor statusStoringServiceMonitorAdaptor = new StatusStoringServiceMonitorAdaptor(pollerConfig, pkg, persisterFactory);

        final MonitoredService monitoredService = new MockMonitoredService(3, "Firewall", "Default",
                InetAddress.getByName("192.168.1.5"), svcName);

        when(this.rrdStrategy.getDefaultFileExtension()).thenReturn(".jrb");

        when(this.rrdStrategy.createDefinition(eq("192.168.1.5"),
                eq(getStatusRoot().resolve("192.168.1.5").toString()),
                eq(expectedName),
                anyInt(),
                anyList(),
                anyList())).thenReturn(null);

        final Map<String, Object> parameters = new TreeMap<>();
        parameters.put("rrd-repository", getStatusRoot().toString());

        if (rrdBaseName != null) {
            parameters.put("rrd-base-name", rrdBaseName);
        }

        if (dsName != null) {
            parameters.put("ds-name", dsName);
        }

        statusStoringServiceMonitorAdaptor.handlePollResult(monitoredService, parameters, PollStatus.available(42.0));
        statusStoringServiceMonitorAdaptor.handlePollResult(monitoredService, parameters, PollStatus.unavailable(""));
        statusStoringServiceMonitorAdaptor.handlePollResult(monitoredService, parameters, PollStatus.unresponsive(""));
        statusStoringServiceMonitorAdaptor.handlePollResult(monitoredService, parameters, PollStatus.unknown(""));

        verify(this.rrdStrategy, atLeastOnce()).getDefaultFileExtension();
        verify(this.rrdStrategy, atLeastOnce()).createDefinition(eq("192.168.1.5"),
                eq(getStatusRoot().resolve("192.168.1.5").toString()),
                eq(expectedName),
                anyInt(),
                anyList(),
                isNull());

        verify(this.rrdStrategy, atLeastOnce()).createFile(anyObject());
        verify(this.rrdStrategy, atLeastOnce()).openFile(eq(getStatusRoot().resolve("192.168.1.5").resolve(expectedName + ".jrb").toString()));

        clearInvocations(this.rrdStrategy);
    }
}
