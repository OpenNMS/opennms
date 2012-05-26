package org.opennms.features.vaadin.app;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.ops4j.pax.vaadin.ApplicationFactory;

import com.vaadin.Application;

public class TopologyWidgetTestApplicationFactory implements ApplicationFactory {

	@Override
	public Application createApplication(HttpServletRequest request) throws ServletException {
		return new TopologyWidgetTestApplication();
	}

	@Override
	public Class<? extends Application> getApplicationClass() throws ClassNotFoundException {
		return TopologyWidgetTestApplication.class;
	}

}
