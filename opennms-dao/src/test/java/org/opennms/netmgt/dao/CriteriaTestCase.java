package org.opennms.netmgt.dao;

import java.util.Collection;

import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsNode;

public class CriteriaTestCase extends AbstractDaoTestCase {

    public void testSimple() {
        OnmsCriteria crit = new OnmsCriteria(OnmsNode.class);
        crit.add(Restrictions.eq("label", "node1"));
        
        Collection<OnmsNode> matching = getNodeDao().findMatching(crit);
        
        assertEquals("Expect a single node with label node1", 1, matching.size());
        
        OnmsNode node = matching.iterator().next();
        assertEquals("node1", node.getLabel());
        assertEquals(3, node.getIpInterfaces().size());
    }
    
    public void testComplicated() {
        OnmsCriteria crit = 
            new OnmsCriteria(OnmsNode.class)
            .createAlias("ipInterfaces", "iface")
            .add(Restrictions.eq("iface.ipAddress", "192.168.2.1"));
        
        Collection<OnmsNode> matching = getNodeDao().findMatching(crit);
        
        assertEquals("Expect a single node with an interface 192.168.2.1", 1, matching.size());
        
        OnmsNode node = matching.iterator().next();
        assertEquals("node2", node.getLabel());
        assertEquals(3, node.getIpInterfaces().size());
            
    }
}
