/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.ncs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.plugins.ncs.NCSEdgeProvider.NCSVertex;
import org.opennms.features.topology.plugins.ncs.NCSPathEdgeProvider.NCSPathEdge;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NCSServicePath {
    
    private NCSComponentRepository m_dao;
    private NodeDao m_nodeDao;
    private String m_nodeForeignSource;
    private List<NCSVertex> m_vertices = new LinkedList<>();
    private String m_serviceForeignSource;
    private String m_deviceAForeignID;
    private String m_deviceZForeignID;
    private String m_serviceName;
    private int m_statusCode;
    
    public NCSServicePath(Node data, NCSComponentRepository dao, NodeDao nodeDao, String nodeForeignSource, String serviceForeignSource, String deviceAID, String deviceZID, String serviceName) {
        m_dao = dao;
        m_nodeDao = nodeDao;
        m_nodeForeignSource = nodeForeignSource;
        m_serviceForeignSource = serviceForeignSource;
        m_deviceAForeignID = deviceAID;
        m_deviceZForeignID = deviceZID;
        m_serviceName = serviceName;
        
        setStatusCode(data);
        
        //Add device A to path, its not sent in the path
        m_vertices.add( getVertexRefForForeignId(m_deviceAForeignID, m_nodeForeignSource) );
        NodeList childNodes = getServicePath(data);
        for(int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if(item.getNodeName().equals("LSPPath")) {
                parsePath(item);
            }
        }
        
        //Add device Z to path, its not sent in the path from the server
        m_vertices.add( getVertexRefForForeignId(m_deviceZForeignID, m_nodeForeignSource) );
    }

    private NodeList getServicePath(Node data) {
        NodeList servicePath = data.getOwnerDocument().getElementsByTagName("ServicePath");
        return servicePath.item(0).getChildNodes();
    }

    private void setStatusCode(Node data) {
        NodeList childNodes = data.getChildNodes();
        for(int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if(item.getNodeName().equals("Status")) {
                Node firstChild = item.getFirstChild();
                String nodeValue = firstChild.getFirstChild().getNodeValue();
                m_statusCode = Integer.valueOf(nodeValue);
            }
        }
        
    }
    
    public int getStatusCode() {
        return m_statusCode;
    }

    private void parsePath(Node item) {
        NodeList lspNode = item.getChildNodes();
        for(int i = 0; i < lspNode.getLength(); i++) {
            Node node = lspNode.item(i);
            if(node.getNodeName().equals("LSPNode")) {
                String nodeForeignId = node.getLastChild().getLastChild().getTextContent();
                if(!m_deviceAForeignID.equals(nodeForeignId) && !m_deviceZForeignID.equals(nodeForeignId)) {
                    NCSVertex vertex = getVertexRefForForeignId(nodeForeignId, m_nodeForeignSource);
                    if(vertex != null) {
                        m_vertices.add( vertex );
                    }
                }
            }
        }
    }

    private NCSVertex getVertexRefForForeignId(String nodeForeignId, String nodeForeignSource) {
        OnmsNode node = m_nodeDao.findByForeignId(nodeForeignSource, nodeForeignId);
        if(node != null) {
            NCSVertex vertex = new NCSVertex(String.valueOf(node.getId()), node.getLabel());
            return vertex;
        }else {
            return null;
        }
        
    }

    public Collection<NCSVertex> getVertices() {
        return m_vertices;
    }
    
    public List<Edge> getEdges(){
        List<Edge> edges = new ArrayList<>();
        
        if(m_vertices.size() >= 2) {
            String deviceA = m_vertices.get(0).getLabel();
            String deviceZ = m_vertices.get(m_vertices.size() -1).getLabel();
            ListIterator<NCSVertex> iterator = m_vertices.listIterator();
            while(iterator.hasNext()) {
                
                NCSVertex sourceRef = iterator.next();
                if(iterator.hasNext()) {
                    NCSVertex targetRef = m_vertices.get(iterator.nextIndex());
                    NCSPathEdge ncsPathEdge = new NCSPathEdge(m_serviceName, deviceA, deviceZ, sourceRef, targetRef);
                    ncsPathEdge.setStyleName("ncs edge direct");
                    edges.add(ncsPathEdge);
                }
                
            }
            
        }
        
        
        return edges;
    }

}
