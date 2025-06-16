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
package org.opennms.netmgt.model;

import java.util.List;

/**
 * <p>ServiceSelector class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jason.aras@fastsearch.com">Jason Ayers</a>
 */
public class ServiceSelector {
	private String m_filterRule;
	private List<String> m_serviceNames;
	
	
	/**
	 * <p>Constructor for ServiceSelector.</p>
	 *
	 * @param filterRule a {@link java.lang.String} object.
	 * @param serviceNames a {@link java.util.List} object.
	 */
	public ServiceSelector(String filterRule, List<String> serviceNames) {
		m_filterRule = filterRule;
		m_serviceNames = serviceNames;
	}
	
	/**
	 * <p>getFilterRule</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFilterRule() {
		return m_filterRule;
	}
	
	/**
	 * <p>getServiceNames</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<String> getServiceNames() {
		return m_serviceNames;
	}
}
