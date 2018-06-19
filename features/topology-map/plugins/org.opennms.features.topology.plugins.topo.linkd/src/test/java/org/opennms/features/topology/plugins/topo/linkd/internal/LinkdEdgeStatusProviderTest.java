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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.IsIsElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.model.topology.BridgePort;

import com.codahale.metrics.MetricRegistry;

public class LinkdEdgeStatusProviderTest extends EnhancedLinkdTopologyProvider {

    public LinkdEdgeStatusProviderTest() {
        super(new MetricRegistry());
    }

    private AlarmDao m_alarmDao;
    private LinkdEdgeStatusProvider m_statusProvider;
    private EdgeProvider m_edgeProvider;
    private OnmsNode m_node1;
    private OnmsNode m_node2;
    private OnmsNode m_node3;
    private OnmsNode m_node4;
    private OnmsNode m_node5;
    private OnmsNode m_nodeChennai;
    private OnmsNode m_nodeDehli;

    private List<LinkdEdge> m_edges; 

    @Before
    public void setUp(){
        m_node1 = new OnmsNode();
        m_node1.setId(1);

        m_node2 = new OnmsNode();
        m_node2.setId(2);

        m_node3 = new OnmsNode();
        m_node3.setId(3);
        
        m_node4 = new OnmsNode();
        m_node4.setId(4);

        m_node5 = new OnmsNode();
        m_node5.setId(5);

        m_nodeDehli = new OnmsNode();
        m_nodeDehli.setId(10);

        m_nodeChennai = new OnmsNode();
        m_nodeChennai.setId(14);


        m_edges = new ArrayList<>();
        Vertex sourceCloud = new AbstractVertex("nodes", "1", "source");
        // Cloud is identified by the designated bridge and designated port
        Vertex cloud = new AbstractVertex("nodes", "1:48", "cloud");
        Vertex targetCloud1 = new AbstractVertex("nodes", "2", "target1");
        Vertex targetCloud2 = new AbstractVertex("nodes", "3", "target2");

        Vertex source = new AbstractVertex("nodes", "4", "source");
        Vertex target = new AbstractVertex("nodes", "5", "target");

        Vertex dehli = new AbstractVertex("nodes", "10", "dehli");
        Vertex chennai = new AbstractVertex("nodes", "14", "chennai");

        // identification of link is done with targets id..that is port or mac        
        BridgePort bpnode1port48 = new BridgePort();
        bpnode1port48.setNodeId(m_node1.getId());
        bpnode1port48.setBridgePort(48);
        bpnode1port48.setBridgePortIfIndex(48);
        m_edges.add(connectVertices(bpnode1port48, cloud, sourceCloud, BRIDGE_EDGE_NAMESPACE));

        BridgePort bpnode2port24 = new BridgePort();
        bpnode2port24.setNodeId(m_node2.getId());
        bpnode2port24.setBridgePort(24);
        bpnode2port24.setBridgePortIfIndex(24);
        m_edges.add(connectVertices(bpnode2port24, cloud, targetCloud1, BRIDGE_EDGE_NAMESPACE));

        m_edges.add(connectCloudMacVertices("a8d0e5a0a467", cloud, targetCloud2, BRIDGE_EDGE_NAMESPACE));
        
        // isis link
        IsIsLink link1 = createIsIsLink(m_node4, 599, 599, 1, 1, "001f12accbf1", "000110255062");
        link1.setId(104);

        IsIsLink link2 = createIsIsLink(m_node5, 578, 578, 1, 1, "0021590e47c1", "000110088500");
        link2.setId(105);
        
        m_edges.add(connectVertices(
                                    new IsIsLinkDetail(Math.min(link1.getId(), link2.getId()) + "|" + Math.max(link1.getId(), link2.getId()), source, link1, target, link2), 
                                    ISIS_EDGE_NAMESPACE));

        // lldp link
        LldpLink link3 = new LldpLink(m_node4, 12, 1, "node4PortId", "node4PortDescr", LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL,
                                     "node4ChassisId", "node4SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL, "node4PortId",
                                     LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, "node2PortDescr");
        link3.setId(204);
  
        LldpLink link4 = new LldpLink(m_node5, 21, 2, "node5PortId", "node5PortDescr", LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL,
                                     "node5ChassisId", "node5SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL, "node5PortId",
                                     LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, "node1PortDescr");
        link4.setId(205);
         
         m_edges.add(connectVertices(
                                    new LldpLinkDetail(Math.min(link3.getId(), link4.getId()) + "|" + Math.max(link3.getId(), link4.getId()),
                                                       source, link3, target, link4), LLDP_EDGE_NAMESPACE));
        
        //ospf link
         OspfLink link5 = createOspfLink(m_node4, "192.168.100.246", "255.255.255.252", 0, 10101, "192.168.100.249", "192.168.100.245", 0);
         link5.setId(404);

         OspfLink link6 = createOspfLink(m_node5, "192.168.100.245", "255.255.255.252", 0, 10100, "192.168.100.250", "192.168.100.246", 0);
         link6.setId(405);
         m_edges.add(connectVertices(
                                  new OspfLinkDetail(Math.min(link5.getId(), link6.getId()) + "|" + Math.max(link5.getId(), link6.getId()),
                                                       source, link5, target, link6), OSPF_EDGE_NAMESPACE));

        //cdp link
        LinkdEdge edgeG = new LinkdEdge(EnhancedLinkdTopologyProvider.CDP_EDGE_NAMESPACE, "504|505", source, target);
        edgeG.setSourceNodeid(m_node4.getId());
        edgeG.setTargetNodeid(m_node5.getId());
        edgeG.setSourceEndPoint("101");
        edgeG.setTargetEndPoint("100");
        m_edges.add(edgeG);

        // another ospf link
        LinkdEdge edgeChennaiTodehli = new LinkdEdge(EnhancedLinkdTopologyProvider.OSPF_EDGE_NAMESPACE, "310|314", dehli, chennai);
        edgeChennaiTodehli.setSourceNodeid(m_nodeDehli.getId());
        edgeChennaiTodehli.setTargetNodeid(m_nodeChennai.getId());
        edgeChennaiTodehli.setSourceEndPoint("13");
        edgeChennaiTodehli.setTargetEndPoint("13");
        m_edges.add(edgeChennaiTodehli);

        m_alarmDao = EasyMock.createMock(AlarmDao.class);
        m_edgeProvider = EasyMock.createMock(EdgeProvider.class);
        m_statusProvider = new LinkdEdgeStatusProvider();
        m_statusProvider.setAlarmDao(m_alarmDao);


    }

    @After
    public void tearDown() {
        EasyMock.reset(m_alarmDao,m_edgeProvider);
    }

    @Test
    public void testLinkStatusWithNoAlarms() {
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createEmptyAlarmList()).anyTimes();
        List<EdgeRef> edges = getEdgeRefs();
        for (EdgeRef ref: edges) 
            EasyMock.expect(
                            m_edgeProvider.getEdge(ref)).andReturn(getEdgeFromRef(ref)).anyTimes();
        EasyMock.replay(m_alarmDao,m_edgeProvider);
        

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);

        assertEquals(8, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        for (Status status : statusMap.values()){
            assertEquals("up", status.computeStatus());
        }

        Map<EdgeRef, Status> statusMap2 = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);
        assertEquals(8, statusMap2.size());
        for (Status status : statusMap2.values()) {
            assertEquals("up", status.computeStatus());
        }

    }

    @Test
    public void testGetBridgeLinkStatusDesignatedCloudDown(){
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createCloudDownAlarm()).anyTimes();
        List<EdgeRef> edges = getEdgeRefs();
        for (EdgeRef ref: edges) 
            EasyMock.expect(
                            m_edgeProvider.getEdge(ref)).andReturn(getEdgeFromRef(ref)).anyTimes();
        EasyMock.replay(m_alarmDao,m_edgeProvider);

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);
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
    }

    @Test
    public void testGetBridgeLinkStatusOneDown(){
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createBridgeDownAlarm()).anyTimes();
        List<EdgeRef> edges = getEdgeRefs();
        for (EdgeRef ref: edges) 
            EasyMock.expect(
                            m_edgeProvider.getEdge(ref)).andReturn(getEdgeFromRef(ref)).anyTimes();
        EasyMock.replay(m_alarmDao,m_edgeProvider);

        
        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);
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
    }
    
    @Test
    public void testGetIsisLinkStatusDown(){
        EasyMock.expect(
                        m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createIsIsDownAlarm()).anyTimes();
        List<EdgeRef> edges = getEdgeRefs();
        for (EdgeRef ref: edges) 
            EasyMock.expect(
                            m_edgeProvider.getEdge(ref)).andReturn(getEdgeFromRef(ref)).anyTimes();
        EasyMock.replay(m_alarmDao,m_edgeProvider);

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);
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
    }

    @Test
    public void testGetLldpLinkStatusDown(){
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createLldpDownAlarm()).anyTimes();
        List<EdgeRef> edges = getEdgeRefs();
        for (EdgeRef ref: edges) 
            EasyMock.expect(
                            m_edgeProvider.getEdge(ref)).andReturn(getEdgeFromRef(ref)).anyTimes();
        EasyMock.replay(m_alarmDao,m_edgeProvider);

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);
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
    }

    @Test
    public void testGetOspfLinkStatusDown(){
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createOspfDownAlarm()).anyTimes();
        List<EdgeRef> edges = getEdgeRefs();
        for (EdgeRef ref: edges) 
            EasyMock.expect(
                            m_edgeProvider.getEdge(ref)).andReturn(getEdgeFromRef(ref)).anyTimes();
        EasyMock.replay(m_alarmDao,m_edgeProvider);

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);
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
    }

    @Test
    public void testGetCdpLinkStatusDown(){
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createCdpDownAlarm()).anyTimes();
        List<EdgeRef> edges = getEdgeRefs();
        for (EdgeRef ref: edges) 
            EasyMock.expect(
                            m_edgeProvider.getEdge(ref)).andReturn(getEdgeFromRef(ref)).anyTimes();
        EasyMock.replay(m_alarmDao,m_edgeProvider);

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);
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
    }

    @Test
    public void testSPC944OspfLinkStatus(){
        EasyMock.expect(
                        m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createChennaiDownAlarm()).anyTimes();
                List<EdgeRef> edges = getEdgeRefs();
                for (EdgeRef ref: edges) 
                    EasyMock.expect(
                                    m_edgeProvider.getEdge(ref)).andReturn(getEdgeFromRef(ref)).anyTimes();
                        EasyMock.replay(m_alarmDao,m_edgeProvider);

                Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);
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
    
    private IsIsLink createIsIsLink(OnmsNode node, int isisCircIndex, int isisCircIfindex, int isisAdjIndex, int isisCircAdmin, String isisIsAdjNeighsnpaadress, String isisisAdjNeighSysid) {
        final IsIsLink isisLink = new IsIsLink();
        isisLink.setNode(node);
        isisLink.setIsisCircIndex(isisCircIndex);
        isisLink.setIsisCircIfIndex(isisCircIfindex);
        isisLink.setIsisISAdjIndex(isisAdjIndex);
        isisLink.setIsisCircAdminState(IsIsElement.IsisAdminState.get(isisCircAdmin));
        isisLink.setIsisISAdjNeighSNPAAddress(isisIsAdjNeighsnpaadress);
        isisLink.setIsisISAdjNeighSysID(isisisAdjNeighSysid);
        return isisLink;

    }
    
    private OspfLink createOspfLink(OnmsNode node, String sourceIpAddr, String sourceIpMask, int addrLessIndex, int ifIndex, String remRouterId, String remIpAddr, int remAddrLessIndex) {
        final OspfLink ospfLink = new OspfLink();
        ospfLink.setNode(node);
        ospfLink.setOspfIpAddr(InetAddressUtils.addr(sourceIpAddr));
        ospfLink.setOspfIpMask(InetAddressUtils.addr(sourceIpMask));
        ospfLink.setOspfAddressLessIndex(addrLessIndex);
        ospfLink.setOspfIfIndex(ifIndex);
        ospfLink.setOspfRemRouterId(InetAddressUtils.addr(remRouterId));
        ospfLink.setOspfRemIpAddr(InetAddressUtils.addr(remIpAddr));
        ospfLink.setOspfRemAddressLessIndex(remAddrLessIndex);
        return ospfLink;
    }
}
