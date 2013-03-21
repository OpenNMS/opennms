package org.opennms.features.topology.app.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts as a global manager of VertexProvider and EdgeProvider registrations.
 * It relays bind and unbind events to each GraphProvider.
 */
public class ProviderManager {
	
	public static interface ProviderListener {

		void edgeProviderAdded(EdgeProvider oldProvider,
				EdgeProvider newProvider);

		void edgeProviderRemoved(EdgeProvider removedProvider);

		void vertexProviderAdded(VertexProvider oldProvider,
				VertexProvider newProvider);

		void vertexProviderRemoved(VertexProvider removedProvider);
		
	}
	
	private static final Logger s_log = LoggerFactory.getLogger(ProviderManager.class);
	
	private final Map<String, VertexProvider> m_vertexProviders = new HashMap<String, VertexProvider>();
	private final Map<String, EdgeProvider> m_edgeProviders = new HashMap<String, EdgeProvider>();
	private final Set<ProviderListener> m_listeners = new CopyOnWriteArraySet<ProviderListener>();
	
	public Collection<VertexProvider> getVertexListeners() {
		return Collections.unmodifiableCollection(m_vertexProviders.values());
	}
	
	public Collection<EdgeProvider> getEdgeListeners() {
		return Collections.unmodifiableCollection(m_edgeProviders.values());
	}
	
	public synchronized void onEdgeProviderBind(EdgeProvider newProvider) {
		s_log.info("ProviderManager onEdgeProviderBind({}}", newProvider);
		try {
			EdgeProvider oldProvider = m_edgeProviders.put(newProvider.getEdgeNamespace(), newProvider);

			fireEdgeProviderAdded(oldProvider, newProvider);
		} catch (Throwable e) {
			LoggerFactory.getLogger(this.getClass()).warn("Exception during onEdgeProviderBind()", e);
		}
	}

	public synchronized void onEdgeProviderUnbind(EdgeProvider edgeProvider) {
		s_log.info("ProviderManager onEdgeProviderUnbind({}}", edgeProvider);
		if (edgeProvider == null) return;
		try {
			EdgeProvider removedProvider = m_edgeProviders.remove(edgeProvider.getEdgeNamespace());

			fireEdgeProviderRemoved(removedProvider);
		} catch (Throwable e) {
			LoggerFactory.getLogger(this.getClass()).warn("Exception during onEdgeProviderUnbind()", e);
		}
	}

	public synchronized void onVertexProviderBind(VertexProvider newProvider) {
		s_log.info("ProviderManager onVertexProviderBind({}}", newProvider);
		try {
			VertexProvider oldProvider = m_vertexProviders.put(newProvider.getVertexNamespace(), newProvider);

			fireVertexProviderAdded(oldProvider, newProvider);
		} catch (Throwable e) {
			LoggerFactory.getLogger(this.getClass()).warn("Exception during onVertexProviderBind()", e);
		}
	}

	public synchronized void onVertexProviderUnbind(VertexProvider vertexProvider) {
		s_log.info("ProviderManager onVertexProviderUnbind({}}", vertexProvider);
		if (vertexProvider == null) return;
		try {
			VertexProvider removedProvider = m_vertexProviders.remove(vertexProvider.getVertexNamespace());

			fireVertexProviderRemoved(removedProvider);
		} catch (Throwable e) {
			LoggerFactory.getLogger(this.getClass()).warn("Exception during onVertexProviderUnbind()", e);
		}
	}
    
    private void fireEdgeProviderAdded(EdgeProvider oldProvider, EdgeProvider newProvider) {
    	for(ProviderListener listener : m_listeners) {
    		listener.edgeProviderAdded(oldProvider, newProvider);
    	}
    }

    private void fireEdgeProviderRemoved(EdgeProvider removedProvider) {
    	for(ProviderListener listener : m_listeners) {
    		listener.edgeProviderRemoved(removedProvider);
    	}
	}

    private void fireVertexProviderAdded(VertexProvider oldProvider, VertexProvider newProvider) {
    	for(ProviderListener listener : m_listeners) {
    		listener.vertexProviderAdded(oldProvider, newProvider);
    	}
    }

    private void fireVertexProviderRemoved(VertexProvider removedProvider) {
    	for(ProviderListener listener : m_listeners) {
    		listener.vertexProviderRemoved(removedProvider);
    	}
	}
    
    public void addProviderListener(ProviderListener providerListener) {
    	m_listeners.add(providerListener);
    }
    
    public void removeProviderListener(ProviderListener providerListener) {
    	m_listeners.remove(providerListener);
    }
}
