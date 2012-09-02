package org.opennms.features.topology.app.internal;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class MenuConfigManagedService implements ManagedService {
    
    private CommandManager m_commandManager;

    public void setCommandManager(CommandManager commandManager) {
        m_commandManager = commandManager;
    }
    
    // Implements OSGi API
    @SuppressWarnings("unchecked")
    @Override
    public void updated(Dictionary properties) throws ConfigurationException {
        m_commandManager.updateMenuConfig(properties);
    }

}
