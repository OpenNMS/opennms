/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.remotepollerng;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.poller.LocationAwarePollerClient;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.monitors.IcmpMonitor;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-testRemotePollerDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-shared.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class RemotePollerdIT implements InitializingBean {
    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private PollerConfig pollerConfig;

    @Autowired
    private LocationAwarePollerClient locationAwarePollerClient;

    @Autowired
    private PersisterFactory persisterFactory;

    @Autowired
    private EventForwarder eventForwarder;

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private MockEventIpcManager eventSubscriber;

    private RemotePollerd remotePollerd;

    @Override
    public void afterPropertiesSet() {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    @Transactional
    public void reportResultTest() {
        final OnmsMonitoredService onmsMonitoredService = this.databasePopulator.getMonitoredServiceDao().get(20);
        RemotePolledService remotePolledService = new RemotePolledService(onmsMonitoredService, new Package(), new Service(), new IcmpMonitor());
        Assert.assertEquals(0, this.databasePopulator.getLocationSpecificStatusDao().findAll().size());

        this.eventSubscriber.getEventAnticipator().reset();
        this.eventSubscriber.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI, "RemotePollerd").setNodeid(2).setInterface(InetAddressUtils.getInetAddress("192.168.2.2")).setService("ICMP").setParam("location", "RDU").getEvent());
        this.remotePollerd.reportResult("RDU", remotePolledService, PollStatus.available());
        Assert.assertEquals(1, this.databasePopulator.getLocationSpecificStatusDao().findAll().size());
        this.eventSubscriber.getEventAnticipator().verifyAnticipated();

        this.eventSubscriber.getEventAnticipator().reset();
        this.remotePollerd.reportResult("RDU", remotePolledService, PollStatus.available());
        Assert.assertEquals(1, this.databasePopulator.getLocationSpecificStatusDao().findAll().size());
        this.eventSubscriber.getEventAnticipator().verifyAnticipated();

        this.eventSubscriber.getEventAnticipator().reset();
        this.eventSubscriber.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.REMOTE_NODE_LOST_SERVICE_UEI, "RemotePollerd").setNodeid(2).setInterface(InetAddressUtils.getInetAddress("192.168.2.2")).setService("ICMP").setParam("location", "RDU").getEvent());
        this.remotePollerd.reportResult("RDU", remotePolledService, PollStatus.unavailable("old reason"));
        Assert.assertEquals(2, this.databasePopulator.getLocationSpecificStatusDao().findAll().size());
        this.eventSubscriber.getEventAnticipator().verifyAnticipated();

        this.eventSubscriber.getEventAnticipator().reset();
        this.remotePollerd.reportResult("RDU", remotePolledService, PollStatus.unavailable("old reason"));
        Assert.assertEquals(2, this.databasePopulator.getLocationSpecificStatusDao().findAll().size());
        this.eventSubscriber.getEventAnticipator().verifyAnticipated();

        this.eventSubscriber.getEventAnticipator().reset();
        this.remotePollerd.reportResult("RDU", remotePolledService, PollStatus.unavailable()); // reason is null
        Assert.assertEquals(2, this.databasePopulator.getLocationSpecificStatusDao().findAll().size());
        this.eventSubscriber.getEventAnticipator().verifyAnticipated();

        this.eventSubscriber.getEventAnticipator().reset();
        this.eventSubscriber.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.REMOTE_NODE_LOST_SERVICE_UEI, "RemotePollerd").setNodeid(2).setInterface(InetAddressUtils.getInetAddress("192.168.2.2")).setService("ICMP").setParam("location", "RDU").getEvent());
        this.remotePollerd.reportResult("RDU", remotePolledService, PollStatus.unavailable("new reason"));
        Assert.assertEquals(3, this.databasePopulator.getLocationSpecificStatusDao().findAll().size());
        this.eventSubscriber.getEventAnticipator().verifyAnticipated();
    }

    @Before
    public void setUp() {
        databasePopulator.populateDatabase();

        this.remotePollerd = new RemotePollerd(
                this.sessionUtils,
                this.databasePopulator.getMonitoringLocationDao(),
                this.pollerConfig,
                this.databasePopulator.getMonitoredServiceDao(),
                this.locationAwarePollerClient,
                this.databasePopulator.getLocationSpecificStatusDao(),
                this.persisterFactory,
                this.eventForwarder
        );
    }

    @After
    public void tearDown() {
        databasePopulator.resetDatabase();
    }
}
