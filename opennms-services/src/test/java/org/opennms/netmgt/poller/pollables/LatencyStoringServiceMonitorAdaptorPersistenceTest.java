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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
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
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockPollerConfig;
import org.opennms.netmgt.mock.MockThresholdingService;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Verifies that latency samples are properly persisted.
 *
 * @author jwhite
 */
@Transactional
public class LatencyStoringServiceMonitorAdaptorPersistenceTest {

    @Rule
    public TemporaryFolder m_tempFolder = new TemporaryFolder();

    private RrdPersisterFactory m_persisterFactory;

    private FilesystemResourceStorageDao m_resourceStorageDao;
    private RrdStrategy<Object, Object> m_rrdStrategy;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        m_resourceStorageDao = new FilesystemResourceStorageDao();
        m_resourceStorageDao.setRrdDirectory(m_tempFolder.newFolder("response"));
        m_persisterFactory = new RrdPersisterFactory();
        m_persisterFactory.setResourceStorageDao(m_resourceStorageDao);
        m_rrdStrategy = mock(RrdStrategy.class);
        m_persisterFactory.setRrdStrategy(m_rrdStrategy);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(m_rrdStrategy);

        // The persister may catch exception and log them as errors
        // Make sure we fail if this happens
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void canPersistsLatencySamples() throws Exception {
        // No location - the path in the response time folder should be the IP address
        persistAndVerifyLatencySamples(null, Paths.get("192.168.1.5"));

        // Default location - the path in the response time folder should be the IP address
        persistAndVerifyLatencySamples(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, Paths.get("192.168.1.5"));

        // Non-default location - the path in the response time folder should be the location name, and then the IP address
        final String nonDefaultLocation = "not_" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID;
        persistAndVerifyLatencySamples(nonDefaultLocation, Paths.get(nonDefaultLocation, "192.168.1.5"));

        // Location with special characters
        final String someLocation = "TOG @ Pittsboro, NC";
        persistAndVerifyLatencySamples(someLocation, Paths.get("TOG___Pittsboro__NC", "192.168.1.5"));

        verify(m_rrdStrategy, atLeastOnce()).createDefinition(eq("192.168.1.5"), anyString(), anyString(), anyInt(), anyList(), anyList());
        verify(m_rrdStrategy, atLeastOnce()).createFile(any());
        verify(m_rrdStrategy, atLeastOnce()).getDefaultFileExtension();
        verify(m_rrdStrategy, atLeastOnce()).openFile(anyString());
        verify(m_rrdStrategy, atLeastOnce()).updateFile(any(), anyString(), anyString());
    }

    private void persistAndVerifyLatencySamples(String locationName, Path pathToResourceInResponseTime) throws Exception {
        PollStatus pollStatus = PollStatus.get(PollStatus.SERVICE_AVAILABLE, 42.1);
        // For the purposes of this test, it's important the attributes are not added in lexicographical order
        Map<String, Number> props = new LinkedHashMap<String,Number>(pollStatus.getProperties());
        props.put("ping1", Integer.valueOf(1));
        props.put("loss", Integer.valueOf(2));
        props.put("median", Integer.valueOf(3));
        pollStatus.setProperties(props);
        ServiceMonitor serviceMonitor = new FixedServiceMonitor(pollStatus);

        Package pkg = new Package();

        MockNetwork mockNetwork = new MockNetwork();
        mockNetwork.createStandardNetwork();
        MockPollerConfig pollerConfig = new MockPollerConfig(mockNetwork);
        pollerConfig.setRRAList(pkg, Lists.newArrayList("RRA:AVERAGE:0.5:1:2016"));

        LatencyStoringServiceMonitorAdaptor lssma = new LatencyStoringServiceMonitorAdaptor(
                pollerConfig, pkg, m_persisterFactory, new MockThresholdingService());

        MonitoredService monitoredService = new MockMonitoredService(3, "Firewall", locationName,
                InetAddress.getByName("192.168.1.5"), "SMTP");

        Map<String, Object> params = Maps.newHashMap();
        params.put("rrd-repository", getResponseTimeRoot().getAbsolutePath());
        params.put("rrd-base-name", "smtp-base");

        when(m_rrdStrategy.getDefaultFileExtension()).thenReturn(".jrb");

        when(m_rrdStrategy.createDefinition(eq("192.168.1.5"),
                eq(getResponseTimeRoot().toPath().resolve(pathToResourceInResponseTime).toString()),
                eq("smtp-base"),
                anyInt(),
                anyList(),
                anyList())).thenReturn(null);

        // verify(m_rrdStrategy, atLeastOnce()).createFile(anyObject());

        when(m_rrdStrategy.openFile(eq(getResponseTimeRoot().toPath()
                .resolve(pathToResourceInResponseTime.resolve("smtp-base.jrb")).toString()))).thenReturn(null);
        // verify(m_rrdStrategy, atLeastOnce()).openFile(anyString());

        // This is the important bit, the order of the values should match the order there were inserted above
        // verify(m_rrdStrategy, atLeastOnce()).updateFile(isNull(), eq("192.168.1.5"), endsWith(":42.1:1:2:3"));

        // Trigger the poll
        lssma.handlePollResult(monitoredService, params, serviceMonitor.poll(monitoredService, params));
    }

    public File getResponseTimeRoot() {
        return new File(m_tempFolder.getRoot(), "response");
    }

    private static class FixedServiceMonitor extends AbstractServiceMonitor {
        private final PollStatus m_pollStatus;

        public FixedServiceMonitor(PollStatus pollStatus) {
            m_pollStatus = pollStatus;
        }

        @Override
        public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
            return m_pollStatus;
        }
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

        final LatencyStoringServiceMonitorAdaptor latencyStoringServiceMonitorAdaptor = new LatencyStoringServiceMonitorAdaptor(pollerConfig, pkg, m_persisterFactory, new MockThresholdingService());

        final MonitoredService monitoredService = new MockMonitoredService(3, "Firewall", "Default",
                InetAddress.getByName("192.168.1.5"), svcName);

        when(m_rrdStrategy.getDefaultFileExtension()).thenReturn(".jrb");

        when(m_rrdStrategy.createDefinition(eq("192.168.1.5"),
                eq(getResponseTimeRoot().toPath().resolve("192.168.1.5").toString()),
                eq(expectedName),
                anyInt(),
                anyList(),
                anyList())).thenReturn(null);

        final Map<String, Object> parameters = new TreeMap<>();
        parameters.put("rrd-repository", getResponseTimeRoot().toString());

        if (rrdBaseName != null) {
            parameters.put("rrd-base-name", rrdBaseName);
        }

        if (dsName != null) {
            parameters.put("ds-name", dsName);
        }

        latencyStoringServiceMonitorAdaptor.handlePollResult(monitoredService, parameters, PollStatus.available(42.0));
        latencyStoringServiceMonitorAdaptor.handlePollResult(monitoredService, parameters, PollStatus.unavailable(""));
        latencyStoringServiceMonitorAdaptor.handlePollResult(monitoredService, parameters, PollStatus.unresponsive(""));
        latencyStoringServiceMonitorAdaptor.handlePollResult(monitoredService, parameters, PollStatus.unknown(""));

        verify(m_rrdStrategy, atLeastOnce()).getDefaultFileExtension();
        verify(m_rrdStrategy, atLeastOnce()).createDefinition(eq("192.168.1.5"),
                eq(getResponseTimeRoot().toPath().resolve("192.168.1.5").toString()),
                eq(expectedName),
                anyInt(),
                anyList(),
                isNull());

        verify(m_rrdStrategy, atLeastOnce()).createFile(anyObject());
        verify(m_rrdStrategy, atLeastOnce()).openFile(eq(getResponseTimeRoot().toPath().resolve("192.168.1.5").resolve(expectedName + ".jrb").toString()));

        clearInvocations(m_rrdStrategy);
    }
}
