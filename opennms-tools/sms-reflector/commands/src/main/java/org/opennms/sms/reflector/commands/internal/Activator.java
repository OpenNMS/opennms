package org.opennms.sms.reflector.commands.internal;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


/**
 * Extension of the default OSGi bundle activator
 */
public final class Activator
    implements BundleActivator
{
    
    ServiceRegistration m_registration;
    SmsCommands m_commandService;
    /**
     * Called whenever the OSGi framework starts our bundle
     */
    public void start( BundleContext bc )
        throws Exception
    {
        System.out.println( "STARTING org.opennms.sms.reflector.commands" );

        //Dictionary props = new Properties();
        // add specific service properties here...

        System.out.println( "REGISTER org.opennms.sms.reflector.commands.GetPropsCommand" );
        
        m_commandService = new SmsCommands(bc);
        // Register our example service implementation in the OSGi service registry
        m_registration = bc.registerService( CommandProvider.class.getName(), new SmsCommands(bc), null );
    }

    /**
     * Called whenever the OSGi framework stops our bundle
     */
    public void stop( BundleContext bc )
        throws Exception
    {
        System.out.println( "STOPPING org.opennms.sms.reflector.commands" );

        m_registration.unregister();
        m_commandService.stopService();
    }
}

