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
package org.opennms.core.spring.web;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.soa.ServiceRegistry;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.web.util.NestedServletException;

/**
 * This class extends the Spring {@link HttpInvokerServiceExporter} class to provide the ability
 * to proxy any interface that has a provider in the OpenNMS SOA {@link ServiceRegistry}. It must
 * be used in tandem with the {@link ServiceRegistryHttpInvokerProxyFactoryBean} because that client class
 * appends the Java interface name to the {@link RemoteInvocation}. Otherwise, this class does not
 * have the interface name and cannot pick the correct provider out of the {@link ServiceRegistry}.
 * 
 * @author Seth
 */
public class ServiceRegistryHttpInvokerServiceExporter extends HttpInvokerServiceExporter {
	
	private ServiceRegistry serviceRegistry;
	
	public ServiceRegistryHttpInvokerServiceExporter() {
		super.setServiceInterface(ServiceRegistry.class);
	}

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		super.setService(serviceRegistry);
		this.serviceRegistry = serviceRegistry;
	}

	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {
			RemoteInvocation invocation = readRemoteInvocation(request);
			Serializable interfaceNameObject = invocation.getAttribute(ServiceRegistryHttpInvokerProxyFactoryBean.ATTRIBUTE_INTERFACE_NAME);
			if (interfaceNameObject == null) {
				throw new NestedServletException("Interface name attribute not found. This class can only service requests to a " + ServiceRegistryHttpInvokerProxyFactoryBean.class.getSimpleName() + " client.");
			} else {
				String interfaceName = (String)interfaceNameObject;

				try {
					//RemoteInvocationResult result = invokeAndCreateResult(invocation, getProxy());

					// TODO: Use a method similar to {@link RemoteExporter#getProxyForService()} to create an
					// interface proxy that masks any other methods on the remotely invoked object.
					RemoteInvocationResult result = invokeAndCreateResult(invocation, serviceRegistry.findProvider(Class.forName(interfaceName)));

					writeRemoteInvocationResult(request, response, result);
				} catch (IllegalArgumentException e) {
					throw new NestedServletException("No provider registered for interface " + interfaceName, e);
				}	
			}
		} catch (ClassNotFoundException e) {
			throw new NestedServletException("Class not found during deserialization", e);
		}
	}
}
