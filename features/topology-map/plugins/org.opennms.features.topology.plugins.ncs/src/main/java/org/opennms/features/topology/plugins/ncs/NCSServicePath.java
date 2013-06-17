package org.opennms.features.topology.plugins.ncs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.plugins.ncs.NCSEdgeProvider.NCSVertex;
import org.opennms.features.topology.plugins.ncs.NCSPathEdgeProvider.NCSPathEdge;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NCSServicePath {
    
    private NCSComponentRepository m_dao;
    private NodeDao m_nodeDao;
    private String m_nodeForeignSource;
    private LinkedList<NCSVertex> m_vertices = new LinkedList<NCSVertex>();
    private String m_serviceForeignSource;
    private String m_deviceAForeignID;
    private String m_deviceZForeignID;
    
    public NCSServicePath(Node servicePath, NCSComponentRepository dao, NodeDao nodeDao, String nodeForeignSource, String serviceForeignSource, String deviceAID, String deviceZID) {
        m_dao = dao;
        m_nodeDao = nodeDao;
        m_nodeForeignSource = nodeForeignSource;
        m_serviceForeignSource = serviceForeignSource;
        m_deviceAForeignID = deviceAID;
        m_deviceZForeignID = deviceZID;
        
        //Add device A to path, its not sent in the path
        m_vertices.add( getVertexRefForForeignId(m_deviceAForeignID, m_nodeForeignSource) );
        NodeList childNodes = servicePath.getChildNodes();
        for(int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if(item.getNodeName().equals("LSPPath")) {
                parsePath(item);
            }
        }
        
        //Add device Z to path, its not sent in the path from the server
        m_vertices.add( getVertexRefForForeignId(m_deviceZForeignID, m_nodeForeignSource) );
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
        List<Edge> edges = new ArrayList<Edge>();
        
        if(m_vertices.size() >= 2) {
            
            ListIterator<NCSVertex> iterator = m_vertices.listIterator();
            while(iterator.hasNext()) {
                
                NCSVertex sourceRef = iterator.next();
                if(iterator.hasNext()) {
                    NCSVertex targetRef = m_vertices.get(iterator.nextIndex());
                    edges.add(new NCSPathEdge(m_serviceForeignSource, sourceRef, targetRef));
                }
                
            }
            
        }
        
        
        return edges;
    }

}
