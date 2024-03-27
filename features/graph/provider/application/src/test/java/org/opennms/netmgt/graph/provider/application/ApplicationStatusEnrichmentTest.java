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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentGraphBuilder;
import org.opennms.netmgt.graph.api.info.StatusInfo;
import org.opennms.netmgt.graph.domain.simple.SimpleDomainEdge;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;

public class ApplicationStatusEnrichmentTest {

    private final OnmsNode node = new OnmsNode();
    private int idCounter = 1;

    ApplicationGraph.ApplicationGraphBuilder graph;
    Map<String, StatusInfo> statusByServiceMap;
    ApplicationVertex applicationVertex;

    @Before
    public void setUp() {
        statusByServiceMap = new HashMap<>();
        node.setId(idCounter++);
        graph = ApplicationGraph.builder();

        OnmsApplication application = new OnmsApplication();
        application.setId(idCounter++);
        application.setName("myApp");

        applicationVertex = ApplicationVertex.builder()
                .application(application)
                .build();
        graph.addVertex(applicationVertex);
    }

    @Test
    public void noServices() {
        StatusInfo info = getInfo();
        assertEquals(StatusInfo.defaultStatus().build(), info);
    }

    @Test
    public void servicesWithoutAlarm() throws UnknownHostException {
        addServiceWithOutAlarm(new OnmsServiceType(1, "HTTP"));
        assertEquals(StatusInfo.defaultStatus().build(), getInfo());
    }

    @Test
    public void servicesWithOneAlarm() throws UnknownHostException {
        addServiceWithOutAlarm(new OnmsServiceType(1, "HTTP"));
        addServiceWithAlarm(new OnmsServiceType(2, "HTTP8080"), OnmsSeverity.MINOR, 1);
        assertEquals(StatusInfo.builder(OnmsSeverity.MINOR).count(1).build(), getInfo());
    }

    @Test
    public void servicesWithOneAlarmAcknowledged() throws UnknownHostException {
        addServiceWithOutAlarm(new OnmsServiceType(1, "HTTP"));
        addServiceWithAlarm(new OnmsServiceType(2, "HTTP8080"), OnmsSeverity.MINOR, 0);
        assertEquals(StatusInfo.builder(OnmsSeverity.MINOR).count(0).build(), getInfo());
    }

    @Test
    public void allServicesWithAlarms() throws UnknownHostException {
        addServiceWithAlarm(new OnmsServiceType(1, "HTTP"), OnmsSeverity.MINOR, 1);
        addServiceWithAlarm(new OnmsServiceType(2, "HTTP8080"), OnmsSeverity.MINOR, 1);
        // if all services have alarms we expect the application status to be at least "Major"
        assertEquals(StatusInfo.builder(OnmsSeverity.MAJOR).count(2).build(), getInfo());
    }

    @Test
    public void allServicesWithAlarmsAndOneCritical() throws UnknownHostException {
        addServiceWithAlarm(new OnmsServiceType(1, "HTTP"), OnmsSeverity.MINOR, 1);
        addServiceWithAlarm(new OnmsServiceType(2, "HTTP8080"), OnmsSeverity.CRITICAL, 1);
        // if all services have alarms we expect the application status to be at least "Major"
        assertEquals(StatusInfo.builder(OnmsSeverity.CRITICAL).count(2).build(), getInfo());
    }

    private ApplicationVertex addServiceWithOutAlarm(final OnmsServiceType serviceType) throws UnknownHostException {
        OnmsMonitoredService service = new OnmsMonitoredService();
        service.setId(idCounter ++);
        service.setIpInterface(new OnmsIpInterface(InetAddress.getByName("127.0.0.1"), node));
        service.setServiceType(serviceType);

        final ApplicationVertex serviceVertex = ApplicationVertex.builder().service(service).build();
        graph.addVertex(serviceVertex);

        // connect with application
        final SimpleDomainEdge edge = SimpleDomainEdge.builder()
                .namespace(ApplicationGraph.NAMESPACE)
                .source(applicationVertex.getVertexRef())
                .target(serviceVertex.getVertexRef())
                .build();
        graph.addEdge(edge);
        return serviceVertex;
    }

    private ApplicationVertex addServiceWithAlarm(final OnmsServiceType serviceType, final OnmsSeverity severity, final int count) throws UnknownHostException {
        ApplicationVertex service = addServiceWithOutAlarm(serviceType);
        statusByServiceMap.put(ApplicationStatusEnrichment.toId(service.asGenericVertex()),
                StatusInfo.builder(severity).count(count).build());
        return service;
    }

    private StatusInfo getInfo() {
        final EnrichmentGraphBuilder graphBuilder = new EnrichmentGraphBuilder(graph.build().asGenericGraph());
        return new ApplicationStatusEnrichment(Mockito.mock(ApplicationDao.class))
                .buildStatusForApplication(applicationVertex.asGenericVertex(), graphBuilder, this.statusByServiceMap);
    }

}

