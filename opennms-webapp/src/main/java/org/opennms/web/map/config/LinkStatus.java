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
 * Created: February 2, 2007
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
package org.opennms.web.map.config;

/**
 * <p>LinkStatus class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.6.12
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
