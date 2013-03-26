package org.opennms.features.topology.plugins.ncs;

import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class MockNCSCompontentRepositoryActivator implements BundleActivator {

    private ServiceRegistration<NCSComponentRepository> m_registration;

    @Override
    public void start(final BundleContext context) throws Exception {
        final MockNCSComponentRepository repo = new MockNCSComponentRepository();
        m_registration = context.registerService(NCSComponentRepository.class, repo, null);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        m_registration.unregister();
    }
    
}