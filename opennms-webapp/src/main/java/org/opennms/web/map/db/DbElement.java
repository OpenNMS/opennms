/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.map.db;

import java.lang.reflect.UndeclaredThrowableException;

import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.MapsException;

/**
 * <p>DbElement class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 * @since 1.8.1
 */
public class DbElement implements Cloneable {
    private int mapId;

    private int id;

    protected String type;

    private String label;

    private String icon;

    private String sysoid;
    
    /**
     * <p>Getter for the field <code>sysoid</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSysoid() {
        return sysoid;
    }

    /**
     * <p>Setter for the field <code>sysoid</code>.</p>
     *
     * @param sysoid a {@link java.lang.String} object.
     */
    public void setSysoid(String sysoid) {
        this.sysoid = sysoid;
    }

    private int x;

    private int y;
    
    /**
     * <p>Constructor for DbElement.</p>
     */
    protected DbElement() {
        // blank
    }

    /**
     * <p>Constructor for DbElement.</p>
     *
     * @param e a {@link org.opennms.web.map.db.DbElement} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public DbElement(DbElement e) throws MapsException {
        this(e.mapId, e.id, e.type, e.label, e.sysoid, e.icon, e.x, e.y);
    }

    /**
     * <p>Constructor for DbElement.</p>
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
    public DbElement(int mapId, int id, String type, String label,
            String sysoid, String iconName, int x, int y)throws MapsException {
        this.mapId = mapId;
        this.id = id;
        this.setType(type);
        this.label = label;
        this.sysoid = sysoid;
        setIcon(iconName);
        this.x = x;
        this.y = y;
    }

    /**
     * <p>Getter for the field <code>icon</code>.</p>
     *
     * @return Returns the iconName.
     */
    public String getIcon() {
        return icon;
    }

    /**
     * <p>Setter for the field <code>icon</code>.</p>
     *
     * @param iconName
     *            The iconName to set.
     */
    public void setIcon(String iconName) {
        this.icon = iconName;
    }

    /**
     * <p>Getter for the field <code>label</code>.</p>
     *
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * <p>Setter for the field <code>label</code>.</p>
     *
     * @param label
     *            The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * <p>Getter for the field <code>x</code>.</p>
     *
     * @return Returns the x.
     */
    public int getX() {
        return x;
    }

    /**
     * <p>Setter for the field <code>x</code>.</p>
     *
     * @param x
     *            The x to set.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * <p>Getter for the field <code>y</code>.</p>
     *
     * @return Returns the y.
     */
    public int getY() {
        return y;
    }

    /**
     * <p>Setter for the field <code>y</code>.</p>
     *
     * @param y
     *            The y to set.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * <p>Getter for the field <code>type</code>.</p>
     *
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * <p>Setter for the field <code>type</code>.</p>
     *
     * @param type
     *            The type to set.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public void setType(String type) throws MapsException {
        if (type.equals(MapsConstants.MAP_TYPE) || type.equals(MapsConstants.NODE_TYPE) || type.equals(MapsConstants.NODE_HIDE_TYPE) || type.equals(MapsConstants.MAP_HIDE_TYPE))  this.type = type;
        new MapsException("Cannot create an Element with type " + type);
    }

    /**
     * <p>Getter for the field <code>mapId</code>.</p>
     *
     * @return a int.
     */
    public int getMapId() {
        return mapId;
    }

    /**
     * <p>Setter for the field <code>mapId</code>.</p>
     *
     * @param mapId a int.
     */
    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return Returns the id.
     */
    public int getId() {
        return id;
    }

    /**
     * <p>Setter for the field <code>id</code>.</p>
     *
     * @param id
     *            The id to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * <p>clone</p>
     *
     * @return a {@link org.opennms.web.map.db.DbElement} object.
     */
    @Override
    public DbElement clone() {
        try {
            return (DbElement) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new UndeclaredThrowableException(e, "CloneNotSupportedException thrown while calling super.clone(), which is odd since we implement the Cloneable interface");
        }
    }
    
    /**
     * <p>isMap</p>
     *
     * @return a boolean.
     */
    public boolean isMap() {
    	if (type.equals(MapsConstants.MAP_TYPE)) return true;
    	return false;
    }

    /**
     * <p>isNode</p>
     *
     * @return a boolean.
     */
    public boolean isNode() {
    	if (type.equals(MapsConstants.NODE_TYPE)) return true;
    	return false;
    }

    /**
     * <p>isHideMap</p>
     *
     * @return a boolean.
     */
    public boolean isHideMap() {
        if (type.equals(MapsConstants.MAP_HIDE_TYPE)) return true;
        return false;
    }

    /**
     * <p>isHideNode</p>
     *
     * @return a boolean.
     */
    public boolean isHideNode() {
        if (type.equals(MapsConstants.NODE_HIDE_TYPE)) return true;
        return false;
    }

}
