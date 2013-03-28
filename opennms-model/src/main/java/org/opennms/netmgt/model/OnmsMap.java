/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * <p>OnmsMap class.</p>
 */
@XmlRootElement(name = "map")
@Entity
@Table(name = "map")
public class OnmsMap implements Serializable {

    private static final long serialVersionUID = 3885485728813867167L;

    /** Constant <code>USER_GENERATED_MAP="U"</code> */
    public static final String USER_GENERATED_MAP = "U";

    /** Constant <code>AUTOMATICALLY_GENERATED_MAP="A"</code> */
    public static final String AUTOMATICALLY_GENERATED_MAP = "A";

    /** Constant <code>AUTOMATIC_SAVED_MAP="S"</code> */
    public static final String AUTOMATIC_SAVED_MAP = "S";
    /** Constant <code>DELETED_MAP="D"</code> */
    public static final String DELETED_MAP = "D"; //for future use

    /** Constant <code>ACCESS_MODE_ADMIN="RW"</code> */
    public static final String ACCESS_MODE_ADMIN = "RW";
    /** Constant <code>ACCESS_MODE_USER="RO"</code> */
    public static final String ACCESS_MODE_USER = "RO";
    /** Constant <code>ACCESS_MODE_GROUP="RWRO"</code> */
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


    /**
     * <p>Constructor for OnmsMap.</p>
     */
    public OnmsMap() {
        this.createTime = new Date();
        this.lastModifiedTime = new Date();
    }

    /**
     * <p>Constructor for OnmsMap.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param owner a {@link java.lang.String} object.
     */
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

    /**
     * <p>Constructor for OnmsMap.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param owner a {@link java.lang.String} object.
     * @param width a int.
     * @param height a int.
     */
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

    /**
     * <p>Constructor for OnmsMap.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param owner a {@link java.lang.String} object.
     * @param accessMode a {@link java.lang.String} object.
     * @param width a int.
     * @param height a int.
     */
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

    /**
     * <p>Constructor for OnmsMap.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param mapGroup a {@link java.lang.String} object.
     * @param background a {@link java.lang.String} object.
     * @param owner a {@link java.lang.String} object.
     * @param accessMode a {@link java.lang.String} object.
     * @param type a {@link java.lang.String} object.
     * @param width a int.
     * @param height a int.
     */
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

    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return a int.
     */
    @XmlTransient
    @Id
    @Column(name="mapId", nullable=false)
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public int getId() {
        return id;
    }

    /**
     * <p>getMapId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlID
    @Transient
    public String getMapId() {
        return Integer.toString(getId());
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
     * <p>Getter for the field <code>name</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "mapName")
    public String getName() {
        return name;
    }

    /**
     * <p>Setter for the field <code>name</code>.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * <p>Getter for the field <code>mapGroup</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "mapGroup")
    public String getMapGroup() {
        return mapGroup;
    }

    /**
     * <p>Setter for the field <code>mapGroup</code>.</p>
     *
     * @param mapGroup a {@link java.lang.String} object.
     */
    public void setMapGroup(String mapGroup) {
        this.mapGroup = mapGroup;
    }

    /**
     * <p>Getter for the field <code>background</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "mapBackGround")
    public String getBackground() {
        return background;
    }

    /**
     * <p>Setter for the field <code>background</code>.</p>
     *
     * @param background a {@link java.lang.String} object.
     */
    public void setBackground(String background) {
        this.background = background;
    }

    /**
     * <p>Getter for the field <code>owner</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "mapOwner")
    public String getOwner() {
        return owner;
    }

    /**
     * <p>Setter for the field <code>owner</code>.</p>
     *
     * @param owner a {@link java.lang.String} object.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * <p>Getter for the field <code>accessMode</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "mapAccess")
    public String getAccessMode() {
        return accessMode;
    }

    /**
     * <p>Setter for the field <code>accessMode</code>.</p>
     *
     * @param accessMode a {@link java.lang.String} object.
     */
    public void setAccessMode(String accessMode) {
        if(accessMode.trim().equalsIgnoreCase(ACCESS_MODE_GROUP))
            this.accessMode = ACCESS_MODE_GROUP;
        else
            this.accessMode = ACCESS_MODE_ADMIN;
    }

    /**
     * <p>Getter for the field <code>userLastModifies</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "userLastModifies")
    public String getUserLastModifies() {
        return userLastModifies;
    }

    /**
     * <p>Setter for the field <code>userLastModifies</code>.</p>
     *
     * @param userLastModifies a {@link java.lang.String} object.
     */
    public void setUserLastModifies(String userLastModifies) {
        this.userLastModifies = userLastModifies;
    }

    /**
     * <p>Getter for the field <code>createTime</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "mapCreateTime")
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * <p>Setter for the field <code>createTime</code>.</p>
     *
     * @param createTime a {@link java.util.Date} object.
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * <p>Getter for the field <code>lastModifiedTime</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastModifiedTime")
    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * <p>Setter for the field <code>lastModifiedTime</code>.</p>
     *
     * @param lastModifiedTime a {@link java.util.Date} object.
     */
    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * <p>Getter for the field <code>scale</code>.</p>
     *
     * @return a float.
     */
    @Column(name = "mapScale")
    public float getScale() {
        return scale;
    }

    /**
     * <p>Setter for the field <code>scale</code>.</p>
     *
     * @param scale a float.
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * <p>Getter for the field <code>offsetX</code>.</p>
     *
     * @return a int.
     */
    @Column(name = "mapXOffset")
    public int getOffsetX() {
        return offsetX;
    }

    /**
     * <p>Setter for the field <code>offsetX</code>.</p>
     *
     * @param offsetX a int.
     */
    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * <p>Getter for the field <code>offsetY</code>.</p>
     *
     * @return a int.
     */
    @Column(name = "mapYOffset")
    public int getOffsetY() {
        return offsetY;
    }

    /**
     * <p>Setter for the field <code>offsetY</code>.</p>
     *
     * @param offsetY a int.
     */
    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * <p>Getter for the field <code>type</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "mapType")
    public String getType() {
        return type;
    }

    /**
     * <p>Setter for the field <code>type</code>.</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public void setType(String type) {
        if (type.equalsIgnoreCase(OnmsMap.AUTOMATICALLY_GENERATED_MAP))
            this.type = OnmsMap.AUTOMATICALLY_GENERATED_MAP;
        else if (type.equalsIgnoreCase(OnmsMap.AUTOMATIC_SAVED_MAP))
            this.type = OnmsMap.AUTOMATIC_SAVED_MAP;
        else
            this.type = OnmsMap.USER_GENERATED_MAP;
    }

    /**
     * <p>Getter for the field <code>width</code>.</p>
     *
     * @return a int.
     */
    @Column(name = "mapWidth")
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
     * <p>Getter for the field <code>height</code>.</p>
     *
     * @return a int.
     */
    @Column(name = "mapHeight")
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
     * <p>Getter for the field <code>mapElements</code>.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @XmlTransient
    @OneToMany(mappedBy="map")
    @org.hibernate.annotations.Cascade( {
        org.hibernate.annotations.CascadeType.ALL,
        org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<OnmsMapElement> getMapElements() {
        return this.mapElements;
    }

    /**
     * <p>Setter for the field <code>mapElements</code>.</p>
     *
     * @param mapElements a {@link java.util.Set} object.
     */
    public void setMapElements(Set<OnmsMapElement> mapElements) {
        this.mapElements = mapElements;
    }
    
    /**
     * <p>addMapElement</p>
     *
     * @param element a {@link org.opennms.netmgt.model.OnmsMapElement} object.
     */
    public void addMapElement(OnmsMapElement element) {
        element.setMap(this);
        getMapElements().add(element);
    }

    
}
