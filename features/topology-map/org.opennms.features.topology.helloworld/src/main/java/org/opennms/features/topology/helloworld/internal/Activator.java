package org.opennms.features.topology.helloworld.internal;

import java.util.Properties;

import javax.servlet.Servlet;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		registerServlet(context, "/helloworld");
		registerServlet(context, "/test/helloworld");
		registerServlet(context, "/test/other/helloworld");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}
	
	
	public void registerServlet(BundleContext context, String alias) {
		
		Servlet s = new HelloWorldServlet(alias);
		
		Properties info = new Properties();
		info.put("alias", alias);
		
		context.registerService(Servlet.class.getName(), s, info);
	}

}
