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
package org.opennms.nrtg.web.internal;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModelAndView {
	
	private final String m_viewName;
	private final Map<String, Object> m_model;

	public ModelAndView(String viewName) {
		m_viewName = viewName;
		m_model = new LinkedHashMap<String, Object>();
	}

	public void addObject(String name, Object modelObject) {
		m_model.put(name, modelObject);
	}
	
	public String getViewName() {
		return m_viewName;
	}
	
	public Map<String, Object> getModel() {
		return m_model;
	}

}
