package org.opennms.features.topology.plugins.topo;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.topology.api.support.AbstractHistoryManager;
import org.opennms.features.topology.api.support.SavedHistory;

public class MemoryHistoryManager extends AbstractHistoryManager {
    
    private Map<String, SavedHistory> m_historyMap = new HashMap<String, SavedHistory>(); 
    
    @Override
    protected void saveHistory(String userId, SavedHistory hist) {
        m_historyMap.put(hist.getFragment(), hist);
    }

    @Override
    protected SavedHistory getHistory(String userId, String fragmentId) {
        return m_historyMap.get(fragmentId);
    }

    @Override
    public String getHistoryForUser(String userId) {
        return null;
    }
}
