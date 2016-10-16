/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.config.PollOutagesConfig;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.mock.MockPersisterFactory;
import org.opennms.netmgt.poller.LocationAwarePollerClient;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.scheduler.Timer;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-mock.xml",
        "classpath:/META-INF/opennms/applicationContext-serviceMonitorRegistry.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-poller.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.netmgt.icmp.pingerClass=org.opennms.netmgt.icmp.jna.JnaPinger"
})
public class PollableServiceConfigTest {

    @Autowired
    private LocationAwarePollerClient m_locationAwarePollerClient;

    @Test
    public void testPollableServiceConfig() throws Exception {
        final FilterDao fd = mock(FilterDao.class);
        FilterDaoFactory.setInstance(fd);

        InputStream is = new FileInputStream(new File("src/test/resources/etc/psm-poller-configuration.xml"));
        PollerConfigFactory factory = new PollerConfigFactory(0, is, "localhost", false);
        PollerConfigFactory.setInstance(factory);        
        IOUtils.closeQuietly(is);

        PersisterFactory persisterFactory = new MockPersisterFactory();
        ResourceStorageDao resourceStorageDao = new FilesystemResourceStorageDao();

        final PollContext context = mock(PollContext.class);
        final PollableNetwork network = new PollableNetwork(context);
        final PollableNode node = network.createNodeIfNecessary(1, "foo", null);
        final PollableInterface iface = new PollableInterface(node, InetAddressUtils.addr("127.0.0.1"));
        final PollableService svc = new PollableService(iface, "MQ_API_DirectRte_v2");
        final PollOutagesConfig pollOutagesConfig = mock(PollOutagesConfig.class);
        final Package pkg = factory.getPackage("MapQuest");
        final Timer timer = mock(Timer.class);
        final PollableServiceConfig psc = new PollableServiceConfig(svc, factory, pollOutagesConfig, pkg, timer,
                persisterFactory, resourceStorageDao, m_locationAwarePollerClient);
        PollStatus pollStatus = psc.poll();
        assertThat(pollStatus.getReason(), not(containsString("Unexpected exception")));
    }
}
