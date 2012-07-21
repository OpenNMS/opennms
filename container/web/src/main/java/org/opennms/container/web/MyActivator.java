package org.opennms.container.web;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public final class MyActivator implements BundleActivator {

	@Override
	public void start(BundleContext arg0) throws Exception {
		System.err.println("My Activator is started!!!");
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		System.err.println("My Activator is stopped!!!");
	}
	
}