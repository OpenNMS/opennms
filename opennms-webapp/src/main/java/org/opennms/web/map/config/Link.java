/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
	String multilinkwidth;
	int multilinkdasharray;
	
	
	
	public Link(int id,String speed, String text, String width, int dasharray,int snmptype, String multilinkwidth, int multilinkdasharray) {
		super();
		this.id=id;
		this.speed = speed;
		this.text = text;
		this.width = width;
		this.dasharray = dasharray;
		this.snmptype = snmptype;
		this.multilinkwidth = multilinkwidth;
		this.multilinkdasharray = multilinkdasharray;
	}
	
	
	
	public String getMultilinkwidth() {
        return multilinkwidth;
    }



    public void setMultilinkwidth(String multilinkwidth) {
        this.multilinkwidth = multilinkwidth;
    }



    public int getMultilinkdasharray() {
        return multilinkdasharray;
    }



    public void setMultilinkdasharray(int multilinkdasharray) {
        this.multilinkdasharray = multilinkdasharray;
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
