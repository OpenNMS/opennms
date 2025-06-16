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

@Command(scope = "opennms", name = "topo-list-operations", description="Lists the available OpenNMS topology operations.")
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
