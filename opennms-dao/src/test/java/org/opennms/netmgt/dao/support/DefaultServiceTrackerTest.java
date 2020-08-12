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

package org.opennms.netmgt.dao.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.opennms.netmgt.events.api.EventConstants.NODE_GAINED_SERVICE_EVENT_UEI;
import static org.opennms.netmgt.events.api.EventConstants.NODE_LOST_SERVICE_EVENT_UEI;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.ServiceTracker;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.model.ImmutableEvent;
import org.opennms.netmgt.filter.api.FilterDao;

public class DefaultServiceTrackerTest implements ServiceTracker.NodeInterfaceUpdateListener {

    private static final String OPENCONFIG = "OpenConfig";
    private static final String ICMP = "ICMP";

    private DefaultServiceTracker serviceTracker;

    private List<ServiceTracker.NodeInterface> activeServices = new LinkedList<>();
    private int numServicesAdded;
    private int numServicesRemoved;

    @Before
    public void setUp() {
        serviceTracker = new DefaultServiceTracker();

        FilterDao filterDao = mock(FilterDao.class);
        serviceTracker.setFilterDao(filterDao);

        SessionUtils sessionUtils = new MockSessionUtils();
        serviceTracker.setSessionUtils(sessionUtils);

        IpInterfaceDao ipInterfaceDao = mock(IpInterfaceDao.class);
        serviceTracker.setIpInterfaceDao(ipInterfaceDao);
    }

    @Test
    public void canTrackService() {
        // There are no nodes in the database, we start a session, we expect no callbacks
        ServiceTracker.Session session = serviceTracker.watchServices(OPENCONFIG, this);
        assertThat(activeServices, hasSize(0));
        assertThat(numServicesAdded, equalTo(0));
        assertThat(numServicesRemoved, equalTo(0));

        // We add a service with the corresponding name, we expect a callback
        addService(1, InetAddressUtils.ONE_TWENTY_SEVEN, OPENCONFIG);

        // Verify
        assertThat(activeServices, hasSize(1));
        assertThat(numServicesAdded, equalTo(1));
        assertThat(numServicesRemoved, equalTo(0));

        // We add the same service again, we expect no callbacks
        addService(1, InetAddressUtils.ONE_TWENTY_SEVEN, OPENCONFIG);

        // Verify
        assertThat(activeServices, hasSize(1));
        assertThat(numServicesAdded, equalTo(1));
        assertThat(numServicesRemoved, equalTo(0));

        // We add a service that does not match the tracked name, we expect no callbacks
        addService(1, InetAddressUtils.ONE_TWENTY_SEVEN, ICMP);

        // Verify
        assertThat(activeServices, hasSize(1));
        assertThat(numServicesAdded, equalTo(1));
        assertThat(numServicesRemoved, equalTo(0));

        // We remove the original service, we expect a callback
        deleteService(1, InetAddressUtils.ONE_TWENTY_SEVEN, OPENCONFIG);

        // Verify
        assertThat(activeServices, hasSize(0));
        assertThat(numServicesAdded, equalTo(1));
        assertThat(numServicesRemoved, equalTo(1));

        // We remove the other service, we expect no callbacks
        deleteService(1, InetAddressUtils.ONE_TWENTY_SEVEN, ICMP);

        // Verify
        assertThat(activeServices, hasSize(0));
        assertThat(numServicesAdded, equalTo(1));
        assertThat(numServicesRemoved, equalTo(1));
    }


    @Test
    public void canFilterUsingFilterExpressions() {
        // There are no nodes in the database, we start a session, we expect no callbacks

        // We add a service that does not match the filter, we expect a callback

        // We add a category to the node, making it match the service, send an event out, expect a callback

        // We change the node is some other way, send event out, expect callback, but delay must be > than X
    }

    @Test
    public void canFilterUsingLambda() {

    }

    @Override
    public void onInterfaceMatchedFilter(ServiceTracker.NodeInterface iff) {
        activeServices.add(iff);
        numServicesAdded++;
    }

    @Override
    public void onInterfaceStoppedMatchingFilter(ServiceTracker.NodeInterface iff) {
        activeServices.remove(iff);
        numServicesRemoved++;
    }

    private void addService(int nodeId, InetAddress interfaceAddress, String serviceName) {
        serviceTracker.nodeGainedServiceHandler(ImmutableEvent.newBuilder()
                .setUei(NODE_GAINED_SERVICE_EVENT_UEI)
                .setSource("test")
                .setNodeid((long) nodeId)
                .setInterface(InetAddressUtils.str(interfaceAddress))
                .setService(serviceName)
                .build());
    }

    private void deleteService(int nodeId, InetAddress interfaceAddress, String serviceName) {
        serviceTracker.nodeLostServiceHandler(ImmutableEvent.newBuilder()
                .setUei(NODE_LOST_SERVICE_EVENT_UEI)
                .setSource("test")
                .setNodeid((long) nodeId)
                .setInterface(InetAddressUtils.str(interfaceAddress))
                .setService(serviceName)
                .build());
    }

    public static class MockSessionUtils implements SessionUtils {
        @Override
        public <V> V withTransaction(Supplier<V> supplier) {
            return supplier.get();
        }

        @Override
        public <V> V withReadOnlyTransaction(Supplier<V> supplier) {
            return supplier.get();
        }

        @Override
        public <V> V withManualFlush(Supplier<V> supplier) {
            return supplier.get();
        }
    }

}
