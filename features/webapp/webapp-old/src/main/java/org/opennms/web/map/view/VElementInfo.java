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
 * Created: Juy 6, 2007
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
package org.opennms.web.map.view;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class VElementInfo implements Cloneable {
    
	private int id;

    private String uei;
    
    private int severity;
    
    private String label;
    
    
    
	public String getLabel() {
		return label;
	}
	public int getId() {
		return id;
	}
	public int getSeverity() {
		return severity;
	}
	public String getUei() {
		return uei;
	}

    
    
	/**
	 * @param id
	 * @param uei
	 * @param severity
	 * @param label
	 */
	public VElementInfo(int id, String uei, int severity, String label) {
		super();
		this.id = id;
		this.uei = uei;
		this.severity = severity;
		this.label=label;
	}
	
	/**
	 * @param id
	 * @param uei
	 * @param severity
	 */
	public VElementInfo(int id, String uei, int severity) {
		super();
		this.id = id;
		this.uei = uei;
		this.severity = severity;
	}
}
