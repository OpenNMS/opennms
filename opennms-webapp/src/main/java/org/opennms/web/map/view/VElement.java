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
// Modifications:
//
// 2007 Jul 24: Add our own clone() that returns the right object type. - dj@opennms.org
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
 * Created on 10-gen-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.view;

import org.opennms.web.map.MapsException;
import org.opennms.web.map.db.DbElement;

/**
 * @author micmas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class VElement extends DbElement {

	// boolean that represents if this is a childnode mandatory to avoid loops  
	protected boolean isChild = false;
    
    // this is to define the status of the element
	protected int status = 0;

	// this is used to understand if some fact happen is important
	protected int severity = 0;

	// this represents the global information elements
	protected double avail = 0;
	
	

    /**
     * 
     */
	public VElement() {
        super();
    }

    /**
     * @param e
     */
    public VElement(DbElement e) throws MapsException {
        super(e);
    }

    /**
     * @param mapId
     * @param id
     * @param type
     * @param label
     * @param iconName
     * @param x
     * @param y
     */
    public VElement(int mapId, int id, String type, String iconName,String label,
            int x, int y) throws MapsException {
        super(mapId, id, type, label, iconName, x, y);
        isChild = true;
    }    

    public VElement(int mapId, int id, String type, String label, String iconName) throws MapsException {
        super(mapId, id, type, label, iconName, 0, 0);
        isChild = true;
    }    

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		//if (status > LOWER_STATUS && status <= UPPER_STATUS) 
			this.status = status;
	}
	
	public int getSeverity() {
		return severity;
	}
	
	public void setSeverity(int severity) {
		//if (severity > LOWER_SEVERITY && severity <= UPPER_SEVERITY) 
			this.severity = severity;
	}

	public double getAvail() {
		return avail;
	}
	
	public void setAvail(double rtc) {
		this.avail = rtc;
	}
	
    public boolean isChild() {
        return isChild;
    }
    
    public boolean equals(Object other){
    	return equalsIgnorePosition((VElement)other);
    }
    /**
     * @param elem
     * @return
     */

    public boolean equalsIgnorePosition(VElement elem) {
    	if (this.getMapId() == elem.getMapId() &&
    		this.getId() == elem.getId() && this.type.equals( elem.getType()) &&
    		this.avail == elem.getAvail() && this.status == elem.getStatus() &&
			this.severity == elem.getSeverity() && this.getLabel().equals( elem.getLabel() )) return true;
    	return false;
    }

    public boolean equalsIgnorePositionParentMap(VElement elem) {
    	if (this.getId() == elem.getId() && this.type.equals( elem.getType() ) &&
    		this.avail == elem.getAvail() && this.status == elem.getStatus() &&
			this.severity == elem.getSeverity() && this.getLabel().equals( elem.getLabel())) return true;
    	return false;
    }
    
    public boolean hasSameIdentifier(VElement elem) {
    	if (this.getId() == elem.getId() && this.type.equals( elem.getType() )) return true;
        	return false;
    }

    public void setMapId(int mapId) {
        super.setMapId(mapId);
        isChild = true;
    }
    
    final public int getContainerMap()throws VElementNotChildException{
    	if(isChild==true)
    		return getMapId();
    	throw new VElementNotChildException();
    }
    
    public String toString(){
    	return getId()+getType();
    }
    
    public VElement clone() {
        return (VElement) super.clone();
    }
}
