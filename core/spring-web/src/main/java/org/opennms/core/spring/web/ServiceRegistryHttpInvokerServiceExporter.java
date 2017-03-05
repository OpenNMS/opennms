/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
