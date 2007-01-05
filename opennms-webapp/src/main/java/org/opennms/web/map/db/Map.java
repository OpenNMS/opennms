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
 * Created on 9-dic-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.db;

import java.sql.Timestamp;

/**
 * @author
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Map {

    private int id;

    private String name;

    private String background;

    private String owner;

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

    public static final String USER_GENERATED_MAP = "U";

    public static final String AUTOMATICALLY_GENERATED_MAP = "A";

    public static final String DELETED_MAP = "D"; //for future use

    private boolean isNew = false;

    public Map() {
        this.isNew = true;
    }
    
    public Map(int id, String name, String owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
    }
  
    
    public Map(int id, String name, String background, String owner,
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
     * @return Returns the accessMode.
     */
    public String getAccessMode() {
        return accessMode;
    }

    /**
     * @param accessMode
     *            The accessMode to set.
     */
    public void setAccessMode(String accessMode) {
        this.accessMode = accessMode;
    }

    /**
     * @return Returns the background.
     */
    public String getBackground() {
        return background;
    }

    /**
     * @param background
     *            The background to set.
     */
    public void setBackground(String background) {
        this.background = background;
    }

    /**
     * @return Returns the createTime.
     */
    public Timestamp getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime
     *            The createTime to set.
     */
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    /**
     * @return Returns the lastModifiedTime.
     */
    public Timestamp getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * @param lastModifiedTime
     *            The lastModifiedTime to set.
     */
    public void setLastModifiedTime(Timestamp lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the offsetX.
     */
    public int getOffsetX() {
        return offsetX;
    }

    /**
     * @param offsetX
     *            The offsetX to set.
     */
    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * @return Returns the offsetY.
     */
    public int getOffsetY() {
        return offsetY;
    }

    /**
     * @param offsetY
     *            The offsetY to set.
     */
    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * @return Returns the owner.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner
     *            The owner to set.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @return Returns the scale.
     */
    public float getScale() {
        return scale;
    }

    /**
     * @param scale
     *            The scale to set.
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * @return Returns the userLastModifies.
     */
    public String getUserLastModifies() {
        return userLastModifies;
    }

    /**
     * @param userLastModifies
     *            The userLastModifies to set.
     */
    public void setUserLastModifies(String userLastModifies) {
        this.userLastModifies = userLastModifies;
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
    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isNew() {
        return this.isNew;
    }

    void setAsNew(boolean v) {
        this.isNew = v;
    }
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
}
