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
 * Created: January 17, 2007
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
package org.opennms.web.map.db;

import java.lang.reflect.UndeclaredThrowableException;

import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.MapsException;

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DbElement implements Cloneable {
    private int mapId;

    private int id;

    protected String type;

    private String label;

    private String icon;

    private String sysoid;
    
    public String getSysoid() {
        return sysoid;
    }

    public void setSysoid(String sysoid) {
        this.sysoid = sysoid;
    }

    private int x;

    private int y;
    
    protected DbElement() {
        // blank
    }

    public DbElement(DbElement e) throws MapsException {
        this(e.mapId, e.id, e.type, e.label, e.icon, e.x, e.y);
    }

    public DbElement(int mapId, int id, String type, String label,
            String iconName, int x, int y)throws MapsException {
        this.mapId = mapId;
        this.id = id;
        this.setType(type);
        this.label = label;
        setIcon(iconName);
        this.x = x;
        this.y = y;
    }

    /**
     * @return Returns the iconName.
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param iconName
     *            The iconName to set.
     */
    public void setIcon(String iconName) {
        this.icon = iconName;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return Returns the x.
     */
    public int getX() {
        return x;
    }

    /**
     * @param x
     *            The x to set.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return Returns the y.
     */
    public int getY() {
        return y;
    }

    /**
     * @param y
     *            The y to set.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setType(String type) throws MapsException {
        if (type.equals(MapsConstants.MAP_TYPE) || type.equals(MapsConstants.NODE_TYPE) || type.equals(MapsConstants.NODE_HIDE_TYPE) || type.equals(MapsConstants.MAP_HIDE_TYPE))  this.type = type;
        new MapsException("Cannot create an Element with type " + type);
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    /**
     * @return Returns the id.
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    public DbElement clone() {
        try {
            return (DbElement) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new UndeclaredThrowableException(e, "CloneNotSupportedException thrown while calling super.clone(), which is odd since we implement the Cloneable interface");
        }
    }
    
    public boolean isMap() {
    	if (type.equals(MapsConstants.MAP_TYPE)) return true;
    	return false;
    }

    public boolean isNode() {
    	if (type.equals(MapsConstants.NODE_TYPE)) return true;
    	return false;
    }

    public boolean isHideMap() {
        if (type.equals(MapsConstants.MAP_HIDE_TYPE)) return true;
        return false;
    }

    public boolean isHideNode() {
        if (type.equals(MapsConstants.NODE_HIDE_TYPE)) return true;
        return false;
    }

}
