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

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.support.RemoteInvocation;

/**
 */
public class ServiceRegistryHttpInvokerProxyFactoryBean extends HttpInvokerProxyFactoryBean {

	public static final String ATTRIBUTE_INTERFACE_NAME = ServiceRegistryHttpInvokerProxyFactoryBean.class.getName() + ".interface";

	@Override
	public RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
		RemoteInvocation retval = super.createRemoteInvocation(methodInvocation);

		// Add the interface that is being used to access this service as an invocation attibute
		retval.addAttribute(ATTRIBUTE_INTERFACE_NAME, this.getServiceInterface().getName());

		return retval;
	}
}
