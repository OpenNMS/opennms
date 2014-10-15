/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.topology;


/**
 * This class represents a destination in the network such as
 * an IP address or a physical port.
 * Also can represent a TCP Port
 * 
 * @author Antonio
 *
 */
public abstract class EndPoint {
	
	/** 
	 * The Element to which the End Point 
	 * belongs
	 *  
	 */
	private Element m_device;

	/**
	 * Only one Link for End Point is allowed
	 * 
	 */	
	private Link m_link;
	
	public Element getDevice() {
		return m_device;
	}

	public void setDevice(Element device) {
		m_device = device;
	}
	
	public Link getLink() {
		return m_link;
	}

	public void setLink(Link link) {
		m_link = link	;
	}
	
	public boolean hasLink() {
		return m_link != null;
	}

	public boolean hasElement() {
		return m_device != null;
	}
}
