package org.opennms.features.topology.app.internal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.ops4j.pax.vaadin.ApplicationFactory;

import com.vaadin.Application;

public class TopologyWidgetTestApplicationFactory implements ApplicationFactory {
    
	private CommandManager m_commandManager = new CommandManager();
	
	public CommandManager getCommandManager() {
        return m_commandManager;
    }

    public void setCommandManager(CommandManager commandManager) {
        m_commandManager = commandManager;
    }

    @Override
	public Application createApplication(HttpServletRequest request) throws ServletException {
		return new TopologyWidgetTestApplication(m_commandManager);
	}

	@Override
	public Class<? extends Application> getApplicationClass() throws ClassNotFoundException {
		return TopologyWidgetTestApplication.class;
	}

}
