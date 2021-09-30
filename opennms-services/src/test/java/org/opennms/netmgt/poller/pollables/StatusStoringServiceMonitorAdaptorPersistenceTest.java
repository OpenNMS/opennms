/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.nio.file.Path;

import org.easymock.EasyMock;
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
        this.rrdStrategy = EasyMock.createMock(RrdStrategy.class);
        this.persisterFactory.setRrdStrategy(this.rrdStrategy);
    }

    @After
    public void tearDown() {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void canPersistState() throws Exception {
        final Package pkg = new Package();

        final MockNetwork mockNetwork = new MockNetwork().createStandardNetwork();
        final MockPollerConfig pollerConfig = new MockPollerConfig(mockNetwork);

        final StatusStoringServiceMonitorAdaptor sssma = new StatusStoringServiceMonitorAdaptor(pollerConfig, pkg, persisterFactory);

        final MonitoredService monitoredService = new MockMonitoredService(3, "Firewall", "Default",
                                                                           InetAddress.getByName("192.168.1.5"), "SMTP");

        this.rrdStrategy.getDefaultFileExtension();
        EasyMock.expectLastCall().andReturn(".jrb").atLeastOnce();

        this.rrdStrategy.createDefinition(EasyMock.eq("192.168.1.5"),
                                                     EasyMock.eq(getStatusRoot().resolve("192.168.1.5").toString()),
                                                     EasyMock.eq("smtp-base"),
                                                     EasyMock.anyInt(),
                                                     EasyMock.anyObject(),
                                                     EasyMock.anyObject());
        EasyMock.expectLastCall().andReturn(null).atLeastOnce();

        this.rrdStrategy.createFile(EasyMock.anyObject());
        EasyMock.expectLastCall().atLeastOnce();

        this.rrdStrategy.openFile(EasyMock.eq(getStatusRoot().resolve("192.168.1.5").resolve("smtp-base.jrb").toString()));
        EasyMock.expectLastCall().andReturn(null).atLeastOnce();

        this.rrdStrategy.updateFile(EasyMock.isNull(), EasyMock.eq("192.168.1.5"), EasyMock.endsWith(":1"));
        EasyMock.expectLastCall().once();

        this.rrdStrategy.updateFile(EasyMock.isNull(), EasyMock.eq("192.168.1.5"), EasyMock.endsWith(":-1"));
        EasyMock.expectLastCall().once();

        this.rrdStrategy.updateFile(EasyMock.isNull(), EasyMock.eq("192.168.1.5"), EasyMock.endsWith(":U"));
        EasyMock.expectLastCall().once();

        this.rrdStrategy.updateFile(EasyMock.isNull(), EasyMock.eq("192.168.1.5"), EasyMock.endsWith(":0"));
        EasyMock.expectLastCall().once();

        EasyMock.replay(this.rrdStrategy);

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

        EasyMock.verify(this.rrdStrategy);
        EasyMock.reset(this.rrdStrategy);
    }

    @Test
    public void doesNothingWhenDisabled() throws Exception {
        final Package pkg = new Package();

        final MockNetwork mockNetwork = new MockNetwork().createStandardNetwork();
        final MockPollerConfig pollerConfig = new MockPollerConfig(mockNetwork);

        final StatusStoringServiceMonitorAdaptor sssma = new StatusStoringServiceMonitorAdaptor(pollerConfig, pkg, persisterFactory);

        final MonitoredService monitoredService = new MockMonitoredService(3, "Firewall", "Default",
                                                                           InetAddress.getByName("192.168.1.5"), "SMTP");

        EasyMock.replay(this.rrdStrategy);

        sssma.handlePollResult(monitoredService, Maps.newHashMap(ImmutableMap.<String, Object>builder()
                                                             .put("rrd-repository", getStatusRoot().toString())
                                                             .put("rrd-base-name", "smtp-base")
                                                             .build()), PollStatus.available(42.0));

        EasyMock.verify(this.rrdStrategy);
        EasyMock.reset(this.rrdStrategy);
    }

    public Path getStatusRoot() {
        System.err.println(tempFolder.getRoot().toPath().resolve("status").toAbsolutePath());
        return tempFolder.getRoot().toPath().resolve("status").toAbsolutePath();
    }
}
