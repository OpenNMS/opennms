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
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.enlinkd.model.IsIsLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.BridgePort;
import org.opennms.netmgt.enlinkd.service.api.MacPort;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

import com.codahale.metrics.MetricRegistry;

public class LinkdEdgeStatusProviderTest extends LinkdTopologyProvider {

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

    protected void add(String id,
            LinkdVertex sourceV, 
            LinkdVertex targetV,              
            SnmpInterfaceTopologyEntity sourceIntf,
            SnmpInterfaceTopologyEntity targetIntf,
            String sourceAddr,
            String targetAddr,
            ProtocolSupported discoveredBy) {
            LinkdPort sourcePort = LinkdPort.create(
                                                    sourceV, 
                                                    sourceIntf.getIfIndex(),
                                                    sourceAddr,
                                                    sourceIntf
                                                    ); 
            LinkdPort targetPort = LinkdPort.create(targetV, 
                                                    targetIntf.getIfIndex(),
                                                    targetAddr,
                                                    targetIntf);

                m_edges.add(
                 LinkdEdge.create(
                                  id, 
                                  sourcePort,
                                  targetPort,
                                  discoveredBy
                          )
                 );
    }

    protected void add(String id,
            LinkdVertex sourceV, 
            LinkdVertex targetV,              
            SnmpInterfaceTopologyEntity targetIntf,
            String targetAddr,
            ProtocolSupported discoveredBy) {
            LinkdPort sourcePort = LinkdPort.create(sourceV
                                                    ); 
            Integer targetifIndex = null;
            if (targetIntf != null) {
                targetifIndex = targetIntf.getIfIndex();
            }
            LinkdPort targetPort = LinkdPort.create(targetV, 
                                                    targetifIndex,
                                                    targetAddr,
                                                    targetIntf);

                m_edges.add(
                 LinkdEdge.create(
                                  id, 
                                  sourcePort,
                                  targetPort,
                                  discoveredBy
                          )
                 );
    }

    @Before
    public void setUp(){
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
        BridgePort designated = new BridgePort();
        designated.setNodeId(1);
        designated.setBridgePort(48);
        // Cloud is identified by the designated bridge and designated port
        LinkdVertex segment = LinkdVertex.createSegmentVertex(designated);
        LinkdVertex node1Vertex = LinkdVertex.createNodeVertex(NodeTopologyEntity.toNodeTopologyInfo(m_node1),null);
        LinkdVertex node2Vertex = LinkdVertex.createNodeVertex(NodeTopologyEntity.toNodeTopologyInfo(m_node2),null);
        LinkdVertex node3Vertex = LinkdVertex.createNodeVertex(NodeTopologyEntity.toNodeTopologyInfo(m_node3),null);

        LinkdVertex node4Vertex = LinkdVertex.createNodeVertex(NodeTopologyEntity.toNodeTopologyInfo(m_node4),null);
        LinkdVertex node5Vertex = LinkdVertex.createNodeVertex(NodeTopologyEntity.toNodeTopologyInfo(m_node5),null);

        LinkdVertex dehliVertex = LinkdVertex.createNodeVertex(NodeTopologyEntity.toNodeTopologyInfo(m_nodeDehli),null);
        LinkdVertex chennaiVertex = LinkdVertex.createNodeVertex(NodeTopologyEntity.toNodeTopologyInfo(m_nodeChennai),null);

        // identification of link is done with targets id..that is port or mac        
        BridgePort bpnode1port48 = new BridgePort();
        bpnode1port48.setNodeId(m_node1.getId());
        bpnode1port48.setBridgePort(48);
        bpnode1port48.setBridgePortIfIndex(48);
        SnmpInterfaceTopologyEntity sourceinterfacenode1port48 = 
                new SnmpInterfaceTopologyEntity(10048,
                                                bpnode1port48.getBridgePortIfIndex(),
                                                "port 48",
                                                "",
                                                0l,
                                                m_node1.getId());

        add(
            Topology.getEdgeId(segment.getId(), bpnode1port48), 
            segment, 
            node1Vertex, 
            sourceinterfacenode1port48, 
            Topology.getAddress(bpnode1port48),
            ProtocolSupported.BRIDGE
        );

        BridgePort bpnode2port24 = new BridgePort();
        bpnode2port24.setNodeId(m_node2.getId());
        bpnode2port24.setBridgePort(24);
        bpnode2port24.setBridgePortIfIndex(24);
        
        SnmpInterfaceTopologyEntity sourceinterfacenode2port24 = new SnmpInterfaceTopologyEntity(20024,24,"port 24","",0l,m_node2.getId());
        add(Topology.getEdgeId(segment.getId(), bpnode2port24), 
                               segment, 
                               node2Vertex, 
                               sourceinterfacenode2port24, 
                               Topology.getAddress(bpnode2port24), 
                               ProtocolSupported.BRIDGE);
        
        IpNetToMedia iptm3 = new IpNetToMedia();
        iptm3.setNetAddress(InetAddressUtils.addr("10.10.1.1"));
        iptm3.setPhysAddress("a8d0e5a0a467");
        MacPort mp3 = MacPort.create(iptm3);

        add(
            Topology.getEdgeId(segment.getId(), mp3), 
            segment, 
            node3Vertex, 
            null,
            Topology.getAddress(mp3), 
            ProtocolSupported.BRIDGE
        );
        
        // isis link
        IsIsLinkTopologyEntity link1 = createIsIsLink(104,m_node4, 599, 1, "001f12accbf1", "000110255062");
        SnmpInterfaceTopologyEntity node4port599 = new SnmpInterfaceTopologyEntity(104, link1.getIsisCircIfIndex(), "port 599","", 0l, m_node4.getId());

        IsIsLinkTopologyEntity link2 = createIsIsLink(105,m_node5, 578, 1, "0021590e47c1", "000110088500");
        SnmpInterfaceTopologyEntity node5port578 = new SnmpInterfaceTopologyEntity(50578,link2.getIsisCircIfIndex(),"port 578","",0l,m_node5.getId());
        
        add(
            Topology.getDefaultEdgeId(link1.getId(), link2.getId()), 
            node4Vertex, 
            node5Vertex, 
            node4port599, 
            node5port578, 
            Topology.getRemoteAddress(link2),
            Topology.getRemoteAddress(link1), 
            ProtocolSupported.ISIS);

        // lldp link
        LldpLinkTopologyEntity link3 = new LldpLinkTopologyEntity(204, 
                                                                  m_node4.getId(), 
                                                                  "node5ChassisId", 
                                                                  "node5PortId", 
                                                                  LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, 
                                                                  "node4PortId", 
                                                                  LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, 
                                                                  "node4PortDescr", 1);
                                                                  
        SnmpInterfaceTopologyEntity node4portPort1 = new SnmpInterfaceTopologyEntity(40001,link3.getLldpPortIfindex(),"port 1","",0l,m_node4.getId());
  
        LldpLinkTopologyEntity link4 = new LldpLinkTopologyEntity(205, 
                                                                  m_node5.getId(), 
                                                                  "node4ChassisId", 
                                                                  "node4PortId", 
                                                                  LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, 
                                                                  "node5PortId", 
                                                                  LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, 
                                                                  "node5PortDescr", 21);
                                      
        SnmpInterfaceTopologyEntity node5portPort2 = new SnmpInterfaceTopologyEntity(50002,link4.getLldpPortIfindex(),"port 2","",0l,m_node5.getId());
         
         add(
             Topology.getDefaultEdgeId(link3.getId(), link4.getId()), 
             node4Vertex, 
             node5Vertex, 
             node4portPort1, 
             node5portPort2, 
             Topology.getRemoteAddress(link4), 
             Topology.getRemoteAddress(link3), 
             ProtocolSupported.LLDP
             );
        
        //ospf link
         OspfLink link5 = createOspfLink(m_node4, "192.168.100.246", "255.255.255.252", 0, 10101, "192.168.100.249", "192.168.100.245", 0);
         link5.setId(404);
         SnmpInterfaceTopologyEntity node4portPort10101 = new SnmpInterfaceTopologyEntity(410101,link5.getOspfIfIndex(),"port 10101","",0l,m_node4.getId());


         OspfLink link6 = createOspfLink(m_node5, "192.168.100.245", "255.255.255.252", 0, 10100, "192.168.100.250", "192.168.100.246", 0);
         link6.setId(405);
         
         SnmpInterfaceTopologyEntity node5portPort10100 = new SnmpInterfaceTopologyEntity(510100,link6.getOspfIfIndex(),"port 10100","",0l,m_node5.getId());
         add(Topology.getDefaultEdgeId(link5.getId(), link6.getId()), node4Vertex, node5Vertex, 
                         node4portPort10101,node5portPort10100,
                         InetAddressUtils.str(link6.getOspfRemIpAddr()), 
                         InetAddressUtils.str(link5.getOspfRemIpAddr()),  ProtocolSupported.OSPF);

        //cdp link
         SnmpInterfaceTopologyEntity edgeGsourceIntf = new SnmpInterfaceTopologyEntity(40101,101,"port 101","",0l,m_node4.getId());
         LinkdPort edgeGsource = LinkdPort.create(node4Vertex, edgeGsourceIntf.getIfIndex(),null,edgeGsourceIntf); 
         SnmpInterfaceTopologyEntity edgeGtargetIntf = new SnmpInterfaceTopologyEntity(50100,100,"port 100","",0l,m_node5.getId());
         LinkdPort edgeGTarget = LinkdPort.create(node5Vertex, edgeGtargetIntf.getIfIndex(), null,edgeGtargetIntf);
         LinkdEdge edgeG = LinkdEdge.create("504|505", edgeGsource, edgeGTarget, ProtocolSupported.CDP );
         m_edges.add(edgeG);

        // another ospf link
         SnmpInterfaceTopologyEntity dehli13 = new SnmpInterfaceTopologyEntity(77778,12,"port 12","",0l,m_nodeDehli.getId());
         LinkdPort dehliPort = LinkdPort.create(dehliVertex, dehli13.getIfIndex(),"10.1.110.1",dehli13); 
         SnmpInterfaceTopologyEntity chennai13 = new SnmpInterfaceTopologyEntity(88889,13,"port 13","",0l,m_nodeChennai.getId());
        LinkdPort chennaiPort = LinkdPort.create(chennaiVertex, chennai13.getIfIndex(),"10.1.110.2",chennai13);
        LinkdEdge edgeChennaiTodehli = LinkdEdge.create("310|314", dehliPort, chennaiPort, ProtocolSupported.OSPF);
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
    
    private IsIsLinkTopologyEntity createIsIsLink(
            Integer id, 
            OnmsNode node, 
            int isisCircIfindex, 
            int isisAdjIndex, 
            String isisIsAdjNeighsnpaadress, 
            String isisisAdjNeighSysid) 
    {
        return new IsIsLinkTopologyEntity(
                                          id, 
                                          node.getId(), 
                                          isisAdjIndex, 
                                          isisCircIfindex, 
                                          isisisAdjNeighSysid, 
                                          isisIsAdjNeighsnpaadress
                                          );
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