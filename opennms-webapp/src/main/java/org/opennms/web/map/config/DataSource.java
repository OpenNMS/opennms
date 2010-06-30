/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 17, 2007
 *
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

/**
 * <p>DataSource class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.6.12
 */
package org.opennms.web.map.config;

import java.util.HashMap;
public class DataSource {
	String label;
	String implClass;
	HashMap param;
	Filter[] filters;
	

	/**
	 * <p>Constructor for DataSource.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 * @param implClass a {@link java.lang.String} object.
	 * @param param a {@link java.util.HashMap} object.
	 * @param filters an array of {@link org.opennms.web.map.config.Filter} objects.
	 */
	public DataSource(String label, String implClass, HashMap param, Filter[] filters) {
		super();
		this.label=label;
		this.implClass = implClass;
		this.param = param;
		this.filters = filters;
	}
	
	/**
	 * <p>Constructor for DataSource.</p>
	 */
	public DataSource(){
		
	}

	/**
	 * <p>Getter for the field <code>filters</code>.</p>
	 *
	 * @return an array of {@link org.opennms.web.map.config.Filter} objects.
	 */
	public Filter[] getFilters() {
		return filters;
	}

	/**
	 * <p>Setter for the field <code>filters</code>.</p>
	 *
	 * @param filters an array of {@link org.opennms.web.map.config.Filter} objects.
	 */
	public void setFilters(Filter[] filters) {
		this.filters = filters;
	}

	/**
	 * <p>Getter for the field <code>implClass</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getImplClass() {
		return implClass;
	}

	/**
	 * <p>Setter for the field <code>implClass</code>.</p>
	 *
	 * @param implClass a {@link java.lang.String} object.
	 */
	public void setImplClass(String implClass) {
		this.implClass = implClass;
	}

	/**
	 * <p>Getter for the field <code>param</code>.</p>
	 *
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap getParam() {
		return param;
	}

	/**
	 * <p>Setter for the field <code>param</code>.</p>
	 *
	 * @param param a {@link java.util.HashMap} object.
	 */
	public void setParam(HashMap param) {
		this.param = param;
	}

	/**
	 * <p>Getter for the field <code>label</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * <p>Setter for the field <code>label</code>.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	
	
}
