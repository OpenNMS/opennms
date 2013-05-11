/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.map.view;

import org.opennms.web.map.MapsException;
import org.opennms.web.map.db.DbElement;

/**
 * <p>VElement class.</p>
 *
 * @author micmas
 *
 * @since 1.8.1
 */
public class VElement extends DbElement {

	// boolean that represents if this is a childnode mandatory to avoid loops  
	protected boolean isChild = false;
    
    // this is to define the status of the element
	protected int status = -1;

	// this is used to understand if some fact happen is important
	protected int severity = -1;

	// this represents the global information elements
	protected double avail = -1;
	
	

	/**
	 * <p>Constructor for VElement.</p>
	 */
	public VElement() {
        super();
    }

    /**
     * <p>Constructor for VElement.</p>
     *
     * @param e a {@link org.opennms.web.map.db.DbElement} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public VElement(DbElement e) throws MapsException {
        super(e);
    }

    /**
     * <p>Constructor for VElement.</p>
     *
     * @param mapId a int.
     * @param id a int.
     * @param type a {@link java.lang.String} object.
     * @param label a {@link java.lang.String} object.
     * @param sysoid a {@link java.lang.String} object.
     * @param iconName a {@link java.lang.String} object.
     * @param x a int.
     * @param y a int.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public VElement(int mapId, int id, String type, String sysoid, String iconName,String label,
            int x, int y) throws MapsException {
        super(mapId, id, type, label, sysoid, iconName, x, y);
        isChild = true;
    }    

    /**
     * <p>Constructor for VElement.</p>
     *
     * @param mapId a int.
     * @param id a int.
     * @param type a {@link java.lang.String} object.
     * @param label a {@link java.lang.String} object.
     * @param sysoid a {@link java.lang.String} object.
     * @param iconName a {@link java.lang.String} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public VElement(int mapId, int id, String type, String label, String sysoid, String iconName) throws MapsException {
        super(mapId, id, type, label, sysoid, iconName, 0, 0);
        isChild = true;
    }    

	/**
	 * <p>Getter for the field <code>status</code>.</p>
	 *
	 * @return a int.
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * <p>Setter for the field <code>status</code>.</p>
	 *
	 * @param status a int.
	 */
	public void setStatus(int status) {
		//if (status > LOWER_STATUS && status <= UPPER_STATUS) 
			this.status = status;
	}
	
	/**
	 * <p>Getter for the field <code>severity</code>.</p>
	 *
	 * @return a int.
	 */
	public int getSeverity() {
		return severity;
	}
	
	/**
	 * <p>Setter for the field <code>severity</code>.</p>
	 *
	 * @param severity a int.
	 */
	public void setSeverity(int severity) {
		//if (severity > LOWER_SEVERITY && severity <= UPPER_SEVERITY) 
			this.severity = severity;
	}

	/**
	 * <p>Getter for the field <code>avail</code>.</p>
	 *
	 * @return a double.
	 */
	public double getAvail() {
		return avail;
	}
	
	/**
	 * <p>Setter for the field <code>avail</code>.</p>
	 *
	 * @param rtc a double.
	 */
	public void setAvail(double rtc) {
		this.avail = rtc;
	}
	
    /**
     * <p>isChild</p>
     *
     * @return a boolean.
     */
    public boolean isChild() {
        return isChild;
    }
    
    /** {@inheritDoc} */
        @Override
    public boolean equals(Object other){
    	return equalsIgnorePosition((VElement)other);
    }
    
    /**
     * <p>equalsIgnorePosition</p>
     *
     * @param elem a {@link org.opennms.web.map.view.VElement} object.
     * @return a boolean.
     */
    public boolean equalsIgnorePosition(VElement elem) {
    	if (this.getMapId() == elem.getMapId() &&
    		this.getId() == elem.getId() && this.type.equals( elem.getType()) &&
    		this.avail == elem.getAvail() && this.status == elem.getStatus() &&
			this.severity == elem.getSeverity() && this.getLabel().equals( elem.getLabel() )) return true;
    	return false;
    }

    /**
     * <p>equalsIgnorePositionParentMap</p>
     *
     * @param elem a {@link org.opennms.web.map.view.VElement} object.
     * @return a boolean.
     */
    public boolean equalsIgnorePositionParentMap(VElement elem) {
    	if (this.getId() == elem.getId() && this.type.equals( elem.getType() ) &&
    		this.avail == elem.getAvail() && this.status == elem.getStatus() &&
			this.severity == elem.getSeverity() && this.getLabel().equals( elem.getLabel())) return true;
    	return false;
    }
    
    /**
     * <p>hasSameIdentifier</p>
     *
     * @param elem a {@link org.opennms.web.map.view.VElement} object.
     * @return a boolean.
     */
    public boolean hasSameIdentifier(VElement elem) {
    	if (this.getId() == elem.getId() && this.type.equals( elem.getType() )) return true;
        	return false;
    }

    /** {@inheritDoc} */
        @Override
    public void setMapId(int mapId) {
        super.setMapId(mapId);
        isChild = true;
    }
    
    /**
     * <p>getContainerMap</p>
     *
     * @return a int.
     * @throws org.opennms.web.map.view.VElementNotChildException if any.
     */
    final public int getContainerMap()throws VElementNotChildException{
    	if(isChild==true)
    		return getMapId();
    	throw new VElementNotChildException();
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String toString(){
    	return getId()+getType();
    }
    
    /**
     * <p>clone</p>
     *
     * @return a {@link org.opennms.web.map.view.VElement} object.
     */
        @Override
    public VElement clone() {
        return (VElement) super.clone();
    }
}
