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
package org.opennms.netmgt.graph.provider.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.opennms.netmgt.graph.provider.application.ApplicationVertex.createVertexId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.mock.MockSessionUtils;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.domain.simple.SimpleDomainEdge;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;

public class ApplicationGraphProviderTest {

    @Test
    public void shouldCreateProperGraph() {
        final List<OnmsApplication> applications = generateApplications();
        final ApplicationDao dao = Mockito.mock(ApplicationDao.class);
        when(dao.findAll()).thenReturn(applications);

        final ApplicationGraphProvider provider = new ApplicationGraphProvider(new MockSessionUtils(), dao);
        final ImmutableGraph<ApplicationVertex, SimpleDomainEdge> graph = provider.loadGraph();
        assertEquals(30, graph.getVertices().size());
        assertEquals(25, graph.getEdges().size());

        for(OnmsApplication app: applications) {
            for(OnmsMonitoredService service : app.getMonitoredServices()) {
                verifyLinkingBetweenNodes(graph, graph.getVertex(createVertexId(app)), graph.getVertex(createVertexId(service)));
            }
        }
    }

    private List<OnmsApplication> generateApplications() {
        final List<OnmsApplication> applications = new ArrayList<>();
        for(int i = 0 ; i < 5; i++){
            applications.add(generateApplication(i + 1));
        }
        return applications;
    }

    private OnmsApplication generateApplication(final int applicationId) {
        final OnmsApplication app = new OnmsApplication();
        app.setId(applicationId);
        app.setName("Application " + applicationId);
        final Set<OnmsMonitoredService> monitoredServices = new HashSet<>();
        monitoredServices.add(generateMonitoredService(applicationId * 10 + 1, new OnmsServiceType(1, "ICMP")));
        monitoredServices.add(generateMonitoredService(applicationId * 10 + 2, new OnmsServiceType(2,"HTTP")));
        monitoredServices.add(generateMonitoredService(applicationId * 10 + 3, new OnmsServiceType(3,"HTTPS")));
        monitoredServices.add(generateMonitoredService(applicationId * 10 + 4, new OnmsServiceType(4,"SNMP")));
        monitoredServices.add(generateMonitoredService(applicationId * 10 + 5, new OnmsServiceType(5,"SSH")));
        app.setMonitoredServices(monitoredServices);
        return app;
    }

    private OnmsMonitoredService generateMonitoredService(int serviceId, OnmsServiceType serviceType) {
        final OnmsMonitoredService service = new OnmsMonitoredService();
        service.setId(serviceId);
        service.setQualifier("Service " + serviceId);
        service.setServiceType(new OnmsServiceType(UUID.randomUUID().toString()));
        service.setServiceType(serviceType);
        final OnmsIpInterface ipInterface = new OnmsIpInterface();
        ipInterface.setIpAddress(InetAddressUtils.addr("127.0.0.1"));
        ipInterface.addMonitoredService(service);
        final OnmsNode node = new OnmsNode();
        node.setId(123);
        node.addIpInterface(ipInterface);
        service.setIpInterface(ipInterface);
        return service;
    }

    private static void verifyLinkingBetweenNodes(ImmutableGraph<ApplicationVertex, SimpleDomainEdge>  graph, ApplicationVertex left, ApplicationVertex right) {
        final Collection<SimpleDomainEdge> edgesLeft = graph.getConnectingEdges(left);
        final Collection<SimpleDomainEdge> edgesRight = graph.getConnectingEdges(right);

        // 1. get the EdgeRef that connects the 2 vertices
        final Set<SimpleDomainEdge> intersection = intersect(edgesLeft, edgesRight);
        assertEquals(1, intersection.size());
        SimpleDomainEdge edge = intersection.iterator().next();

        // 2. get the Edge and check if it really connects the 2 Vertices
        // we don't know the direction it is connected so we have to test both ways:
        assertTrue(
                (graph.resolveVertex(edge.getSource()).equals(left) || graph.resolveVertex(edge.getSource()).equals(right)) // source side
                        && (graph.resolveVertex(edge.getTarget()).equals(left) || graph.resolveVertex(edge.getTarget()).equals(right)) // target side
                        && !graph.resolveVertex(edge.getSource()).equals(graph.resolveVertex(edge.getTarget()))); // make sure it doesn't connect the same node
    }

    /**
     * Gives back the intersection between the 2 collections, as in:
     * - only elements that are contained in both collections will be retained
     * - double elements are removed
     */
    private static <E> Set<E> intersect(final Collection<E> left, final Collection<E> right){
        Set<E> set = new HashSet<>(left);
        set.retainAll(new HashSet<>(right));
        return set;
    }
}
