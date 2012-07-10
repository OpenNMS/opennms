package org.opennms.features.topology.app.internal;

import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.opennms.features.topology.api.TopologyProvider;
import org.ops4j.pax.vaadin.ApplicationFactory;

import com.vaadin.Application;

public class TopologyWidgetTestApplicationFactory implements ApplicationFactory {
    
	private CommandManager m_commandManager = new CommandManager();
    private TopologyProvider m_topologyProvider;
	
	public CommandManager getCommandManager() {
        return m_commandManager;
    }

    public void setCommandManager(CommandManager commandManager) {
        m_commandManager = commandManager;
    }

    @Override
	public Application createApplication(HttpServletRequest request) throws ServletException {
		return new TopologyWidgetTestApplication(m_commandManager, getTopologyProvider());
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
    
    public void setToplevelMenuOrder(String menuOrder) {
        List<String> menuOrderList = Arrays.asList(menuOrder.split(","));
        getCommandManager().setTopLevelMenuOrder(menuOrderList);
    }

}
