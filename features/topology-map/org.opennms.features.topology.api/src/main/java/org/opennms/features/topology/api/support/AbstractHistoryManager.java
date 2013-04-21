package org.opennms.features.topology.api.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HistoryManager;
import org.opennms.features.topology.api.HistoryOperation;
import org.slf4j.LoggerFactory;

public abstract class AbstractHistoryManager implements HistoryManager {

    private final List<HistoryOperation> m_operations = new CopyOnWriteArrayList<HistoryOperation>();

    @Override
    public void applyHistory(String userId, String fragment, GraphContainer container) {
        SavedHistory hist = getHistory(userId, fragment);
        if (hist != null) {
            hist.apply(container, m_operations);
        }
    }

    @Override
    public String create(String userId, GraphContainer graphContainer) {
        SavedHistory history = new SavedHistory(graphContainer, m_operations);
        saveHistory(userId, history);
        return history.getFragment();
    }
    
    protected abstract SavedHistory getHistory(String userId, String fragment);

    protected abstract void saveHistory(String userId, SavedHistory history);

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
