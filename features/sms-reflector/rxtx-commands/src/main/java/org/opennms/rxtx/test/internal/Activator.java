/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
	
	ServiceRegistration<CommandProvider> m_registration;
	RxtxCommands m_commands;
	
    /**
     * {@inheritDoc}
     *
     * Called whenever the OSGi framework starts our bundle
     */
        @Override
    public void start( BundleContext bc )
        throws Exception
    {
    	
    	m_commands = new RxtxCommands();
    	
        // Register our example service implementation in the OSGi service registry
        m_registration = bc.registerService( CommandProvider.class, m_commands, null );
    }

    /**
     * {@inheritDoc}
     *
     * Called whenever the OSGi framework stops our bundle
     */
        @Override
    public void stop( BundleContext bc )
        throws Exception
    {

    	m_registration.unregister();
    	m_registration = null;
    	

        m_commands.stop();
        m_commands = null;
    }
}

