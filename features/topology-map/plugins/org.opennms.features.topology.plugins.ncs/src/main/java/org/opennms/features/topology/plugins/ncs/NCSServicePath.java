package org.opennms.features.topology.plugins.ncs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.plugins.ncs.NCSEdgeProvider.NCSEdge;
import org.opennms.features.topology.plugins.ncs.NCSEdgeProvider.NCSVertex;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NCSServicePath {
    
    private NCSComponentRepository m_dao;
    private NodeDao m_nodeDao;
    private String m_serviceType;
    private LinkedList<NCSVertex> m_vertices = new LinkedList<NCSVertex>();
    
    public NCSServicePath(Node servicePath, NCSComponentRepository dao, NodeDao nodeDao, String serviceType) {
        m_dao = dao;
        m_nodeDao = nodeDao;
        m_serviceType = serviceType;
        NodeList childNodes = servicePath.getChildNodes();
        for(int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if(item.getNodeName().equals("LSPPath")) {
                parsePath(item);
            }
        }
    }

    private void parsePath(Node item) {
        NodeList lspNode = item.getChildNodes();
        for(int i = 0; i < lspNode.getLength(); i++) {
            Node node = lspNode.item(i);
            if(node.getNodeName().equals("LSPNode")) {
                String nodeForeignId = node.getLastChild().getLastChild().getTextContent();
                m_vertices.add( getVertexRefForForeignId(nodeForeignId, m_serviceType) );
            }
        }
    }

    private NCSVertex getVertexRefForForeignId(String nodeForeignId, String serviceType) {
        OnmsNode node = m_nodeDao.findByForeignId(serviceType, nodeForeignId);
        NCSVertex vertex = new NCSVertex(String.valueOf(node.getId()), node.getLabel());
        return vertex;
    }

    public Collection<NCSVertex> getVertices() {
        return m_vertices;
    }
    
    public Collection<EdgeRef> getEdges(){
        List<EdgeRef> edges = new ArrayList<EdgeRef>();
        
        if(m_vertices.size() >= 2) {
            
            ListIterator<NCSVertex> iterator = m_vertices.listIterator();
            while(iterator.hasNext()) {
                
                NCSVertex sourceRef = iterator.next();
                if(iterator.hasNext()) {
                    NCSVertex targetRef = m_vertices.get(iterator.nextIndex());
                    edges.add(new NCSEdge("something", sourceRef, targetRef));
                }
                
            }
            
        }
        
        
        return edges;
    }

}
