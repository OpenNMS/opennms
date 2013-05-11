/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.layout.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.opennms.features.topology.plugins.layout.ExampleService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

/**
 * Extension of the default OSGi bundle activator
 */
public final class ExampleActivator
    implements BundleActivator
{
    /**
     * Called whenever the OSGi framework starts our bundle
     */
    @Override
    public void start( BundleContext bc )
        throws Exception
    {
        LoggerFactory.getLogger(getClass()).debug("STARTING {}", ExampleService.class.getName());

        Dictionary<String,Object> props = new Hashtable<String,Object>();
        // add specific service properties here...

        LoggerFactory.getLogger(getClass()).debug("REGISTER {}", ExampleService.class.getName());

        // Register our example service implementation in the OSGi service registry
        bc.registerService( ExampleService.class, new ExampleServiceImpl(), props );
    }

    /**
     * Called whenever the OSGi framework stops our bundle
     */
    @Override
    public void stop( BundleContext bc )
        throws Exception
    {
        LoggerFactory.getLogger(getClass()).debug("STOPPING {}", ExampleService.class.getName());

        // no need to unregister our service - the OSGi framework handles it for us
    }
}

