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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.aries.blueprint.NamespaceHandler;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@Command(scope = "opennms", name = "list-namespaces", description="Lists the available blueprint namespaces and their providers.")
@Service
public class BlueprintNamespaceShellCommand implements Action {

    @Reference
    BundleContext bundleContext;

    @Override
    public Object execute() throws Exception {

        final Collection<ServiceReference<NamespaceHandler>> services = this.bundleContext.getServiceReferences(NamespaceHandler.class, null);
    	for (final ServiceReference<NamespaceHandler> sr : services) {
    		final Bundle bundle = sr.getBundle();
    		final Object rawNamespaces = sr.getProperty("osgi.service.blueprint.namespace");

    		final ArrayList<String> namespaces = new ArrayList<>();
    		if (rawNamespaces instanceof String) {
    			namespaces.add((String)rawNamespaces);
    		} else if (rawNamespaces instanceof Object[]) {
    			for (final Object namespace : (Object[])rawNamespaces) {
    				namespaces.add(namespace.toString());
    			}
    		} else if (rawNamespaces instanceof String[]) {
    			for (final String namespace : (String[])rawNamespaces) {
    				namespaces.add(namespace);
    			}
    		} else {
    			System.err.println("Hmm, not sure how to interpret: " + rawNamespaces);
    		}

    		System.out.println(bundle.toString());
    		for (final Object namespace : namespaces) {
    			System.out.println("    " + namespace);
    		}
    		System.out.println();
    	}
        return null;
    }
}
