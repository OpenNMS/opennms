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
// 2007 Apr 10: Organized imports. - dj@opennms.org
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GeneratedValue;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlID;

@XmlRootElement(name = "map")
@Entity
@Table(name = "map")
public class OnmsMap implements Serializable {

    public static final String USER_GENERATED_MAP = "U";

    public static final String AUTOMATICALLY_GENERATED_MAP = "A";

    public static final String DELETED_MAP = "D"; //for future use

    public static final String ACCESS_MODE_ADMIN = "RW";
    public static final String ACCESS_MODE_USER = "RO";
    public static final String ACCESS_MODE_GROUP = "RWRO";
    
    @XmlTransient
    @Id
    @Column(name="mapId")
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    private int id;

    @Column(name = "mapName")
    private String name;

    @Column(name = "mapGroup")
    private String mapGroup;

    @Column(name = "mapBackGround")
    private String background;

    @Column(name = "mapOwner")
    private String owner;

    @Column(name = "mapAccess")
    private String accessMode;

    @Column(name = "userLastModifies")
    private String userLastModifies;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "mapCreateTime")
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastModifiedTime")
    private Date lastModifiedTime;

    @Column(name = "mapScale")
    private float scale;

    @Column(name = "mapXOffset")
    private int offsetX;

    @Column(name = "mapYOffset")
    private int offsetY;

    @Column(name = "mapType")
    private String type;

    @Column(name = "mapWidth")
    private int width;

    @Column(name = "mapHeight")
    private int height;

    @XmlTransient
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "mapId")
    private List<OnmsMapElement> mapElements = new ArrayList<OnmsMapElement>();

    @XmlTransient
    @Transient
    private boolean isNew = false;

    private static final long serialVersionUID = 3885485728813867167L;

    public OnmsMap() {
        this.isNew = true;
        this.createTime = new Date();
        this.lastModifiedTime = new Date();
    }

    public OnmsMap(String name, String owner) {
        this.name = name;
        this.owner = owner;
        this.userLastModifies = owner;
        this.createTime = new Date();
        this.lastModifiedTime = new Date();
        this.accessMode = ACCESS_MODE_USER;
        this.width = 800;
        this.height = 600;
    }

    public OnmsMap(String name, String owner, int width, int height) {
        this.name = name;
        this.owner = owner;
        this.userLastModifies = owner;
        this.createTime = new Date();
        this.lastModifiedTime = new Date();
        this.accessMode = ACCESS_MODE_USER;
        this.width = width;
        this.height = height;
    }

    public OnmsMap(String name, String owner, String accessMode, int width, int height) {
        this.name = name;
        this.owner = owner;
        this.userLastModifies = owner;
        this.createTime = new Date();
        this.lastModifiedTime = new Date();
        setAccessMode(accessMode);
        this.width = width;
        this.height = height;
    }

    public OnmsMap(String name, String mapGroup, String background,
            String owner, String accessMode, String type, int width,
            int height) {
        this.name = name;
        this.mapGroup = mapGroup;
        this.background = background;
        this.owner = owner;
        this.userLastModifies = owner;
        this.createTime = new Date();
        this.lastModifiedTime = new Date();
        setAccessMode(accessMode);
        this.type = type;
        this.width = width;
        this.height = height;
    }

    public int getId() {
        return id;
    }

    @XmlID
    @Transient
    public String getMapId() {
        return Integer.toString(id);
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMapGroup() {
        return mapGroup;
    }

    public void setMapGroup(String mapGroup) {
        this.mapGroup = mapGroup;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(String accessMode) {
        if(accessMode.equals(ACCESS_MODE_GROUP) || accessMode.equals(ACCESS_MODE_ADMIN))
            this.accessMode = accessMode;
        else
            this.accessMode = ACCESS_MODE_USER;
    }

    public String getUserLastModifies() {
        return userLastModifies;
    }

    public void setUserLastModifies(String userLastModifies) {
        this.userLastModifies = userLastModifies;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }


    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public List<OnmsMapElement> getMapElements() {
        return this.mapElements;
    }

    public void setMapElements(List<OnmsMapElement> mapElements) {
        this.mapElements = mapElements;
    }
}
