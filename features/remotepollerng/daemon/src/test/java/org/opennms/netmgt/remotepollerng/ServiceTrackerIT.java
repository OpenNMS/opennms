/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.AnnotationBasedEventListenerAdapter;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-testRemotePollerDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-shared.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-thresholding.xml",
        "classpath:/META-INF/opennms/applicationContext-testPostgresBlobStore.xml",
        "classpath:/META-INF/opennms/applicationContext-testThresholdingDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-utils.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
@Ignore
public class ServiceTrackerIT {

//    @Autowired
//    private MockEventIpcManager eventIpcManager;
//
//    @Autowired
//    private DatabasePopulator databasePopulator;
//
//    private static Callback create(final EventSubscriptionService eventSubscriptionService) {
//        final Callback callback = new Callback();
//
//        final ServiceTracker serviceTracker = new ServiceTracker(
//                PollerConfigFactory.getInstance(),
//                new ServiceTracker.QueryManager() {
//                    @Override
//                    public List<ServiceTracker.Service> getNodeServices(int nodeId) {
//                        return null;
//                    }
//                },
//                (service) -> true,
//                callback);
//
//        new AnnotationBasedEventListenerAdapter(serviceTracker, eventSubscriptionService);
//
//        return callback;
//    }
//
//    @BeforeTransaction
//    public void beforeTransaction() throws Exception {
//        this.databasePopulator.populateDatabase();
//    }
//
//    @AfterTransaction
//    public void afterTransaction() throws Exception {
//        this.databasePopulator.resetDatabase();
//    }
//
//    @Test
//    @Transactional
//    public void testNodeGainedService() throws Exception {
//        final Callback callback = create(this.eventIpcManager);
//
//        this.eventIpcManager.send(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "test")
//                                          .setNodeid(this.databasePopulator.getNode1().getId())
//                                          .setInterface(this.databasePopulator.getNode1().getPrimaryInterface().getIpAddress())
//                                          .setService("ICMP")
//                                          .setParam("location", this.databasePopulator.getNode1().getLocation().getLocationName())
//                                          .getEvent());
//
//        assertThat(callback.getAdded(), contains(
//                pojo(ServiceTracker.Service.class)
//                        .where(ServiceTracker.Service::getNodeId, is(1))
//                        .where(ServiceTracker.Service::getIpAddress, is("192.168.1.1"))
//                        .where(ServiceTracker.Service::getServiceName, is("ICMP"))));
//    }
//
//    private static class Callback implements ServiceTracker.Callback {
//        private final List<ServiceTracker.Service> added = Lists.newArrayList();
//        private final List<ServiceTracker.Service> removed = Lists.newArrayList();
//
//        private final Set<ServiceTracker.Service> active = Sets.newHashSet();
//
//        @Override
//        public void add(final ServiceTracker.Service service) {
//            this.added.add(service);
//            this.active.add(service);
//        }
//
//        @Override
//        public void remove(final ServiceTracker.Service service) {
//            this.removed.add(service);
//            this.active.remove(service);
//        }
//
//        public List<ServiceTracker.Service> getAdded() {
//            return this.added;
//        }
//
//        public List<ServiceTracker.Service> getRemoved() {
//            return this.removed;
//        }
//
//        public Set<ServiceTracker.Service> getActive() {
//            return this.active;
//        }
//    }
}
