/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
 * <p>Severity class.</p>
 *
 * @author mmigliore
 *
 * @since 1.8.1
 */
public class Severity implements Comparable<Severity> {
	private int id;
	private String label;
	private String color;

	private boolean flash = false; 
	/**
	 * <p>Constructor for Severity.</p>
	 *
	 * @param id a int.
	 * @param label a {@link java.lang.String} object.
	 * @param color a {@link java.lang.String} object.
	 */
	public Severity(int id, String label, String color) {
		super();
		this.id = id;
		this.label = label;
		this.color = color;
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * Compares the Severity to another in input by ids.
	 */
        @Override
	public int compareTo(Severity otherSev){
		Severity othSev = otherSev;
		if(this.id==othSev.getId()) return 0;
		else if(this.id<othSev.getId()) return -1;
		else  return 1;
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
	 * <p>Getter for the field <code>id</code>.</p>
	 *
	 * @return a int.
	 */
	public int getId() {
		return id;
	}
	/**
	 * <p>Setter for the field <code>id</code>.</p>
	 *
	 * @param id a int.
	 */
	public void setId(int id) {
		this.id = id;
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
	/**
	 * <p>isFlash</p>
	 *
	 * @return a boolean.
	 */
	public boolean isFlash() {
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
}
