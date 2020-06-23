/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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
