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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.topology.api.Operation;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.common.collect.Lists;

@Command(scope = "topo", name = "listoperations", description="Lists the available OpenNMS topology operations.")
@Service
public class OperationListShellCommand implements Action {

    @Reference
    BundleContext bundleContext;

    @Override
    public Object execute() throws Exception {

    	final List<Operation> operations = Lists.newArrayList();
    	final Map<Operation,Map<String,Object>> properties = new HashMap<>();

    	final Collection<ServiceReference<Operation>> services = this.bundleContext.getServiceReferences(Operation.class, null);
        if (services == null) return null;

        for (final ServiceReference<Operation> sr : services) {
    		final Operation operation = this.bundleContext.getService(sr);
    		if (operation == null) continue;

    		operations.add(operation);

			final Map<String,Object> props = new TreeMap<>();
    		for (final String key : sr.getPropertyKeys()) {
    			props.put(key, sr.getProperty(key));
    		}
    		properties.put(operation, props);
    	}

    	// Output
    	for (final Operation operation : operations) {
    		final String operationClass = operation.getClass().getName();
			System.out.println("    " + makeLine(operationClass));
			System.out.println("    Class: " + operationClass);
    		System.out.println("    ID:    " + operation.getId());

    		final Map<String,Object> props = properties.get(operation);
    		if (!props.isEmpty()) {
	    		System.out.println("    Service Properties:");
	    		for (final String key : props.keySet()) {
	    			final Object object = props.get(key);
	    			final String value = (object instanceof Object[])? Arrays.toString((Object[])object) : object.toString();
					System.out.println("        " + key + "=" + value);
	    		}
    		}
    	}

        return null;
    }

	private static String makeLine(final String s) {
		return new String(new char[s.length()]).replace("\0", "-");
	}
}
