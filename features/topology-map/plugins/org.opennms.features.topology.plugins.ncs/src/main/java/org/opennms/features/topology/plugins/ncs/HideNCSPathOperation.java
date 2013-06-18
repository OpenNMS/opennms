package org.opennms.features.topology.plugins.ncs;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.ncs.internal.NCSCriteriaServiceManager;

public class HideNCSPathOperation implements Operation {

    private NCSCriteriaServiceManager m_serviceManager;
    
    @Override
    public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
        String sessionId = operationContext.getGraphContainer().getSessionId();
        if(m_serviceManager.isCriteriaRegistered("ncsPath", sessionId)) {
            m_serviceManager.unregisterCriteria("ncsPath", sessionId);
        }
        
        
        return null;
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        String sessionId = operationContext.getGraphContainer().getSessionId();
        if(m_serviceManager.isCriteriaRegistered("ncsPath", sessionId)) {
            return true;
        }
        
        return false;
    }

    @Override
    public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
        String sessionId = operationContext.getGraphContainer().getSessionId();
        if(m_serviceManager.isCriteriaRegistered("ncsPath", sessionId)) {
            return true;
        }
        return false;
    }

    @Override
    public String getId() {
        return null;
    }

    public NCSCriteriaServiceManager getNcsCriteriaServiceManager() {
        return m_serviceManager;
    }

    public void setNcsCriteriaServiceManager(NCSCriteriaServiceManager serviceManager) {
        m_serviceManager = serviceManager;
    }

}
