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
 * <p>Status class.</p>
 *
 * @author mmigliore
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 * @since 1.8.1
 */
public class Status implements Comparable{
	private int id;
	private String uei;
	private String color;
	private String text;
	
	
	
	
	
	
	/**
	 * <p>Constructor for Status.</p>
	 *
	 * @param id a int.
	 * @param uei a {@link java.lang.String} object.
	 * @param color a {@link java.lang.String} object.
	 * @param text a {@link java.lang.String} object.
	 */
	public Status(int id, String uei, String color, String text) {
		super();
		this.id = id;
		this.uei = uei;
		this.color = color;
		this.text = text;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Compares the Status to another in input by id.
	 */
	public int compareTo(Object otherStatus){
		Status othStat = (Status) otherStatus;
		if(this.id==othStat.getId()) return 0;
		else if(this.id<othStat.getId()) return -1;
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
	 * <p>Getter for the field <code>text</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getText() {
		return text;
	}
	/**
	 * <p>Setter for the field <code>text</code>.</p>
	 *
	 * @param text a {@link java.lang.String} object.
	 */
	public void setText(String text) {
		this.text = text;
	}
	/**
	 * <p>Getter for the field <code>uei</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getUei() {
		return uei;
	}
	/**
	 * <p>Setter for the field <code>uei</code>.</p>
	 *
	 * @param uei a {@link java.lang.String} object.
	 */
	public void setUei(String uei) {
		this.uei = uei;
	}

}
