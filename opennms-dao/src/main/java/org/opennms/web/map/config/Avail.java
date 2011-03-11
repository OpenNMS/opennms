//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
/*
 * Created on 11-lug-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.config;

/**
 * <p>Avail class.</p>
 *
 * @author mmigliore
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 * @since 1.8.1
 */
@SuppressWarnings("unchecked")
public class Avail implements Comparable{
	private int id;
	private int min;
	private String color;
	private boolean flash = false;
	
	/**
	 * <p>Constructor for Avail.</p>
	 *
	 * @param id a int.
	 * @param min a int.
	 * @param color a {@link java.lang.String} object.
	 */
	public Avail(int id, int min, String color) {
		this.id = id;
		this.min = min;
		this.color = color;
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * Compares the Avail to another in input by min.
	 */
	public int compareTo(Object otherAvail){
		Avail othAvail = (Avail) otherAvail;
		if(this.min==othAvail.getMin()) return 0;
		else if(this.min<othAvail.getMin()) return -1;
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
	 * <p>Getter for the field <code>min</code>.</p>
	 *
	 * @return a int.
	 */
	public int getMin() {
		return min;
	}
	/**
	 * <p>Setter for the field <code>min</code>.</p>
	 *
	 * @param min a int.
	 */
	public void setMin(int min) {
		this.min = min;
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
