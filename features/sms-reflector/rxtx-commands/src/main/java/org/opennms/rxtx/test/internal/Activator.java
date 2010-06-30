package org.opennms.rxtx.test.internal;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Extension of the default OSGi bundle activator
 *
 * @author ranger
 * @version $Id: $
 */
public final class Activator
    implements BundleActivator
{
	
	ServiceRegistration m_registration;
	RxtxCommands m_commands;
	
    /**
     * {@inheritDoc}
     *
     * Called whenever the OSGi framework starts our bundle
     */
    public void start( BundleContext bc )
        throws Exception
    {
    	
    	m_commands = new RxtxCommands();
    	
        // Register our example service implementation in the OSGi service registry
        m_registration = bc.registerService( CommandProvider.class.getName(), m_commands, null );
    }

    /**
     * {@inheritDoc}
     *
     * Called whenever the OSGi framework stops our bundle
     */
    public void stop( BundleContext bc )
        throws Exception
    {

    	m_registration.unregister();
    	m_registration = null;
    	

        m_commands.stop();
        m_commands = null;
    }
}

