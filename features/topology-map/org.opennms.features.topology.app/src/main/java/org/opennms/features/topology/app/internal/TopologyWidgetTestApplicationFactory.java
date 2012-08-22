package org.opennms.features.topology.app.internal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;
import org.ops4j.pax.vaadin.ApplicationFactory;

import com.vaadin.Application;

public class TopologyWidgetTestApplicationFactory implements ApplicationFactory {
    
	private CommandManager m_commandManager = new CommandManager();
	private IconRepositoryManager m_iconRepositoryManager = new IconRepositoryManager();
    private TopologyProvider m_topologyProvider;
    private String m_themeName = "reindeer";
	
	public CommandManager getCommandManager() {
        return m_commandManager;
    }

    public void setCommandManager(CommandManager commandManager) {
        m_commandManager = commandManager;
    }

    @Override
	public Application createApplication(HttpServletRequest request) throws ServletException {
		TopologyWidgetTestApplication application = new TopologyWidgetTestApplication(m_commandManager, getTopologyProvider(), m_iconRepositoryManager);
		application.setTheme(m_themeName);
        return application;
	}

	@Override
	public Class<? extends Application> getApplicationClass() throws ClassNotFoundException {
		return TopologyWidgetTestApplication.class;
	}

    public TopologyProvider getTopologyProvider() {
        return m_topologyProvider;
    }

    public void setTopologyProvider(TopologyProvider topologyProvider) {
        m_topologyProvider = topologyProvider;
    }

    public IconRepositoryManager getIconRepositoryManager() {
        return m_iconRepositoryManager;
    }

    public void setIconRepositoryManager(IconRepositoryManager iconRepositoryManager) {
        m_iconRepositoryManager = iconRepositoryManager;
    }
    
    public void setTheme(String themeName) {
        m_themeName = themeName;
    }
    
}
