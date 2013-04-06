package org.opennms.features.topology.api.support;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HistoryManager;
import org.opennms.features.topology.api.HistoryOperation;
import org.slf4j.LoggerFactory;

public abstract class AbstractHistoryManager implements HistoryManager {

	/**
	 * TODO: Fix concurrent access to this list
	 */
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
	public synchronized void onBind(HistoryOperation operation) {
    	try {
    		m_operations.add(operation);
        } catch (Throwable e) {
            LoggerFactory.getLogger(this.getClass()).warn("Exception during onBind()", e);
        }
	}

    @Override
	public synchronized void onUnbind(HistoryOperation operation) {
    	try {
    		m_operations.remove(operation);
        } catch (Throwable e) {
            LoggerFactory.getLogger(this.getClass()).warn("Exception during onUnbind()", e);
        }
	}

}
