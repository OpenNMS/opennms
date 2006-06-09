/**
 * 
 */
package org.opennms.secret.dao.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.opennms.secret.dao.NodeInterfaceDao;
import org.opennms.secret.model.Node;
import org.opennms.secret.model.NodeInterface;

/**
 * @author craig
 *
 */
public class NodeInterfaceDaoSimple implements NodeInterfaceDao {
    
    private static int LENGTH = 5;
    private NodeInterface[] interfacearray =  new NodeInterface[LENGTH];
    
    public NodeInterfaceDaoSimple(){
        this.populate();
    }
    
    private void populate() {
        for (int i=1; i<LENGTH;i++) {
            NodeInterface ni = new NodeInterface();
            ni.setId(new Long(i));
            ni.setIfIndex(new Long(i));
            ni.setIpAddr("172.12.5."+i);
            ni.setIpHostName("Node-"+i);
            ni.setIpLastCapsdPoll(new Date());
            ni.setIpStatus(new Long(1));
            ni.setIsManaged("Y");
            ni.setIsSnmpPrimary("P");
//          ni.setNodeId();
            interfacearray[i]=ni;
        }
    }
    
    /* (non-Javadoc)
     * @see org.opennms.secret.dao.NodeInterfaceDao#initialize(java.lang.Object)
     */
    public void initialize(Object obj) {
        // TODO Auto-generated method stub
        
    }
    
    /* (non-Javadoc)
     * @see org.opennms.secret.dao.NodeInterfaceDao#getNodeInterface(java.lang.Long)
     */
    public NodeInterface getNodeInterface(Long id) {
        return interfacearray[id.intValue()];
    }
    
    /* (non-Javadoc)
     * @see org.opennms.secret.dao.NodeInterfaceDao#createInterface(org.opennms.secret.model.NodeInterface)
     */
    public void createInterface(NodeInterface iface) {
        // TODO Auto-generated method stub
        
    }
    
    /* (non-Javadoc)
     * @see org.opennms.secret.dao.NodeInterfaceDao#getServiceCollection(org.opennms.secret.model.NodeInterface)
     */
    public Collection getServiceCollection(NodeInterface ni) {
        return null;
    }
    
    public Collection getNodeInterfaces(Node node) {
        Collection interfaces = new HashSet();
        for (int i =1; i<LENGTH; i++ ) {
            interfaces.add(this.interfacearray[i]);
        }
        return interfaces;
    }
    
}
