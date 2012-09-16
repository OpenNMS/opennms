/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.features.topology.api.TopologyProvider;

import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.util.Assert;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;

public class LinkdTopologyProvider implements TopologyProvider {
    public static final String GROUP_ICON_KEY = "linkd-group";
    public static final String SERVER_ICON_KEY = "linkd-server";

    private boolean addNodeWithoutLink = true;
    
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;
    
    private NodeDao m_nodeDao;
    
    private IpInterfaceDao m_ipInterfaceDao;
    
    private SnmpInterfaceDao m_snmpInterfaceDao;

    private AlarmDao m_alarmDao;

    private String m_configurationFile;
    
    
    private Map<Integer, OnmsSeverity> m_nodeToSeveritymap;
    
    public String getConfigurationFile() {
        return m_configurationFile;
    }

    public void setConfigurationFile(String configurationFile) {
        m_configurationFile = configurationFile;
    }

    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }

    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    
    public boolean isAddNodeWithoutLink() {
        return addNodeWithoutLink;
    }

    public void setAddNodeWithoutLink(boolean addNodeWithoutLink) {
        this.addNodeWithoutLink = addNodeWithoutLink;
    }

    private LinkdVertexContainer m_vertexContainer;
    private BeanContainer<String, LinkdEdge> m_edgeContainer;

    private int m_groupCounter = 0;
    
    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(DataLinkInterfaceDao dataLinkInterfaceDao) {
        m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }

    public void onInit() {
        log("init: loading topology v1.1");
        loadtopology();
    }
    
    public LinkdTopologyProvider() {
        m_vertexContainer = new LinkdVertexContainer();
        m_edgeContainer = new BeanContainer<String, LinkdEdge>(LinkdEdge.class);
        m_edgeContainer.setBeanIdProperty("id");
        m_nodeToSeveritymap = new HashMap<Integer, OnmsSeverity>();
    }

    @Override
    public Object addGroup(String groupIcon) {
        return addGroup(getNextGroupId(), groupIcon);
    }

    private Item addGroup(String groupId, String icon) {
        if (m_vertexContainer.containsId(groupId)) {
            throw new IllegalArgumentException("A vertex or group with id " + groupId + " already exists!");
        }
        System.err.println("Adding a group: " + groupId);
        LinkdVertex vertex = new LinkdGroup(groupId, "Group " + groupId);
        vertex.setIconKey(icon);
        return m_vertexContainer.addBean(vertex);        
    }

    public String getNextGroupId() {
        return "linkdg" + m_groupCounter++;
    }

    
    @Override
    public boolean containsVertexId(Object vertexId) {
        return m_vertexContainer.containsId(vertexId);    
    }

    @Override
    public BeanContainer<String, LinkdEdge> getEdgeContainer() {
        return m_edgeContainer;
    }

    @Override
    public Collection<String> getEdgeIds() {
        return m_edgeContainer.getItemIds();
    }

    @Override
    public Collection<String> getEdgeIdsForVertex(Object vertexId) {
        
        LinkdVertex vertex = getRequiredVertex(vertexId);
        
        List<String> edges = new ArrayList<String>(vertex.getEdges().size());
        
        for(LinkdEdge e : vertex.getEdges()) {
            
            String edgeId = e.getId();
            
            edges.add(edgeId);

        }
        
        return edges;
    }

    
    private LinkdVertex getRequiredVertex(Object vertexId) {
        return getVertex(vertexId, true);
    }

    private LinkdVertex getVertex(Object vertexId, boolean required) {
        BeanItem<LinkdVertex> item = m_vertexContainer.getItem(vertexId);
        if (required && item == null) {
            throw new IllegalArgumentException("required vertex " + vertexId + " not found.");
        }
        
        return item == null ? null : item.getBean();
    }

    @Override
    public Item getEdgeItem(Object edgeId) {
        return m_edgeContainer.getItem(edgeId);
    }

    @Override
    public Collection<String> getEndPointIdsForEdge(Object edgeId) {
        LinkdEdge edge= getRequiredEdge(edgeId);

        List<String> endPoints = new ArrayList<String>(2);
        
        endPoints.add(edge.getSource().getId());
        endPoints.add(edge.getTarget().getId());

        return endPoints;

    }

    private LinkdEdge getRequiredEdge(Object edgeId) {
        return getEdge(edgeId, true);
    }

    private LinkdEdge getEdge(Object edgeId, boolean required) {
        BeanItem<LinkdEdge> item = m_edgeContainer.getItem(edgeId);
        if (required && item == null) {
            throw new IllegalArgumentException("required edge " + edgeId + " not found.");
        }
        
        return item == null ? null : item.getBean();
    }

    @Override
    public LinkdVertexContainer getVertexContainer() {
        return m_vertexContainer;
    }

    @Override
    public Collection<String> getVertexIds() {
        return m_vertexContainer.getItemIds();
    }

    @Override
    public Item getVertexItem(Object vertexId) {
        return m_vertexContainer.getItem(vertexId);
    }

    @Override
    public void load(String filename) {
        if (filename == null) {
            loadtopology();
        } else {
            loadfromfile(filename);
        }
    }

    @XmlRootElement(name="graph")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class SimpleGraph {
        
        @XmlElements({
                @XmlElement(name="vertex", type=LinkdNodeVertex.class),
                @XmlElement(name="group", type=LinkdGroup.class)
        })
        List<LinkdVertex> m_vertices = new ArrayList<LinkdVertex>();
        
        @XmlElement(name="edge")
        List<LinkdEdge> m_edges = new ArrayList<LinkdEdge>();
        
        @SuppressWarnings("unused")
        public SimpleGraph() {}

        public SimpleGraph(List<LinkdVertex> vertices, List<LinkdEdge> edges) {
            m_vertices = vertices;
            m_edges = edges;
        }

    }

    private SimpleGraph getGraphFromFile(File file) {
        return JAXB.unmarshal(file, SimpleGraph.class);
    }

    private void loadfromfile(String filename) {
        File file = new File(filename);
        if (file.exists() && file.canRead()) {
            SimpleGraph graph = getGraphFromFile(file);        
            m_vertexContainer.addAll(graph.m_vertices);        
            m_edgeContainer.addAll(graph.m_edges);
        }
    }

    private void loadseveritymap() {
        m_nodeToSeveritymap.clear();
        for (OnmsAlarm alarm: m_alarmDao.findAll()) {
            if (m_nodeToSeveritymap.containsKey(alarm.getNodeId())
                    && alarm.getSeverity().isLessThan(m_nodeToSeveritymap.get(alarm.getNodeId())))
                return;
            m_nodeToSeveritymap.put(alarm.getNodeId(), alarm.getSeverity());
        }
    }

    private void loadtopology() {
        log("loadtopology: loading topology: configFile:" + m_configurationFile);
        
        log("loadtopology: Clean Vertexcontainer");
        getVertexContainer().removeAllItems();
        log("loadtopology: Clean EdgeContainer");
        getEdgeContainer().removeAllItems();

        log("loadtopology: loading node to severity map");
        loadseveritymap();
        
        Map<String, LinkdVertex> vertexes = new HashMap<String, LinkdVertex>();
        Collection<LinkdEdge> edges = new ArrayList<LinkdEdge>();
        for (DataLinkInterface link: m_dataLinkInterfaceDao.findAll()) {
            log("loadtopology: parsing link: " + link.getDataLinkInterfaceId());

            OnmsNode node = m_nodeDao.get(link.getNode().getId());
            log("loadtopology: found node: " + node.getLabel());
            String sourceId = node.getNodeId();
            LinkdVertex source;
            if ( vertexes.containsKey(sourceId)) {
                source = vertexes.get(sourceId);
            } else {
                log("loadtopology: adding source as vertex: " + node.getLabel());
                OnmsIpInterface ip = getAddress(node);
                source = new LinkdNodeVertex(node.getNodeId(), 0, 0, getIconName(node), node.getLabel(), ( ip == null ? null : ip.getIpAddress().getHostAddress()));
                source.setTooltipText(getNodeTooltipText(node, source, ip));
                vertexes.put(sourceId, source);
            }

            OnmsNode parentNode = m_nodeDao.get(link.getNodeParentId());
            log("loadtopology: found parentnode: " + parentNode.getLabel());
                       String targetId = parentNode.getNodeId();
            LinkdVertex target;
            if (vertexes.containsKey(targetId)) {
                target = vertexes.get(targetId);
            } else {
                log("loadtopology: adding target as vertex: " + parentNode.getLabel());
                OnmsIpInterface ip = getAddress(parentNode);
                target = new LinkdNodeVertex(parentNode.getNodeId(), 0, 0, getIconName(parentNode), parentNode.getLabel(), ( ip == null ? null : ip.getIpAddress().getHostAddress()));
                target.setTooltipText(getNodeTooltipText(parentNode, target, ip));                
                vertexes.put(targetId, target);
            }
            LinkdEdge edge = new LinkdEdge(link.getDataLinkInterfaceId(),source,target); 
            edge.setTooltipText(getEdgeTooltipText(link,source,target));
            edges.add(edge);
        }
        
        log("loadtopology: isAddNodeWithoutLink: " + isAddNodeWithoutLink());
        if (isAddNodeWithoutLink()) {
            for (OnmsNode onmsnode: m_nodeDao.findAll()) {
                log("loadtopology: parsing link less node: " + onmsnode.getLabel());
                String nodeId = onmsnode.getNodeId();
                LinkdVertex linklessnode;
                if (!vertexes.containsKey(nodeId)) {
                    log("loadtopology: adding link less node: " + onmsnode.getLabel());
                    OnmsIpInterface ip = getAddress(onmsnode);
                    linklessnode = new LinkdNodeVertex(onmsnode.getNodeId(), 0, 0, getIconName(onmsnode), onmsnode.getLabel(), ( ip == null ? null : ip.getIpAddress().getHostAddress()));
                    linklessnode.setTooltipText(getNodeTooltipText(onmsnode, linklessnode, ip));
                    vertexes.put(nodeId,linklessnode);
                }                
            }
        }
        
        File configFile = new File(m_configurationFile);

        if (configFile.exists() && configFile.canRead()) {
            log("loadtopology: loading topology from configuration file: " + m_configurationFile);
            m_groupCounter=0;
            SimpleGraph graph = getGraphFromFile(configFile);
            for (LinkdVertex vertex: graph.m_vertices) {
                if (!vertex.isLeaf()) {
                    m_groupCounter++;
                    LinkdGroup group = (LinkdGroup) vertex;
                    log("loadtopology: found group: " + group.getId());
                    for (LinkdVertex vx: group.getMembers()) {
                        log("loadtopology: found group/member: " + group.getId()+"/"+ vx.getId());
                        if (vx.isLeaf() && !vertexes.containsKey(vx.getId()))
                            group.removeMember(vx);
                    }
                    vertexes.put(group.getId(), group);
                }
            }
        }
        
        log("Found Vertexes: #" + vertexes.size());        
        log("Found Edges: #" + edges.size());
        log("Found Groups: #" + m_groupCounter);
        
        m_vertexContainer.addAll(vertexes.values());
        m_edgeContainer.addAll(edges);        
    }

    private String getEdgeTooltipText(DataLinkInterface link,
            LinkdVertex source, LinkdVertex target) {
        String tooltipText="";

        OnmsSnmpInterface sourceInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(Integer.parseInt(source.getId()), link.getIfIndex());
        OnmsSnmpInterface targetInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(Integer.parseInt(target.getId()), link.getParentIfIndex());
        
        if (sourceInterface == null || targetInterface == null) {
            tooltipText+= "Type of the Link: Layer2";
        } else if (sourceInterface.getNetMask() != null && !sourceInterface.getNetMask().isLoopbackAddress() 
                && targetInterface.getNetMask() != null && !targetInterface.getNetMask().isLoopbackAddress()) {
            tooltipText+= "Type of the Link: Layer3/Layer2";
        }
        tooltipText +="\n";

        tooltipText += "Name: <endpoint1 " + source.getLabel() ;
        if (sourceInterface != null ) 
            tooltipText += ":"+sourceInterface.getIfName();
        
        tooltipText += " ---- endpoint2 " + target.getLabel();
        if (targetInterface != null) 
            tooltipText += ":"+targetInterface.getIfName();
        tooltipText +=">\n";

        
        if (targetInterface != null) {
            tooltipText += "Bandwidth: " + LinkdTopologyProvider.getHumanReadableIfSpeed(targetInterface.getIfSpeed());
            tooltipText +="\n";
            tooltipText += "Link status: " + LinkdTopologyProvider.getIfStatusString(targetInterface.getIfAdminStatus()) + "/" + LinkdTopologyProvider.getIfStatusString(targetInterface.getIfOperStatus());
            tooltipText +="\n";
        } else if (sourceInterface != null) {
            tooltipText += "Bandwidth: " + LinkdTopologyProvider.getHumanReadableIfSpeed(sourceInterface.getIfSpeed());
            tooltipText +="\n";
            tooltipText += "Link status: " + LinkdTopologyProvider.getIfStatusString(sourceInterface.getIfAdminStatus()) + "/" + LinkdTopologyProvider.getIfStatusString(sourceInterface.getIfOperStatus());
            tooltipText +="\n";
        }

        tooltipText += "EndPoint1: " + source.getLabel() + ", " + source.getIpAddr();
        tooltipText +="\n";
        
        tooltipText += "EndPoint2: " + target.getLabel() + ", " + target.getIpAddr();
        tooltipText +="\n";
        log("getEdgeTooltipText\n" + tooltipText);
        return tooltipText;
    }

    private String getNodeTooltipText(OnmsNode node, LinkdVertex vertex, OnmsIpInterface ip) {
        String tooltipText="";
        if (node.getSysDescription() != null && node.getSysDescription().length() >0) {
            tooltipText +=node.getSysDescription();
            tooltipText +="\n";
        }
        tooltipText += vertex.getIpAddr();
        tooltipText +="\n";
        
        tooltipText += vertex.getLabel();
        tooltipText +="\n";
        
        if (node.getSysLocation() != null && node.getSysLocation().length() >0) {
            tooltipText +=node.getSysLocation();
            tooltipText +="\n";
        }
        
        OnmsSeverity severity = OnmsSeverity.NORMAL;
        if (m_nodeToSeveritymap.containsKey(node.getId()))
                severity = m_nodeToSeveritymap.get(node.getId());
        tooltipText += "Alarm Status: " + severity.getLabel();
        tooltipText +="\n";
        
        tooltipText += LinkdTopologyProvider.getNodeStatusString(node);
        if (ip.isManaged()) {
            tooltipText += "\\Managed";
        } else {
            tooltipText += "\\UnManaged";
        }
        tooltipText +="\n";

        log("getNodeTooltipText:\n" + tooltipText);
        
        return tooltipText;

    }
    
    protected String getIconName(OnmsNode node) {
        String iconName = SERVER_ICON_KEY;
        
        if (node.getSysObjectId() != null)
            iconName = "snmp:"+node.getSysObjectId();
        return iconName;
       
    }
    
    private OnmsIpInterface getAddress(OnmsNode node) {
        return m_ipInterfaceDao.findPrimaryInterfaceByNodeId(node.getId());
    }
    
    @Override
    public void save(String filename) {
        if (filename == null) 
            filename=m_configurationFile;
        List<LinkdVertex> vertices = getBeans(m_vertexContainer);
        List<LinkdEdge> edges = getBeans(m_edgeContainer);

        SimpleGraph graph = new SimpleGraph(vertices, edges);
        
        JAXB.marshal(graph, new File(filename));
    }

    private <T> List<T> getBeans(BeanContainer<?, T> container) {
        Collection<?> itemIds = container.getItemIds();
        List<T> beans = new ArrayList<T>(itemIds.size());
        
        for(Object itemId : itemIds) {
            beans.add(container.getItem(itemId).getBean());
        }
        
        return beans;
    }

    @Override
    public void setParent(Object vertexId, Object parentId) {
        m_vertexContainer.setParent(vertexId, parentId);
    }

    
    private void log(final String string) {
        System.err.println("LinkdTopologyProvider: "+ string);
    }
    
    /**
     * Do not use directly. Call {@link #getNodeStatusMap 
     * getInterfaceStatusMap} instead.
     */
    private static final Map<Character, String> m_nodeStatusMap;

    /**
     * Do not use directly. Call {@link #getInterfaceStatusMap 
     * getInterfaceStatusMap} instead.
     */
    private static final Map<Character, String> m_interfaceStatusMap;

    /**
     * Do not use directly. Call {@link #getSnmpInterfaceStatusMap 
     * getInterfaceStatusMap} instead.
     */
    private static final Map<Character, String> m_interfaceSnmpStatusMap;

    /**
     * Do not use directly. Call {@link #getServiceStatusMap 
     * getServiceStatusMap} instead.
     */
    private static final Map<Character, String> m_serviceStatusMap;

    static {
        m_nodeStatusMap = new HashMap<Character, String>();
        m_nodeStatusMap.put('A', "Active");
        m_nodeStatusMap.put(' ', "Unknown");
        m_nodeStatusMap.put('D', "Deleted");
        
        m_interfaceStatusMap = new HashMap<Character, String>();
        m_interfaceStatusMap.put('M', "Managed");
        m_interfaceStatusMap.put('U', "Unmanaged");
        m_interfaceStatusMap.put('D', "Deleted");
        m_interfaceStatusMap.put('F', "Forced Unmanaged");
        m_interfaceStatusMap.put('N', "Not Monitored");
        
        m_interfaceSnmpStatusMap = new HashMap<Character, String>();
        m_interfaceSnmpStatusMap.put('P', "Polled");
        m_interfaceSnmpStatusMap.put('N', "Not Monitored");
        
        m_serviceStatusMap = new HashMap<Character, String>();
        m_serviceStatusMap.put('A', "Managed");
        m_serviceStatusMap.put('U', "Unmanaged");
        m_serviceStatusMap.put('D', "Deleted");
        m_serviceStatusMap.put('F', "Forced Unmanaged");
        m_serviceStatusMap.put('N', "Not Monitored");
        m_serviceStatusMap.put('R', "Rescan to Resume");
        m_serviceStatusMap.put('S', "Rescan to Suspend");
        m_serviceStatusMap.put('X', "Remotely Monitored");
        
        
    }

    static final String[] IFTYPES = new String[] {
        "&nbsp;",                     //0 (not supported)
        "other",                    //1
        "regular1822",              //2
        "hdh1822",                  //3
        "ddn-x25",                  //4
        "rfc877-x25",               //5
        "ethernetCsmacd",           //6
        "iso88023Csmacd",           //7
        "iso88024TokenBus",         //8
        "iso88025TokenRing",        //9
        "iso88026Man",              //10
        "starLan",                  //11
        "proteon-10Mbit",           //12
        "proteon-80Mbit",           //13
        "hyperchannel",             //14
        "fddi",                     //15
        "lapb",                     //16
        "sdlc",                     //17
        "ds1",                      //18
        "e1",                       //19
        "basicISDN",                //20
        "primaryISDN",              //21
        "propPointToPointSerial",   //22
        "ppp",                      //23
        "softwareLoopback",         //24
        "eon",                      //25
        "ethernet-3Mbit",           //26
        "nsip",                     //27
        "slip",                     //28
        "ultra",                    //29
        "ds3",                      //30
        "sip",                      //31
        "frame-relay",              //32
        "rs232",                    //33
        "para",                     //34
        "arcnet",                   //35
        "arcnetPlus",               //36
        "atm",                      //37
        "miox25",                   //38
        "sonet",                    //39
        "x25ple",                   //40
        "is0880211c",               //41
        "localTalk",                //42
        "smdsDxi",                  //43
        "frameRelayService",        //44
        "v35",                      //45
        "hssi",                     //46
        "hippi",                    //47
        "modem",                    //48
        "aa15",                     //49
        "sonetPath",                //50
        "sonetVT",                  //51
        "smdsIcip",                 //52
        "propVirtual",              //53
        "propMultiplexor",          //54
        "ieee80212",                //55
        "fibreChannel",             //56
        "hippiInterface",           //57
        "frameRelayInterconnect",   //58
        "aflane8023",               //59
        "aflane8025",               //60
        "cctEmul",                  //61
        "fastEther",                //62
        "isdn",                     //63
        "v11",                      //64
        "v36",                      //65
        "g703at64k",                //66
        "g703at2mb",                //67
        "qllc",                     //68
        "fastEtherFX",              //69
        "channel",                  //70
        "ieee80211",                //71
        "ibm370parChan",            //72
        "escon",                    //73
        "dlsw",                     //74
        "isdns",                    //75
        "isdnu",                    //76
        "lapd",                     //77
        "ipSwitch",                 //78
        "rsrb",                     //79
        "atmLogical",               //80
        "ds0",                      //81
        "ds0Bundle",                //82
        "bsc",                      //83
        "async",                    //84
        "cnr",                      //85
        "iso88025Dtr",              //86
        "eplrs",                    //87
        "arap",                     //88
        "propCnls",                 //89
        "hostPad",                  //90
        "termPad",                  //91
        "frameRelayMPI",            //92
        "x213",                     //93
        "adsl",                     //94
        "radsl",                    //95
        "sdsl",                     //96
        "vdsl",                     //97
        "iso88025CRFPInt",          //98
        "myrinet",                  //99
        "voiceEM",                  //100
        "voiceFXO",                 //101
        "voiceFXS",                 //102
        "voiceEncap",               //103
        "voiceOverIp",              //104
        "atmDxi",                   //105
        "atmFuni",                  //106
        "atmIma",                   //107
        "pppMultilinkBundle",       //108
        "ipOverCdlc",               //109
        "ipOverClaw",               //110
        "stackToStack",             //111
        "virtualIpAddress",         //112
        "mpc",                      //113
        "ipOverAtm",                //114
        "iso88025Fiber",            //115
        "tdlc",                     //116
        "gigabitEthernet",          //117
        "hdlc",                     //118
        "lapf",                     //119
        "v37",                      //120
        "x25mlp",                   //121
        "x25huntGroup",             //122
        "trasnpHdlc",               //123
        "interleave",               //124
        "fast",                     //125
        "ip",                       //126
        "docsCableMaclayer",        //127
        "docsCableDownstream",      //128
        "docsCableUpstream",        //129
        "a12MppSwitch",             //130
        "tunnel",                   //131
        "coffee",                   //132
        "ces",                      //133
        "atmSubInterface",          //134
        "l2vlan",                   //135
        "l3ipvlan",                 //136
        "l3ipxvlan",                //137
        "digitalPowerline",         //138
        "mediaMailOverIp",          //139
        "dtm",                      //140
        "dcn",                      //141
        "ipForward",                //142
        "msdsl",                    //143
        "ieee1394",                 //144
        "if-gsn",                   //145
        "dvbRccMacLayer",           //146
        "dvbRccDownstream",         //147
        "dvbRccUpstream",           //148
        "atmVirtual",               //149
        "mplsTunnel",               //150
        "srp",                      //151
        "voiceOverAtm",             //152
        "voiceOverFrameRelay",      //153
        "idsl",                     //154
        "compositeLink",            //155
        "ss7SigLink",               //156
        "propWirelessP2P",          //157
        "frForward",                //158
        "rfc1483",                  //159
        "usb",                      //160
        "ieee8023adLag",            //161
        "bgppolicyaccounting",      //162
        "frf16MfrBundle",           //163
        "h323Gatekeeper",           //164
        "h323Proxy",                //165
        "mpls",                     //166
        "mfSigLink",                //167
        "hdsl2",                    //168
        "shdsl",                    //169
        "ds1FDL",                   //170
        "pos",                      //171
        "dvbAsiIn",                 //172
        "dvbAsiOut",                //173
        "plc",                      //174
        "nfas",                     //175
        "tr008",                    //176
        "gr303RDT",                 //177
        "gr303IDT",                 //178
        "isup",                     //179
        "propDocsWirelessMaclayer",      //180
        "propDocsWirelessDownstream",    //181
        "propDocsWirelessUpstream",      //182
        "hiperlan2",                //183
        "propBWAp2Mp",              //184
        "sonetOverheadChannel",     //185
        "digitalWrapperOverheadChannel", //186
        "aal2",                     //187
        "radioMAC",                 //188
        "atmRadio",                 //189
        "imt",                      //190
        "mvl",                      //191
        "reachDSL",                 //192
        "frDlciEndPt",              //193
        "atmVciEndPt",              //194
        "opticalChannel",           //195
        "opticalTransport",         //196
        "propAtm",                  //197
        "voiceOverCable",           //198
        "infiniband",               //199
        "teLink",                   //200
        "q2931",                    //201
        "virtualTg",                //202
        "sipTg",                    //203
        "sipSig",                   //204
        "docsCableUpstreamChannel", //205
        "econet",                   //206
        "pon155",                   //207
        "pon622",                   //208
        "bridge",                   //209
        "linegroup",                //210
        "voiceEMFGD",               //211
        "voiceFGDEANA",             //212
        "voiceDID",                 //213
        "mpegTransport",            //214
        "sixToFour",                //215
        "gtp",                      //216
        "pdnEtherLoop1",            //217
        "pdnEtherLoop2",            //218
        "opticalChannelGroup",      //219
        "homepna",                  //220
        "gfp",                      //221
        "ciscoISLvlan",             //222
        "actelisMetaLOOP",          //223
        "fcipLink",                 //224
        "rpr",                      //225
        "qam",                      //226
        "lmp",                      //227
        "cblVectaStar",             //228
        "docsCableMCmtsDownstream", //229
        "adsl2",                    //230
        "macSecControlledIF",       //231
        "macSecUncontrolledIF",     //232
        "aviciOpticalEther",        //233
        "atmbond",                  //234
        "voiceFGDOS",               //235
        "mocaVersion1",             //236
        "ieee80216WMAN",            //237
        "adsl2plus",                //238
        "dvbRcsMacLayer",           //239
        "dvbTdm",                   //240
        "dvbRcsTdma",               //241
        "x86Laps",                  //242
        "wwanPP",                   //243
        "wwanPP2",                  //244
        "voiceEBS",                 //245
        "ifPwType",                 //246
        "ilan",                     //247
        "pip",                      //248
        "aluELP",                   //249
        "gpon",                     //250
      };
    
     static final String[] OPER_ADMIN_STATUS = new String[] {
        "&nbsp;",          //0 (not supported)
        "Up",              //1
        "Down",            //2
        "Testing",         //3
        "Unknown",         //4
        "Dormant",         //5
        "NotPresent",      //6
        "LowerLayerDown"   //7
      };
      
     static final String[] IP_ROUTE_TYPE = new String[] {
            "&nbsp;",         //0 (not supported)
            "Other",          //1
            "Invalid",        //2
            "Direct",         //3
            "Indirect",       //4
          };

    static final String[] IP_ROUTE_PROTO = new String[] {
            "&nbsp;",         //0 (not supported)
            "Other",          //1
            "Local",          //2
            "Netmgmt",        //3
            "icmp",           //4
            "egp",            //5
            "ggp",            //6
            "hello",          //7
            "rip",            //8
            "is-is",          //9
            "es-is",          //10
            "CiscoIGRP",      //11
            "bbnSpfIgp",      //12
            "ospf",           //13
            "bgp",            //14
          };

      public static String getIpRouteProtocolString(int iprouteprotocol) {
          if (IP_ROUTE_PROTO.length > iprouteprotocol)
          return IP_ROUTE_PROTO[iprouteprotocol];
          return IP_ROUTE_PROTO[0];
      }

      public static String getIpRouteTypeString(int iproutetype) {
          if (IP_ROUTE_TYPE.length > iproutetype)
          return IP_ROUTE_TYPE[iproutetype];
          return IP_ROUTE_TYPE[0];
      }

      public static String getIfStatusString(int ifStatusNum) {
          if (ifStatusNum < OPER_ADMIN_STATUS.length) {
              return OPER_ADMIN_STATUS[ifStatusNum];
          } else {
              return "Unknown (" + ifStatusNum + ")";
          }
      }
      
    /**
     * Return the human-readable name for a interface type, should never be null.
     *
     * @param int ifTypeNum.
     * @return a {@link java.lang.String} object.
     */
    public static String getIfTypeString(int ifTypeNum) {
        if (ifTypeNum < IFTYPES.length) {
            return IFTYPES[ifTypeNum];
        } else {
            return "Unknown (" + ifTypeNum + ")";
        }
    }

    /**
     * Return the human-readable name for a node's status, may be null.
     *
     * @param node a {@link OnmsNode} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getNodeStatusString(OnmsNode node) {
        Assert.notNull(node, "node argument cannot be null");

        return getNodeStatusString(node.getType().charAt(0));
    }

    /**
     * Return the human-readable name for a interface status character, may be
     * null.
     *
     * @param c a char.
     * @return a {@link java.lang.String} object.
     */
    public static String getNodeStatusString(char c) {
        return m_nodeStatusMap.get(c);
    }
    
    /**
     * Return the human-readable name for a service status character, may be
     * null.
     *
     * @param c a char.
     * @return a {@link java.lang.String} object.
     */
    public static String getServiceStatusString(char c) {
        return m_serviceStatusMap.get(c);
    }
    
    /** Constant <code>DEFAULT_TRUNCATE_THRESHOLD=28</code> */
    public static final int DEFAULT_TRUNCATE_THRESHOLD = 28;

    /**
     * <p>truncateLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String truncateLabel(String label) {
        return truncateLabel(label, DEFAULT_TRUNCATE_THRESHOLD);
    }

    /**
     * <p>truncateLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     * @param truncateThreshold a int.
     * @return a {@link java.lang.String} object.
     */
    public static String truncateLabel(String label, int truncateThreshold) {
        Assert.notNull(label, "label argument cannot be null");
        Assert.isTrue(truncateThreshold >= 3, "Cannot take a truncate position less than 3 (truncateThreshold is " + truncateThreshold + ")");

        String shortLabel = label;

        if (label.length() > truncateThreshold) {
            shortLabel = label.substring(0, truncateThreshold - 3) + "...";
        }

        return shortLabel;
    }

    /**
     * Always print at least one digit after the decimal point,
     * and at most three digits after the decimal point.
     */
    private static final DecimalFormat s_oneDigitAfterDecimal = new DecimalFormat("0.0##");
    
    /**
     * Print no digits after the decimal point (heh, nor a decimal point).
     */
    private static final DecimalFormat s_noDigitsAfterDecimal = new DecimalFormat("0");
    
    /**
     * Method used to convert an integer bits-per-second value to a more
     * readable vale using commonly recognized abbreviation for network
     * interface speeds. Feel free to expand it as necessary to accomodate
     * different values.
     *
     * @param ifSpeed
     *            The bits-per-second value to be converted into a string
     *            description
     * @return A string representation of the speed (&quot;100 Mbps&quot; for
     *         example)
     */
    public static String getHumanReadableIfSpeed(long ifSpeed) {
        DecimalFormat formatter;
        double displaySpeed;
        String units;
        
        if (ifSpeed >= 1000000000L) {
            if ((ifSpeed % 1000000000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000000000;
            units = "Gbps";
        } else if (ifSpeed >= 1000000L) {
            if ((ifSpeed % 1000000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000000;
            units = "Mbps";
        } else if (ifSpeed >= 1000L) {
            if ((ifSpeed % 1000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000;
            units = "kbps";
        } else {
            formatter = s_noDigitsAfterDecimal;
            displaySpeed = (double) ifSpeed;
            units = "bps";
        }
        
        return formatter.format(displaySpeed) + " " + units;
    }

}
