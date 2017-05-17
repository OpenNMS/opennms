/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.pollables;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.collection.persistence.rrd.RrdPersisterFactory;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockPollerConfig;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.netmgt.rrd.RrdStrategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;

/**
 * Verifies that latency samples are properly persisted.
 *
 * @author jwhite
 */
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
        m_rrdStrategy = EasyMock.createMock(RrdStrategy.class);
        m_persisterFactory.setRrdStrategy(m_rrdStrategy);
    }

    @After
    public void tearDown() {
        // The persister may catch exception and log them as errors
        // Make sure we fail if this happens
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void canPersistsLatencySamples() throws Exception {
        PollStatus pollStatus = PollStatus.get(PollStatus.SERVICE_AVAILABLE, 42.1);
        // For the purposes of this test, it's important the attributes are not added in lexicographical order
        Map<String, Number> props = pollStatus.getProperties();
        props.put("ping1", Integer.valueOf(1));
        props.put("loss", Integer.valueOf(2));
        props.put("median", Integer.valueOf(3));
        ServiceMonitor serviceMonitor = new FixedServiceMonitor(pollStatus);

        Package pkg = new Package();

        MockNetwork mockNetwork = new MockNetwork();
        mockNetwork.createStandardNetwork();
        MockPollerConfig pollerConfig = new MockPollerConfig(mockNetwork);
        pollerConfig.setRRAList(pkg, Lists.newArrayList("RRA:AVERAGE:0.5:1:2016"));

        LatencyStoringServiceMonitorAdaptor lssma = new LatencyStoringServiceMonitorAdaptor(
                serviceMonitor, pollerConfig, pkg, m_persisterFactory, m_resourceStorageDao);

        MonitoredService monitoredService = new MockMonitoredService(3, "Firewall",
                InetAddress.getByName("192.168.1.5"), "SMTP");

        Map<String, Object> params = Maps.newHashMap();
        params.put("rrd-repository", getResponseTimeRoot().getAbsolutePath());
        params.put("rrd-base-name", "smtp-base");

        EasyMock.expect(m_rrdStrategy.getDefaultFileExtension()).andReturn(".jrb").atLeastOnce();

        m_rrdStrategy.createDefinition(EasyMock.eq("192.168.1.5"),
                EasyMock.eq(getResponseTimeRoot().toPath()
                .resolve(Paths.get("192.168.1.5")).toString()),
                EasyMock.eq("smtp-base"),
                EasyMock.anyInt(),
                EasyMock.anyObject(),
                EasyMock.anyObject());
        EasyMock.expectLastCall().andReturn(null).once();

        m_rrdStrategy.createFile(EasyMock.anyObject());
        EasyMock.expectLastCall().once();

        m_rrdStrategy.openFile(EasyMock.eq(getResponseTimeRoot().toPath()
                .resolve(Paths.get("192.168.1.5", "smtp-base.jrb")).toString()));
        EasyMock.expectLastCall().andReturn(null).once();

        // This is the important bit, the order of the values should match the order there were inserted above
        m_rrdStrategy.updateFile(EasyMock.isNull(), EasyMock.eq("192.168.1.5"), EasyMock.endsWith(":42.1:1:2:3"));
        EasyMock.expectLastCall().once();

        EasyMock.replay(m_rrdStrategy);

        // Trigger the poll
        lssma.poll(monitoredService, params);

        // Verify
        EasyMock.verify(m_rrdStrategy);
    }

    public File getResponseTimeRoot() {
        return new File(m_tempFolder.getRoot(), "response");
    }

    private static class FixedServiceMonitor implements ServiceMonitor {
        private final PollStatus m_pollStatus;

        public FixedServiceMonitor(PollStatus pollStatus) {
            m_pollStatus = pollStatus;
        }

        @Override
        public void initialize(Map<String, Object> parameters) {}

        @Override
        public void release() {}

        @Override
        public void initialize(MonitoredService svc) {}

        @Override
        public void release(MonitoredService svc) {}

        @Override
        public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
            return m_pollStatus;
        }
    }
}
