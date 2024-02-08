/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

