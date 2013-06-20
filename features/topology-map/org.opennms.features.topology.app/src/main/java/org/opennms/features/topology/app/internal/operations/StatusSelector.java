package org.opennms.features.topology.app.internal.operations;

import org.opennms.features.topology.api.AbstractCheckedOperation;
import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.LoggerFactory;

import java.util.*;

public class StatusSelector {
    private static class StatusSelectorOperation extends AbstractCheckedOperation{

        private StatusProvider m_statusProvider;
        private Map<?,?> m_metaData;

        public StatusSelectorOperation(StatusProvider statusProvider, Map<?, ?> metaData) {
            m_statusProvider = statusProvider;
            m_metaData = metaData;
        }

        @Override
        public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
            execute(operationContext.getGraphContainer());
            return null;
        }

        private void execute(GraphContainer container) {
            LoggerFactory.getLogger(getClass()).debug("Active status provider is: {}", m_statusProvider);
            if(isChecked(container)) {
                container.setStatusProvider(StatusProvider.NULL);
            } else {
                container.setStatusProvider(m_statusProvider);
            }
        }

        @Override
        public boolean display(List<VertexRef> targets, OperationContext operationContext) {
            return true;
        }

        @Override
        public String getId() {
            return getLabel();
        }

        @Override
        protected boolean isChecked(GraphContainer container) {
            StatusProvider activeStatusProvider = container.getStatusProvider();
            if (activeStatusProvider == null) container.setStatusProvider(m_statusProvider); // enable this status-provider
            return !StatusProvider.NULL.equals(activeStatusProvider)  // not NULL-Provider
                    && m_statusProvider.equals(activeStatusProvider); // but selected
        }

        @Override
        public Map<String, String> createHistory(GraphContainer container){
            return Collections.singletonMap(this.getClass().getName() + "." + getLabel(), Boolean.toString(isChecked(container)));
        }

        @Override
        public void applyHistory(GraphContainer container, Map<String, String> settings) {
            if("true".equals(settings.get(this.getClass().getName() + "." + getLabel()))) {
                execute(container);
            }
        }

        public String getLabel() {
            return m_metaData.get("label") == null ? "No Label for Status Provider" : (String) m_metaData.get("label");
        }
    }

    private BundleContext m_bundleContext;
    private final Map<StatusProvider, StatusSelectorOperation> m_operations = new HashMap<StatusProvider, StatusSelectorOperation>();
    private final Map<StatusProvider, ServiceRegistration<CheckedOperation>> m_registrations = new HashMap<StatusProvider, ServiceRegistration<CheckedOperation>>();

    public void setBundleContext(BundleContext bundleContext) {
        m_bundleContext = bundleContext;
    }

    public synchronized void addStatusProvider(StatusProvider statusProvider, Map<?,?> metaData) {

        LoggerFactory.getLogger(getClass()).debug("Adding status provider: " + statusProvider);

        StatusSelectorOperation operation = new StatusSelectorOperation(statusProvider, metaData);
        m_operations.put(statusProvider, operation);

        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("operation.menuLocation", "View");
        properties.put("operation.label", operation.getLabel()+"?group=status");

        ServiceRegistration<CheckedOperation> reg = m_bundleContext.registerService(CheckedOperation.class, operation, properties);

        m_registrations.put(statusProvider, reg);
    }
    
    public synchronized void removeStatusProvider(StatusProvider statusProvider, Map<?,?> metaData) {
        try {
            LoggerFactory.getLogger(getClass()).debug("Removing status provider: {}", statusProvider);

            m_operations.remove(statusProvider);
            ServiceRegistration<CheckedOperation> reg = m_registrations.remove(statusProvider);
            if(reg != null) {
                reg.unregister();
            }
        } catch (Throwable e) {
            LoggerFactory.getLogger(this.getClass()).warn("Exception during removeStatusProvider()", e);
        }
    }
}
