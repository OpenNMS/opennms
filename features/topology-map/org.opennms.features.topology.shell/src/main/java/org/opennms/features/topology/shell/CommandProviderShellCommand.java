/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.shell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.reflect.MapEntry;
import org.osgi.service.blueprint.reflect.ServiceMetadata;
import org.osgi.service.blueprint.reflect.ValueMetadata;

@Command(scope = "onms", name = "listcommands", description="Lists the available shell commands and their providers.")
public class CommandProviderShellCommand extends OsgiCommandSupport {

    @Override
    protected Object doExecute() throws Exception {
    	final Collection<ServiceReference<BlueprintContainer>> services = this.bundleContext.getServiceReferences(BlueprintContainer.class, null);
    	for (final ServiceReference<BlueprintContainer> sr : services) {

    		final List<String> commands = new ArrayList<String>();
    		
    		final BlueprintContainer container = this.bundleContext.getService(sr);
			final Collection<ServiceMetadata> metadata = container.getMetadata(ServiceMetadata.class);
    		for (final ServiceMetadata data : metadata) {
    			String scope = null;
    			String function = null;

    			// Implements OSGi API
    			@SuppressWarnings("rawtypes")
				final List properties = data.getServiceProperties();
    			for (final Object o : properties) {
    				final MapEntry entry = (MapEntry)o;
    				String key, value;
    				if (entry.getKey() instanceof ValueMetadata) {
    					final ValueMetadata vmKey = (ValueMetadata)entry.getKey();
    					key = vmKey.getStringValue();
    				} else {
    					key = entry.getKey().toString();
    				}
    				if (entry.getValue() instanceof ValueMetadata) {
    					final ValueMetadata vmValue = (ValueMetadata)entry.getValue();
    					value = vmValue.getStringValue();
    				} else {
    					value = entry.getValue().toString();
    				}
    				if (key.equals("osgi.command.scope")) {
    					scope = value;
    				} else if (key.equals("osgi.command.function")) {
    					function = value;
    				}
    			}
    			if (scope != null && function != null) {
    				commands.add(scope + ":" + function);
    			}
    		}
    		
    		if (commands.size() > 0) {
	    		System.out.println(sr.getBundle());
	    		
    			for (final String command : commands) {
    				System.out.println("    " + command);
    			}
	    		
	    		System.out.println();
    		}
    	}

    	return null;
    }
}
