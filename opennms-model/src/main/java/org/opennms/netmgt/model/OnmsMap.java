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
import java.util.LinkedHashSet;
import java.util.Set;

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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlID;

@XmlRootElement(name = "map")
@Entity
@Table(name = "map")
public class OnmsMap implements Serializable {

    private static final long serialVersionUID = 3885485728813867167L;

    public static final String USER_GENERATED_MAP = "U";

    public static final String AUTOMATICALLY_GENERATED_MAP = "A";

    public static final String AUTOMATIC_SAVED_MAP = "S";
    public static final String DELETED_MAP = "D"; //for future use

    public static final String ACCESS_MODE_ADMIN = "RW";
    public static final String ACCESS_MODE_USER = "RO";
    public static final String ACCESS_MODE_GROUP = "RWRO";
    
    private int id;

    private String name;

    private String mapGroup;

    private String background;

    private String owner;

    private String accessMode;

    private String userLastModifies;

    private Date createTime;

    private Date lastModifiedTime;

    private float scale;

    private int offsetX;

    private int offsetY;

    private String type;

    private int width;

    private int height;

    private Set<OnmsMapElement> mapElements = new LinkedHashSet<OnmsMapElement>();

    private boolean isNew = false;

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
        this.type=OnmsMap.USER_GENERATED_MAP;
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
        this.type=OnmsMap.USER_GENERATED_MAP;
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
        this.type=OnmsMap.USER_GENERATED_MAP;
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
        setType(type);
        this.width = width;
        this.height = height;
    }

    @XmlTransient
    @Id
    @Column(name="mapId")
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public int getId() {
        return id;
    }

    @XmlID
    @Transient
    public String getMapId() {
        return Integer.toString(getId());
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "mapName")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "mapGroup")
    public String getMapGroup() {
        return mapGroup;
    }

    public void setMapGroup(String mapGroup) {
        this.mapGroup = mapGroup;
    }

    @Column(name = "mapBackGround")
    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    @Column(name = "mapOwner")
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Column(name = "mapAccess")
    public String getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(String accessMode) {
        if(accessMode.trim().equalsIgnoreCase(ACCESS_MODE_GROUP))
            this.accessMode = ACCESS_MODE_GROUP;
        else
            this.accessMode = ACCESS_MODE_ADMIN;
    }

    @Column(name = "userLastModifies")
    public String getUserLastModifies() {
        return userLastModifies;
    }

    public void setUserLastModifies(String userLastModifies) {
        this.userLastModifies = userLastModifies;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "mapCreateTime")
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastModifiedTime")
    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    @Column(name = "mapScale")
    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    @Column(name = "mapXOffset")
    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    @Column(name = "mapYOffset")
    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    @Column(name = "mapType")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type.equalsIgnoreCase(OnmsMap.AUTOMATICALLY_GENERATED_MAP))
            this.type = OnmsMap.AUTOMATICALLY_GENERATED_MAP;
        else if (type.equalsIgnoreCase(OnmsMap.AUTOMATIC_SAVED_MAP))
            this.type = OnmsMap.AUTOMATIC_SAVED_MAP;
        else
            this.type = OnmsMap.USER_GENERATED_MAP;
    }

    @Column(name = "mapWidth")
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }


    @Column(name = "mapHeight")
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @XmlTransient
    @Transient
    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    @XmlTransient
    @OneToMany(mappedBy="map")
    @org.hibernate.annotations.Cascade( {
        org.hibernate.annotations.CascadeType.ALL,
        org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<OnmsMapElement> getMapElements() {
        return this.mapElements;
    }

    public void setMapElements(Set<OnmsMapElement> mapElements) {
        this.mapElements = mapElements;
    }
    
    public void addMapElement(OnmsMapElement element) {
        element.setMap(this);
        getMapElements().add(element);
    }

    
}
