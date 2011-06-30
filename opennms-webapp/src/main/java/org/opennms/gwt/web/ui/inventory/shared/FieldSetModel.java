/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.gwt.web.ui.inventory.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a> 
 *
 */
public class FieldSetModel implements ContentElement, IsSerializable {

	private static final long serialVersionUID = 5771829135339245531L;

	String m_name = "";
	String m_value = "";
	String m_helpText = "";
	
	public FieldSetModel() {
		
	}
	
	public FieldSetModel(String name, String value, String helpText) {
		m_name = name;
		m_value = value;
		m_helpText = helpText;
	}

	/**
	 * @return
	 */
	public String getName() {
		return m_name;
	}
	public String getValue() {
		return m_value;
	}
	public String getHelpText() {
		return m_helpText;
	}

	/**
	 * @param value
	 */
	public void setValue(String value) {
		m_value = value;
	}
}
