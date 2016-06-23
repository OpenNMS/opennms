package org.opennms.container.jaas;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
    @Override
    public synchronized void start(final BundleContext context) throws Exception {
        OpenNMSLoginModule.setContext(context);
    }

    @Override
    public synchronized void stop(final BundleContext context) throws Exception {
    }
}
