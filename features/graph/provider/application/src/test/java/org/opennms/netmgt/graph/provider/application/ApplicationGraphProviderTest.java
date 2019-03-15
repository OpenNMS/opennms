/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.graph.provider.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.graph.api.Graph;
import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.simple.SimpleEdge;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsServiceType;

public class ApplicationGraphProviderTest {

    private IdGenerator idGenerator;
    private Graph graph;

    @Before
    public void setUp(){
        idGenerator = new IdGenerator();
    }

    @Test
    public void shouldCreateProperGraph() {
        List<OnmsApplication> applications = generateApplications();
        ApplicationDao dao = Mockito.mock(ApplicationDao.class);
        when(dao.findAll()).thenReturn(applications);
        ApplicationGraphProvider provider = new ApplicationGraphProvider(dao);
        graph = provider.loadGraph();
        assertEquals(30, graph.getVertices().size());
        for(OnmsApplication app: applications) {
            for(OnmsMonitoredService service : app.getMonitoredServices()){
                verifyLinkingBetweenNodes(graph.getVertex(app.getId().toString()), graph.getVertex(service.getId().toString()));
            }
        }
    }

    private List<OnmsApplication> generateApplications() {
        List<OnmsApplication> applications = new ArrayList<>();
        for(int i = 0 ; i < 5; i++){
            applications.add(generateApplication());
        }
        return applications;
    }

    private OnmsApplication generateApplication(){
        OnmsApplication app = new OnmsApplication();
        app.setId(idGenerator.next());
        app.setName(UUID.randomUUID().toString());
        app.setMonitoredServices(generateMonitoredServices());
        return app;
    }

    private Set<OnmsMonitoredService> generateMonitoredServices() {
        Set<OnmsMonitoredService> monitoredServices = new HashSet<>();
        for(int i = 0; i < 5; i++) {
            monitoredServices.add(generateMonitoredService());
        }
        return monitoredServices;
    }

    private OnmsMonitoredService generateMonitoredService() {
        OnmsMonitoredService service = new OnmsMonitoredService();
        service.setId(idGenerator.next());
        service.setQualifier(UUID.randomUUID().toString());
        service.setServiceType(new OnmsServiceType(UUID.randomUUID().toString()));
        return service;
    }

    private void verifyLinkingBetweenNodes(Vertex left, Vertex right) {

        Collection<SimpleEdge> edgesLeft = this.graph.getConnectingEdges(left); // TODO: patrick get the generics right
        Collection<SimpleEdge> edgesRight = this.graph.getConnectingEdges(right);

        // 1.) get the EdgeRef that connects the 2 vertices
        Set<SimpleEdge> intersection = intersect(edgesLeft, edgesRight);
        assertEquals(1, intersection.size());
        SimpleEdge edge = intersection.iterator().next();

        // 2.) get the Edge and check if it really connects the 2 Vertices

        // we don't know the direction it is connected so we have to test both ways:
        assertTrue(
                (resolveVertex(edge.getSource()).equals(left) || resolveVertex(edge.getSource()).equals(right)) // source side
                        && (resolveVertex(edge.getTarget()).equals(left) || resolveVertex(edge.getTarget()).equals(right)) // target side
                        && !resolveVertex(edge.getSource()).equals(resolveVertex(edge.getTarget()))); // make sure it doesn't connect the same node
    }

    private Vertex resolveVertex(VertexRef ref) {
        return this.graph.getVertex(ref.getId());
    }

    /**
     * Gives back the intersection between the 2 collections, as in:
     * - only elements that are contained in both collections will be retained
     * - double elements are removed
     */
    private <E> Set<E> intersect(final Collection<E> left, final Collection<E> right){
        Set<E> set = new HashSet<>(left);
        set.retainAll(new HashSet<>(right));
        return set;
    }
}
