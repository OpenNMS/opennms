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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * <p>OnmsMapElement class.</p>
 */
@XmlRootElement(name = "mapElement")
@Entity
@Table(name = "element")
public class OnmsMapElement implements Serializable {
    private static final long serialVersionUID = 1594163211618494443L;
    
    /** Constant <code>MAP_TYPE="M"</code> */
    public static final String MAP_TYPE = "M";
    /** Constant <code>MAP_HIDE_TYPE="W"</code> */
    public static final String MAP_HIDE_TYPE = "W";
    /** Constant <code>NODE_TYPE="N"</code> */
    public static final String NODE_TYPE = "N";
    /** Constant <code>NODE_HIDE_TYPE="H"</code> */
    public static final String NODE_HIDE_TYPE = "H";
    /** Constant <code>defaultNodeIcon="unspecified"</code> */
    public static final String defaultNodeIcon = "unspecified";
    /** Constant <code>defaultMapIcon="map"</code> */
    public static final String defaultMapIcon = "map";

    private int id;

    private int elementId;

    @XmlTransient
    @ManyToOne
    @JoinColumn(name = "mapId")
    private OnmsMap map;

    protected String type;

    private String label;

    private String iconName;

    private int x;

    private int y;

    /**
     * <p>Constructor for OnmsMapElement.</p>
     */
    protected OnmsMapElement() {
        // blank
    }

    /**
     * <p>Constructor for OnmsMapElement.</p>
     *
     * @param e a {@link org.opennms.netmgt.model.OnmsMapElement} object.
     */
    public OnmsMapElement(OnmsMapElement e) {
        this(e.map, e.elementId, e.type, e.label, e.iconName, e.x, e.y, e.id);
    }

    /**
     * <p>Constructor for OnmsMapElement.</p>
     *
     * @param map a {@link org.opennms.netmgt.model.OnmsMap} object.
     * @param elementId a int.
     * @param type a {@link java.lang.String} object.
     * @param label a {@link java.lang.String} object.
     * @param iconName a {@link java.lang.String} object.
     * @param x a int.
     * @param y a int.
     * @param id a int.
     */
    public OnmsMapElement(OnmsMap map, int elementId, String type, String label,
            String iconName, int x, int y, int id) {
        this.map = map;
        this.id = id;
        this.elementId = elementId;
        setType(type);
        this.label = label;
        setIconName(iconName);
        this.x = x;
        this.y = y;
        
    }

    /**
     * <p>Constructor for OnmsMapElement.</p>
     *
     * @param map a {@link org.opennms.netmgt.model.OnmsMap} object.
     * @param elementId a int.
     * @param type a {@link java.lang.String} object.
     * @param label a {@link java.lang.String} object.
     * @param iconName a {@link java.lang.String} object.
     * @param x a int.
     * @param y a int.
     */
    public OnmsMapElement(OnmsMap map, int elementId, String type, String label,
            String iconName, int x, int y) {
        this.map = map;
        this.elementId = elementId;
        setType(type);
        this.label = label;
        setIconName(iconName);
        this.x = x;
        this.y = y;
    }

    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return a int.
     */
    @XmlTransient
    @Id
    @Column(name="id", nullable=false)
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public int getId() {
        return id;
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
     * <p>getMapElementId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlID
    @Transient
    public String getMapElementId() {
        return Integer.toString(id);
    }


    /**
     * <p>Getter for the field <code>elementId</code>.</p>
     *
     * @return a int.
     */
    @Column(name = "elementId")
    public int getElementId() {
        return elementId;
    }

    /**
     * <p>Setter for the field <code>elementId</code>.</p>
     *
     * @param elementId a int.
     */
    public void setElementId(int elementId) {
        this.elementId = elementId;
    }

    /**
     * <p>Getter for the field <code>type</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "elementType")
    public String getType() {
        return type;
    }

    /**
     * <p>Setter for the field <code>type</code>.</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public void setType(String type) {
        if (type.equals(MAP_TYPE) || type.equals(NODE_TYPE) || type.equals(NODE_HIDE_TYPE) || type.equals(MAP_HIDE_TYPE))
            this.type = type;
    }

    /**
     * <p>Getter for the field <code>label</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "elementLabel")
    public String getLabel() {
        return label;
    }

    /**
     * <p>Setter for the field <code>label</code>.</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * <p>Getter for the field <code>iconName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "elementIcon")
    public String getIconName() {
        return iconName;
    }

    /**
     * <p>Setter for the field <code>iconName</code>.</p>
     *
     * @param iconName a {@link java.lang.String} object.
     */
    public void setIconName(String iconName) {
        if(iconName==null){
    		iconName=defaultNodeIcon;
    	}
        this.iconName = iconName;
    }

    /**
     * <p>Getter for the field <code>x</code>.</p>
     *
     * @return a int.
     */
    @Column(name = "elementX")
    public int getX() {
        return x;
    }

    /**
     * <p>Setter for the field <code>x</code>.</p>
     *
     * @param x a int.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * <p>Getter for the field <code>y</code>.</p>
     *
     * @return a int.
     */
    @Column(name = "elementY")
    public int getY() {
        return y;
    }

    /**
     * <p>Setter for the field <code>y</code>.</p>
     *
     * @param y a int.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * <p>Getter for the field <code>map</code>.</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsMap} object.
     */
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="mapId")
    @XmlIDREF
    public OnmsMap getMap() {
        return map;
    }

    /**
     * <p>Setter for the field <code>map</code>.</p>
     *
     * @param map a {@link org.opennms.netmgt.model.OnmsMap} object.
     */
    public void setMap(OnmsMap map) {
        this.map = map;
    }
}
