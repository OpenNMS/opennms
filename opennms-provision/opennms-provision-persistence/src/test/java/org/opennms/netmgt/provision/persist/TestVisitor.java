package org.opennms.netmgt.provision.persist;

import java.util.ArrayList;
import java.util.List;


public class TestVisitor extends AbstractRequisitionVisitor {
    
    private final List<OnmsNodeRequisition> m_nodeReqs = new ArrayList<OnmsNodeRequisition>();

    @Override
    public void completeNode(OnmsNodeRequisition nodeReq) {
        m_nodeReqs.add(nodeReq);
    }

    public List<OnmsNodeRequisition> getNodeReqs() {
        return m_nodeReqs;
    }
}