/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.map.db;

import java.sql.Timestamp;

/**
 * <p>DbMap class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DbMap {

    private int id;

    private String name;

    private String background;

    private String owner;

    private String group;
    
    private String accessMode;

    private String userLastModifies;

    private Timestamp createTime;

    private Timestamp lastModifiedTime;

    private float scale;

    private int offsetX;

    private int offsetY;

    private String type;
    
    private int width;
    
    private int height;

    private boolean isNew = false;

    /**
     * <p>Constructor for DbMap.</p>
     */
    public DbMap() {
        this.isNew = true;
    }
    
    /**
     * <p>Constructor for DbMap.</p>
     *
     * @param id a int.
     * @param name a {@link java.lang.String} object.
     * @param owner a {@link java.lang.String} object.
     */
    public DbMap(int id, String name, String owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
    }
  
    
    /**
     * <p>Constructor for DbMap.</p>
     *
     * @param id a int.
     * @param name a {@link java.lang.String} object.
     * @param background a {@link java.lang.String} object.
     * @param owner a {@link java.lang.String} object.
     * @param accessMode a {@link java.lang.String} object.
     * @param userLastModifies a {@link java.lang.String} object.
     * @param scale a float.
     * @param offsetX a int.
     * @param offsetY a int.
     * @param type a {@link java.lang.String} object.
     * @param width a int.
     * @param height a int.
     */
    public DbMap(int id, String name, String background, String owner,
            String accessMode, String userLastModifies, float scale,
            int offsetX, int offsetY, String type, int width, int height) {
        this.id = id;
        this.name = name;
        this.background = background;
        this.owner = owner;
        this.accessMode = accessMode;
        this.userLastModifies = userLastModifies;
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.type = type;
        this.width=width;
        this.height=height;
    }

    /**
     * <p>Constructor for DbMap.</p>
     *
     * @param id a int.
     * @param name a {@link java.lang.String} object.
     * @param background a {@link java.lang.String} object.
     * @param owner a {@link java.lang.String} object.
     * @param group a {@link java.lang.String} object.
     * @param accessMode a {@link java.lang.String} object.
     * @param userLastModifies a {@link java.lang.String} object.
     * @param scale a float.
     * @param offsetX a int.
     * @param offsetY a int.
     * @param type a {@link java.lang.String} object.
     * @param width a int.
     * @param height a int.
     */
    public DbMap(int id, String name, String background, String owner, String group,
            String accessMode, String userLastModifies, float scale,
            int offsetX, int offsetY, String type, int width, int height) {
        this.id = id;
        this.name = name;
        this.background = background;
        this.owner = owner;
        this.group = group;
        this.accessMode = accessMode;
        this.userLastModifies = userLastModifies;
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.type = type;
        this.width=width;
        this.height=height;
    }


    /**
     * <p>Getter for the field <code>accessMode</code>.</p>
     *
     * @return Returns the accessMode.
     */
    public String getAccessMode() {
        return accessMode;
    }

    /**
     * <p>Setter for the field <code>accessMode</code>.</p>
     *
     * @param accessMode
     *            The accessMode to set.
     */
    public void setAccessMode(String accessMode) {
        this.accessMode = accessMode;
    }

    /**
     * <p>Getter for the field <code>background</code>.</p>
     *
     * @return Returns the background.
     */
    public String getBackground() {
        return background;
    }

    /**
     * <p>Setter for the field <code>background</code>.</p>
     *
     * @param background
     *            The background to set.
     */
    public void setBackground(String background) {
        this.background = background;
    }

    /**
     * <p>Getter for the field <code>createTime</code>.</p>
     *
     * @return Returns the createTime.
     */
    public Timestamp getCreateTime() {
        return createTime;
    }

    /**
     * <p>Setter for the field <code>createTime</code>.</p>
     *
     * @param createTime
     *            The createTime to set.
     */
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    /**
     * <p>Getter for the field <code>lastModifiedTime</code>.</p>
     *
     * @return Returns the lastModifiedTime.
     */
    public Timestamp getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * <p>Setter for the field <code>lastModifiedTime</code>.</p>
     *
     * @param lastModifiedTime
     *            The lastModifiedTime to set.
     */
    public void setLastModifiedTime(Timestamp lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * <p>Getter for the field <code>name</code>.</p>
     *
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * <p>Setter for the field <code>name</code>.</p>
     *
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * <p>Getter for the field <code>offsetX</code>.</p>
     *
     * @return Returns the offsetX.
     */
    public int getOffsetX() {
        return offsetX;
    }

    /**
     * <p>Setter for the field <code>offsetX</code>.</p>
     *
     * @param offsetX
     *            The offsetX to set.
     */
    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * <p>Getter for the field <code>offsetY</code>.</p>
     *
     * @return Returns the offsetY.
     */
    public int getOffsetY() {
        return offsetY;
    }

    /**
     * <p>Setter for the field <code>offsetY</code>.</p>
     *
     * @param offsetY
     *            The offsetY to set.
     */
    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * <p>Getter for the field <code>owner</code>.</p>
     *
     * @return Returns the owner.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * <p>Setter for the field <code>owner</code>.</p>
     *
     * @param owner
     *            The owner to set.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * <p>Getter for the field <code>scale</code>.</p>
     *
     * @return Returns the scale.
     */
    public float getScale() {
        return scale;
    }

    /**
     * <p>Setter for the field <code>scale</code>.</p>
     *
     * @param scale
     *            The scale to set.
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * <p>Getter for the field <code>userLastModifies</code>.</p>
     *
     * @return Returns the userLastModifies.
     */
    public String getUserLastModifies() {
        return userLastModifies;
    }

    /**
     * <p>Setter for the field <code>userLastModifies</code>.</p>
     *
     * @param userLastModifies
     *            The userLastModifies to set.
     */
    public void setUserLastModifies(String userLastModifies) {
        this.userLastModifies = userLastModifies;
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
     */
    public void setType(String type) {
        this.type = type;
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
            this.id=id;
    }

    /**
     * <p>isNew</p>
     *
     * @return a boolean.
     */
    public boolean isNew() {
        return this.isNew;
    }

    /**
     * <p>setAsNew</p>
     *
     * @param v a boolean.
     */
    public void setAsNew(boolean v) {
        this.isNew = v;
    }
	/**
	 * <p>Getter for the field <code>height</code>.</p>
	 *
	 * @return a int.
	 */
	public int getHeight() {
		return height;
	}
	/**
	 * <p>Setter for the field <code>height</code>.</p>
	 *
	 * @param height a int.
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	/**
	 * <p>Getter for the field <code>width</code>.</p>
	 *
	 * @return a int.
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * <p>Setter for the field <code>width</code>.</p>
	 *
	 * @param width a int.
	 */
	public void setWidth(int width) {
		this.width = width;
	}

    /**
     * <p>Getter for the field <code>group</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGroup() {
        return group;
    }

    /**
     * <p>Setter for the field <code>group</code>.</p>
     *
     * @param group a {@link java.lang.String} object.
     */
    public void setGroup(String group) {
        this.group = group;
    }
}
