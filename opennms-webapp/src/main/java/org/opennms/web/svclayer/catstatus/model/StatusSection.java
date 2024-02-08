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
package org.opennms.web.svclayer.catstatus.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>StatusSection class.</p>
 *
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class StatusSection {

	private String m_name;
	private Collection<StatusCategory> m_categorylist;
	
	/**
	 * <p>Constructor for StatusSection.</p>
	 */
	public StatusSection(){
		
		m_categorylist = new ArrayList<>();
		
	}
	
	/**
	 * <p>setName</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(String name){
		m_name = name;
	}
	
	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * <p>getCategories</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<StatusCategory> getCategories() {
		return m_categorylist;
	}
	
	/**
	 * <p>addCategory</p>
	 *
	 * @param newCategory a {@link org.opennms.web.svclayer.catstatus.model.StatusCategory} object.
	 */
	public void addCategory(StatusCategory newCategory) {
		m_categorylist.add(newCategory);
	}
	
}
