package org.opennms.features.topology.api.support;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HistoryManager;

public abstract class AbstractHistoryManager implements HistoryManager {

    @Override
    public void applyHistory(String fragmentId, GraphContainer container) {
        getHistoryForFragment(fragmentId, container);
    }

    @Override
    public String create(GraphContainer graphContainer) {
        SavedHistory hist = new SavedHistory(graphContainer);
        save(hist);
        return hist.getFragment();
    }
    
    public void getHistoryForFragment(String fragmentId, GraphContainer graphContainer) {
        SavedHistory hist = getHistory(fragmentId);
        if (hist != null) {
            hist.apply(graphContainer);
        }
        
    }
    
    protected abstract SavedHistory getHistory(String fragmentId);

    protected abstract void save(SavedHistory hist);

}
