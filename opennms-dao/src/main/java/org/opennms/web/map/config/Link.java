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
 * <p>Link class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.8.1
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
	
	
	
	/**
	 * <p>Constructor for Link.</p>
	 *
	 * @param id a int.
	 * @param speed a {@link java.lang.String} object.
	 * @param text a {@link java.lang.String} object.
	 * @param width a {@link java.lang.String} object.
	 * @param dasharray a int.
	 * @param snmptype a int.
	 * @param multilinkwidth a {@link java.lang.String} object.
	 * @param multilinkdasharray a int.
	 */
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
	
	
	
	/**
	 * <p>Getter for the field <code>multilinkwidth</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getMultilinkwidth() {
        return multilinkwidth;
    }



    /**
     * <p>Setter for the field <code>multilinkwidth</code>.</p>
     *
     * @param multilinkwidth a {@link java.lang.String} object.
     */
    public void setMultilinkwidth(String multilinkwidth) {
        this.multilinkwidth = multilinkwidth;
    }



    /**
     * <p>Getter for the field <code>multilinkdasharray</code>.</p>
     *
     * @return a int.
     */
    public int getMultilinkdasharray() {
        return multilinkdasharray;
    }



    /**
     * <p>Setter for the field <code>multilinkdasharray</code>.</p>
     *
     * @param multilinkdasharray a int.
     */
    public void setMultilinkdasharray(int multilinkdasharray) {
        this.multilinkdasharray = multilinkdasharray;
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
	 * <p>Getter for the field <code>dasharray</code>.</p>
	 *
	 * @return a int.
	 */
	public int getDasharray() {
		return dasharray;
	}
	/**
	 * <p>Setter for the field <code>dasharray</code>.</p>
	 *
	 * @param dasharray a int.
	 */
	public void setDasharray(int dasharray) {
		this.dasharray = dasharray;
	}
	/**
	 * <p>Getter for the field <code>speed</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSpeed() {
		return speed;
	}
	/**
	 * <p>Setter for the field <code>speed</code>.</p>
	 *
	 * @param speed a {@link java.lang.String} object.
	 */
	public void setSpeed(String speed) {
		this.speed = speed;
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
	 * <p>Getter for the field <code>width</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getWidth() {
		return width;
	}
	/**
	 * <p>Setter for the field <code>width</code>.</p>
	 *
	 * @param width a {@link java.lang.String} object.
	 */
	public void setWidth(String width) {
		this.width = width;
	}
	/**
	 * <p>Getter for the field <code>snmptype</code>.</p>
	 *
	 * @return a int.
	 */
	public int getSnmptype() {
		return snmptype;
	}
	/**
	 * <p>Setter for the field <code>snmptype</code>.</p>
	 *
	 * @param snmptype a int.
	 */
	public void setSnmptype(int snmptype) {
		this.snmptype = snmptype;
	}
	
}
