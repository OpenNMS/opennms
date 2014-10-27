/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.map.config;

/**
 * <p>LinkStatus class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class LinkStatus {
	String id;
	String color;
	boolean flash;
	
	
	/**
	 * <p>Constructor for LinkStatus.</p>
	 *
	 * @param id a {@link java.lang.String} object.
	 * @param color a {@link java.lang.String} object.
	 * @param flash a boolean.
	 */
	public LinkStatus(String id, String color, boolean flash) {
		super();
		this.id = id;
		this.color = color;
		this.flash = flash;
	}
	
	/**
	 * <p>Getter for the field <code>color</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getColor() {
		return color;
	}
	/**
	 * <p>Setter for the field <code>color</code>.</p>
	 *
	 * @param color a {@link java.lang.String} object.
	 */
	public void setColor(String color) {
		this.color = color;
	}
	/**
	 * <p>Getter for the field <code>flash</code>.</p>
	 *
	 * @return a boolean.
	 */
	public boolean getFlash() {
		return flash;
	}
	/**
	 * <p>Setter for the field <code>flash</code>.</p>
	 *
	 * @param flash a boolean.
	 */
	public void setFlash(boolean flash) {
		this.flash = flash;
	}
	/**
	 * <p>Getter for the field <code>id</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getId() {
		return id;
	}
	/**
	 * <p>Setter for the field <code>id</code>.</p>
	 *
	 * @param id a {@link java.lang.String} object.
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	
	
}
