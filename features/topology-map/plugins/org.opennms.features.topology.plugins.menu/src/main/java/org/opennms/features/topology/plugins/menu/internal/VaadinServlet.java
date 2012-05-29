package org.opennms.features.topology.plugins.menu.internal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;


public class VaadinServlet extends AbstractApplicationServlet {

    
	@Override
	protected Class<? extends Application> getApplicationClass() throws ClassNotFoundException {
		return DefaultMenu.class;
	}

	@Override
	protected Application getNewApplication(HttpServletRequest request)	throws ServletException {
		return new DefaultMenu();
	}


}
