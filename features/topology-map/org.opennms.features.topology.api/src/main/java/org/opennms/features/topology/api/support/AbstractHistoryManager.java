package org.opennms.features.topology.api.support;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HistoryManager;
import org.opennms.features.topology.api.HistoryOperation;

public abstract class AbstractHistoryManager implements HistoryManager {

    private final List<HistoryOperation> m_operations = new ArrayList<HistoryOperation>();

    @Override
    public void applyHistory(String fragment, GraphContainer container) {
        SavedHistory hist = getHistory(fragment);
        if (hist != null) {
            hist.apply(container, m_operations);
        }
    }

    @Override
    public String create(GraphContainer graphContainer) {
        SavedHistory history = new SavedHistory(graphContainer, m_operations);
        saveHistory(history);
        return history.getFragment();
    }
    
    protected abstract SavedHistory getHistory(String fragment);

    protected abstract void saveHistory(SavedHistory history);

    @Override
	public void onBind(HistoryOperation operation) {
		m_operations.add(operation);
	}

    @Override
	public void onUnbind(HistoryOperation operation) {
		m_operations.remove(operation);
	}

}
