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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.features.topology.api.Operation;
import org.osgi.framework.ServiceReference;

@Command(scope = "topo", name = "listoperations", description="Lists the available OpenNMS topology operations.")
public class OperationListShellCommand extends OsgiCommandSupport {
//	public static final Comparator<Operation> OPERATION_COMPARATOR = new Comparator<Operation>() {
//		@Override
//		public int compare(final Operation a, final Operation b) {
//			final int comp = a.getId().compareTo(b.getId());
//			return comp == 0? a.getLabel().compareTo(b.getLabel()) : comp;
//		}
//	};

    @Override
    protected Object doExecute() throws Exception {

    	final Set<Operation> operations = new TreeSet<Operation>();
    	final Map<Operation,Map<String,Object>> properties = new HashMap<Operation,Map<String,Object>>();

    	final Collection<ServiceReference<Operation>> services = this.bundleContext.getServiceReferences(Operation.class, null);
        if (services == null) return null;

        for (final ServiceReference<Operation> sr : services) {
    		final Operation operation = this.bundleContext.getService(sr);
    		if (operation == null) continue;

    		operations.add(operation);

			final Map<String,Object> props = new TreeMap<String,Object>();
    		for (final String key : sr.getPropertyKeys()) {
    			props.put(key, sr.getProperty(key));
    		}
    		properties.put(operation, props);
    	}

    	for (final Operation operation : operations) {
    		final String operationClass = operation.getClass().getName();
			System.out.println("    " + operationClass);
    		System.out.println("    " + makeLine(operationClass));
    		System.out.println();
    		
    		System.out.println("    ID:    " + operation.getId());
    		//System.out.println("    Label: " + operation.getLabel());
    		System.out.println();

    		final Map<String,Object> props = properties.get(operation);

    		if (props.size() > 0) {
	    		System.out.println("    Service Properties:");
	    		System.out.println("    " + makeLine("Service Properties:"));
	    		System.out.println();
	    		
	    		for (final String key : props.keySet()) {
	    			final Object object = props.get(key);
	    			final String value = (object instanceof Object[])? Arrays.toString((Object[])object) : object.toString();
					System.out.println("        " + key + "=" + value);
	    		}
	    		
	    		System.out.println();
    		}
    	}

        return null;
    }

	private String makeLine(final String s) {
		return new String(new char[s.length()]).replace("\0", "-");
	}
}
