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
package org.opennms.features.topology.plugins.topo.linkd.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.mock.MockSessionUtils;
import org.opennms.netmgt.enlinkd.common.TopologyUpdater;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

public class LinkdEdgeStatusProviderTest {

    public LinkdEdgeStatusProviderTest() {
    }

    private AlarmDao m_alarmDao;
    private LinkdEdgeStatusProvider m_statusProvider;
    private LinkdTopologyFactory m_topologyFactory;
    private BackendGraph m_graph;
    private OnmsNode m_node1;
    private OnmsNode m_node2;
    private OnmsNode m_node3;
    private OnmsNode m_node4;
    private OnmsNode m_node5;
    private OnmsNode m_nodeChennai;
    private OnmsNode m_nodeDehli;

    private List<LinkdEdge> m_edges; 

    private LinkdVertex getVertexFromNode(OnmsNode node) {
        return LinkdVertex.create(TopologyUpdater.create(NodeTopologyEntity.toNodeTopologyInfo(node), null), LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD);
    }

    @Before
    public void setUp() {
        OnmsMonitoringLocation location = new OnmsMonitoringLocation("default","default");
        m_node1 = new OnmsNode();
        m_node1.setId(1);
        m_node1.setLabel("source");
        m_node1.setLocation(location);

        m_node2 = new OnmsNode();
        m_node2.setId(2);
        m_node2.setLabel("target1");
        m_node2.setLocation(location);


        m_node3 = new OnmsNode();
        m_node3.setId(3);
        m_node3.setLabel("target2");
        m_node3.setLocation(location);

        m_node4 = new OnmsNode();
        m_node4.setId(4);
        m_node4.setLabel("source");
        m_node4.setLocation(location);

        m_node5 = new OnmsNode();
        m_node5.setId(5);
        m_node5.setLabel("target");
        m_node5.setLocation(location);

        m_nodeDehli = new OnmsNode();
        m_nodeDehli.setId(10);
        m_nodeDehli.setLabel("dehli");
        m_nodeDehli.setLocation(location);

        m_nodeChennai = new OnmsNode();
        m_nodeChennai.setId(14);
        m_nodeChennai.setLabel("chennai");
        m_nodeChennai.setLocation(location);


        m_edges = new ArrayList<>();
        //segment s:1:48
        LinkdVertex segmentVertex = new LinkdVertex("s:1:48", LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD);
        LinkdVertex node1Vertex = getVertexFromNode(m_node1);
        LinkdVertex node2Vertex = getVertexFromNode(m_node2);
        LinkdVertex node3Vertex = getVertexFromNode(m_node3);
        LinkdPort segmentPort = new LinkdPort(segmentVertex, -1);
        LinkdPort bpnode1port48 = new LinkdPort(node1Vertex,48);
        LinkdPort bpnode2port24 = new LinkdPort(node2Vertex,24);
        LinkdPort iptm3 = new LinkdPort(node3Vertex,-1);
        iptm3.setToolTipText("a8d0e5a0a467/[10.10.1.1]");
        m_edges.add(LinkdEdge.create("s:1:48|1:48", segmentPort, bpnode1port48, ProtocolSupported.BRIDGE, LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD));
        m_edges.add(LinkdEdge.create("s:1:48|2:24", segmentPort, bpnode2port24, ProtocolSupported.BRIDGE, LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD));
        m_edges.add(LinkdEdge.create("s:1:48|3",segmentPort,iptm3,ProtocolSupported.BRIDGE, LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD));

        
        // node4 and node5 connected via cdp isis ospf and lldp
        LinkdVertex node4Vertex = getVertexFromNode(m_node4);
        LinkdVertex node5Vertex = getVertexFromNode(m_node5);
        LinkdPort node4port599 = new LinkdPort(node4Vertex, 599);
        LinkdPort node5port578 = new LinkdPort(node5Vertex, 578);
        LinkdPort node4port1 = new LinkdPort(node4Vertex, 1);
        LinkdPort node5port21 = new LinkdPort(node5Vertex, 21);
        LinkdPort node4port10101 = new LinkdPort(node4Vertex, 10101);
        LinkdPort node5port10100 = new LinkdPort(node5Vertex, 10100);
        LinkdPort node4port101 = new LinkdPort(node4Vertex, 101);
        LinkdPort node5port100 = new LinkdPort(node5Vertex, 100);
        m_edges.add(LinkdEdge.create("104|105", node4port599, node5port578,ProtocolSupported.ISIS, LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD));
        m_edges.add(LinkdEdge.create("204|205", node4port1, node5port21,ProtocolSupported.LLDP, LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD));
        m_edges.add(LinkdEdge.create("404|405", node4port10101, node5port10100, ProtocolSupported.OSPF, LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD));
        m_edges.add(LinkdEdge.create("504|505", node4port101, node5port100, ProtocolSupported.CDP, LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD));

        // dehli and chennai connected via ospf
        LinkdVertex dehliVertex = getVertexFromNode(m_nodeDehli);
        LinkdVertex chennaiVertex = getVertexFromNode(m_nodeChennai);
        LinkdPort dehliport12 = new LinkdPort(dehliVertex, 12); 
        LinkdPort chennaiport13 = new LinkdPort(chennaiVertex, 13);
        m_edges.add(LinkdEdge.create("310|314", dehliport12, chennaiport13, ProtocolSupported.OSPF, LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD));

        assertEquals(8, m_edges.size());

        m_alarmDao = mock(AlarmDao.class);
        m_graph = mock(BackendGraph.class);
        m_topologyFactory = mock(LinkdTopologyFactory.class);
        m_statusProvider = new LinkdEdgeStatusProvider();
        m_statusProvider.setAlarmDao(m_alarmDao);
        m_statusProvider.setSessionUtils(new MockSessionUtils());
        m_statusProvider.setLinkdTopologyFactory(m_topologyFactory);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(m_alarmDao);
        verifyNoMoreInteractions(m_graph);
        verifyNoMoreInteractions(m_topologyFactory);
    }

    @Test
    public void testLinkStatusWithNoAlarms() {
        when(m_alarmDao.findMatching(any(org.opennms.core.criteria.Criteria.class))).thenReturn(createEmptyAlarmList());
        List<EdgeRef> edges = getEdgeRefs();
        assertEquals(8, edges.size());
        for (EdgeRef ref: edges)
            when(m_graph.getEdge(ref)).thenReturn(getEdgeFromRef(ref));
        

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_graph, edges, new Criteria[0]);

        assertEquals(8, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        for (Status status : statusMap.values()){
            assertEquals("up", status.computeStatus());
        }

        Map<EdgeRef, Status> statusMap2 = m_statusProvider.getStatusForEdges(m_graph, edges, new Criteria[0]);
        assertEquals(8, statusMap2.size());
        for (Status status : statusMap2.values()) {
            assertEquals("up", status.computeStatus());
        }

        verify(m_alarmDao, atLeastOnce()).findMatching(any(org.opennms.core.criteria.Criteria.class));
        verify(m_graph, atLeastOnce()).getEdge(any(EdgeRef.class));
    }

    @Test
    public void testGetBridgeLinkStatusDesignatedCloudDown(){
        when(m_alarmDao.findMatching(any(org.opennms.core.criteria.Criteria.class))).thenReturn(createCloudDownAlarm());
        List<EdgeRef> edges = getEdgeRefs();
        for (EdgeRef ref: edges) 
            when(m_graph.getEdge(ref)).thenReturn(getEdgeFromRef(ref));

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_graph, edges, new Criteria[0]);
        assertEquals(8, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        assertEquals(statusMap.get(edges.get(1)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(2)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(3)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(4)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(5)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(6)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(7)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(0)).computeStatus(), "down");

        verify(m_alarmDao, atLeastOnce()).findMatching(any(org.opennms.core.criteria.Criteria.class));
        verify(m_graph, atLeastOnce()).getEdge(any(EdgeRef.class));
    }

    @Test
    public void testGetBridgeLinkStatusOneDown(){
        when(m_alarmDao.findMatching(any(org.opennms.core.criteria.Criteria.class))).thenReturn(createBridgeDownAlarm());
        List<EdgeRef> edges = getEdgeRefs();
        for (EdgeRef ref: edges) 
            when(m_graph.getEdge(ref)).thenReturn(getEdgeFromRef(ref));

        
        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_graph, edges, new Criteria[0]);
        assertEquals(8, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));

        assertEquals(statusMap.get(edges.get(0)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(2)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(3)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(4)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(5)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(6)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(7)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(1)).computeStatus(), "down");

        verify(m_alarmDao, atLeastOnce()).findMatching(any(org.opennms.core.criteria.Criteria.class));
        verify(m_graph, atLeastOnce()).getEdge(any(EdgeRef.class));
    }
    
    @Test
    public void testGetIsisLinkStatusDown(){
        when(m_alarmDao.findMatching(any(org.opennms.core.criteria.Criteria.class))).thenReturn(createIsIsDownAlarm());
        List<EdgeRef> edges = getEdgeRefs();
        for (EdgeRef ref: edges) 
            when(m_graph.getEdge(ref)).thenReturn(getEdgeFromRef(ref));

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_graph, edges, new Criteria[0]);
        assertEquals(8, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        assertEquals(statusMap.get(edges.get(0)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(1)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(2)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(4)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(5)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(6)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(7)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(3)).computeStatus(), "down");

        verify(m_alarmDao, atLeastOnce()).findMatching(any(org.opennms.core.criteria.Criteria.class));
        verify(m_graph, atLeastOnce()).getEdge(any(EdgeRef.class));
    }

    @Test
    public void testGetLldpLinkStatusDown(){
        when(m_alarmDao.findMatching(any(org.opennms.core.criteria.Criteria.class))).thenReturn(createLldpDownAlarm());
        List<EdgeRef> edges = getEdgeRefs();
        for (EdgeRef ref: edges) 
            when(m_graph.getEdge(ref)).thenReturn(getEdgeFromRef(ref));

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_graph, edges, new Criteria[0]);
        assertEquals(8, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        assertEquals(statusMap.get(edges.get(0)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(1)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(2)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(3)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(5)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(6)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(7)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(4)).computeStatus(), "down");

        verify(m_alarmDao, atLeastOnce()).findMatching(any(org.opennms.core.criteria.Criteria.class));
        verify(m_graph, atLeastOnce()).getEdge(any(EdgeRef.class));
    }

    @Test
    public void testGetOspfLinkStatusDown(){
        when(m_alarmDao.findMatching(any(org.opennms.core.criteria.Criteria.class))).thenReturn(createOspfDownAlarm());
        List<EdgeRef> edges = getEdgeRefs();
        for (EdgeRef ref: edges) 
            when(m_graph.getEdge(ref)).thenReturn(getEdgeFromRef(ref));

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_graph, edges, new Criteria[0]);
        assertEquals(8, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        assertEquals(statusMap.get(edges.get(0)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(1)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(2)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(3)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(4)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(6)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(7)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(5)).computeStatus(), "down");

        verify(m_alarmDao, atLeastOnce()).findMatching(any(org.opennms.core.criteria.Criteria.class));
        verify(m_graph, atLeastOnce()).getEdge(any(EdgeRef.class));
    }

    @Test
    public void testGetCdpLinkStatusDown(){
        when(m_alarmDao.findMatching(any(org.opennms.core.criteria.Criteria.class))).thenReturn(createCdpDownAlarm());
        List<EdgeRef> edges = getEdgeRefs();
        for (EdgeRef ref: edges) 
            when(m_graph.getEdge(ref)).thenReturn(getEdgeFromRef(ref));

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_graph, edges, new Criteria[0]);
        assertEquals(8, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        assertEquals(statusMap.get(edges.get(0)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(1)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(2)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(3)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(4)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(5)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(7)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(6)).computeStatus(), "down");

        verify(m_alarmDao, atLeastOnce()).findMatching(any(org.opennms.core.criteria.Criteria.class));
        verify(m_graph, atLeastOnce()).getEdge(any(EdgeRef.class));
    }

    @Test
    public void testSPC944OspfLinkStatus(){
        when(m_alarmDao.findMatching(any(org.opennms.core.criteria.Criteria.class))).thenReturn(createChennaiDownAlarm());
                List<EdgeRef> edges = getEdgeRefs();
                for (EdgeRef ref: edges) 
                    when(m_graph.getEdge(ref)).thenReturn(getEdgeFromRef(ref));

                Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_graph, edges, new Criteria[0]);
                assertEquals(8, statusMap.size());
                assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
                assertEquals(statusMap.get(edges.get(0)).computeStatus(), "up");
                assertEquals(statusMap.get(edges.get(1)).computeStatus(), "up");
                assertEquals(statusMap.get(edges.get(2)).computeStatus(), "up");
                assertEquals(statusMap.get(edges.get(3)).computeStatus(), "up");
                assertEquals(statusMap.get(edges.get(4)).computeStatus(), "up");
                assertEquals(statusMap.get(edges.get(5)).computeStatus(), "up");
                assertEquals(statusMap.get(edges.get(6)).computeStatus(), "up");
                assertEquals(statusMap.get(edges.get(7)).computeStatus(), "down");

                verify(m_alarmDao, atLeastOnce()).findMatching(any(org.opennms.core.criteria.Criteria.class));
                verify(m_graph, atLeastOnce()).getEdge(any(EdgeRef.class));
    }


    private List<EdgeRef> getEdgeRefs() {
        List<EdgeRef> edgerefs = new ArrayList<>();
        for (LinkdEdge edge: m_edges)
            edgerefs.add((EdgeRef)edge);
        return edgerefs;
    }
     
    private Edge getEdgeFromRef(EdgeRef ref) {
        for (LinkdEdge edge: m_edges) {
            if (edge.getId().equals(ref.getId()))
                return edge;
        }
        return null;
    }
    
    private List<OnmsAlarm> createEmptyAlarmList() {
        return Collections.emptyList();
    }

    private List<OnmsAlarm> createChennaiDownAlarm(){
        List<OnmsAlarm> alarms = new ArrayList<>();

        OnmsAlarm alarm1 = new OnmsAlarm();
        alarm1.setNode(m_nodeChennai);
        alarm1.setIfIndex(13);
        alarm1.setUei(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);
        alarms.add(alarm1);

        return alarms;
    }

    private List<OnmsAlarm> createCdpDownAlarm(){
        List<OnmsAlarm> alarms = new ArrayList<>();

        OnmsAlarm alarm1 = new OnmsAlarm();
        alarm1.setNode(m_node5);
        alarm1.setIfIndex(100);
        alarm1.setUei(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);
        alarms.add(alarm1);

        return alarms;
    }

    private List<OnmsAlarm> createIsIsDownAlarm(){
        List<OnmsAlarm> alarms = new ArrayList<>();

        OnmsAlarm alarm1 = new OnmsAlarm();
        alarm1.setNode(m_node5);
        alarm1.setIfIndex(578);
        alarm1.setUei(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);
        alarms.add(alarm1);

        return alarms;
    }

    private List<OnmsAlarm> createOspfDownAlarm(){
        List<OnmsAlarm> alarms = new ArrayList<>();

        OnmsAlarm alarm1 = new OnmsAlarm();
        alarm1.setNode(m_node4);
        alarm1.setIfIndex(10101);
        alarm1.setUei(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);
        alarms.add(alarm1);

        return alarms;
    }

    private List<OnmsAlarm> createLldpDownAlarm(){
        List<OnmsAlarm> alarms = new ArrayList<>();

        OnmsAlarm alarm1 = new OnmsAlarm();
        alarm1.setNode(m_node4);
        alarm1.setIfIndex(1);
        alarm1.setUei(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);
        alarms.add(alarm1);

        return alarms;
    }

    private List<OnmsAlarm> createBridgeDownAlarm(){

        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setNode(m_node2);
        alarm.setIfIndex(24);
        alarm.setUei(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);

        return Arrays.asList(alarm);
    }

    private List<OnmsAlarm> createCloudDownAlarm() {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setNode(m_node1);
        alarm.setIfIndex(48);
        alarm.setUei(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);

        return Arrays.asList(alarm);
    }    
}