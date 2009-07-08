/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.map.config;

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class Link {
	String speed; 
	String text; 
	String width;
	int dasharray=-1;
	int snmptype;
	int id;
	
	
	
	public Link(int id,String speed, String text, String width, int dasharray,int snmptype) {
		super();
		this.id=id;
		this.speed = speed;
		this.text = text;
		this.width = width;
		this.dasharray = dasharray;
		this.snmptype = snmptype;
	}
	
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDasharray() {
		return dasharray;
	}
	public void setDasharray(int dasharray) {
		this.dasharray = dasharray;
	}
	public String getSpeed() {
		return speed;
	}
	public void setSpeed(String speed) {
		this.speed = speed;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public int getSnmptype() {
		return snmptype;
	}
	public void setSnmptype(int snmptype) {
		this.snmptype = snmptype;
	}
	
}
