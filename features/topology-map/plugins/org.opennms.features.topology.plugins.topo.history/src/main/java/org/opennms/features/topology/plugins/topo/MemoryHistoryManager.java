package org.opennms.features.topology.plugins.topo;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.topology.api.support.AbstractHistoryManager;
import org.opennms.features.topology.api.support.SavedHistory;

public class MemoryHistoryManager extends AbstractHistoryManager {
    
    private Map<String, SavedHistory> m_historyMap = new HashMap<String, SavedHistory>(); 
    
    @Override
    protected void save(SavedHistory hist) {
        m_historyMap.put(hist.getFragment(), hist);
    }

    @Override
    protected SavedHistory getHistory(String fragmentId) {
        SavedHistory hist = null;
        if(m_historyMap.containsKey(fragmentId)) {
            hist = m_historyMap.get(fragmentId);
        }
        return hist;
    }

}
