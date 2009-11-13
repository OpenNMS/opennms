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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.model;

import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GeneratedValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlID;
import java.io.Serializable;

@XmlRootElement(name = "mapElement")
@Entity
@Table(name = "element")
public class OnmsMapElement implements Serializable {
    private static final long serialVersionUID = 1594163211618494443L;
    
    public static final String MAP_TYPE = "M";
    public static final String MAP_HIDE_TYPE = "W";
    public static final String NODE_TYPE = "N";
    public static final String NODE_HIDE_TYPE = "H";
    public static final String defaultNodeIcon = "unspecified";
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

    protected OnmsMapElement() {
        // blank
    }

    public OnmsMapElement(OnmsMapElement e) {
        this(e.map, e.elementId, e.type, e.label, e.iconName, e.x, e.y, e.id);
    }

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

    @XmlTransient
    @Id
    @Column(name="id")
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlID
    @Transient
    public String getMapElementId() {
        return Integer.toString(id);
    }


    @Column(name = "elementId")
    public int getElementId() {
        return elementId;
    }

    public void setElementId(int elementId) {
        this.elementId = elementId;
    }

    @Column(name = "elementType")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type.equals(MAP_TYPE) || type.equals(NODE_TYPE) || type.equals(NODE_HIDE_TYPE) || type.equals(MAP_HIDE_TYPE))
            this.type = type;
    }

    @Column(name = "elementLabel")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Column(name = "elementIcon")
    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        if(iconName==null){
    		iconName=defaultNodeIcon;
    	}
        this.iconName = iconName;
    }

    @Column(name = "elementX")
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    @Column(name = "elementY")
    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="mapId")
    @XmlIDREF
    public OnmsMap getMap() {
        return map;
    }

    public void setMap(OnmsMap map) {
        this.map = map;
    }
}
