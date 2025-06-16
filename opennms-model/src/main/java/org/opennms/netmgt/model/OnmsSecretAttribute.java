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

/**
 * This is part of the 'secret' project from the 2005 Dev-Jam.  It will mostly
 * likely be replaced by or refactored into the new OnmsResource/OnmsAttribute
 * model classes.
 */
public class OnmsSecretAttribute {
	
	private String m_label;
	private String m_id;
	
	/**
	 * <p>Constructor for OnmsSecretAttribute.</p>
	 */
	public OnmsSecretAttribute() {
		
	}
	
	/**
	 * <p>Constructor for OnmsSecretAttribute.</p>
	 *
	 * @param id a {@link java.lang.String} object.
	 * @param label a {@link java.lang.String} object.
	 */
	public OnmsSecretAttribute(String id, String label) {
		m_id = id;
		m_label = label;
	}

	/**
	 * <p>getId</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getId() {
		return m_id;
	}
	/**
	 * <p>setId</p>
	 *
	 * @param id a {@link java.lang.String} object.
	 */
	public void setId(String id) {
		m_id = id;
	}
	
	/**
	 * <p>getLabel</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLabel() {
		return m_label;
	}
	/**
	 * <p>setLabel</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public void setLabel(String label) {
		m_label = label;
	}
	

}
