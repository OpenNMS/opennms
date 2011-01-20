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
// 2007 Apr 05: Organized imports. - dj@opennms.org
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
import java.util.HashSet;
import java.util.Set;

import org.opennms.netmgt.model.OnmsNode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javax.xml.bind.annotation.XmlIDREF;
import javax.persistence.OneToOne;
import javax.persistence.FetchType;

import org.hibernate.annotations.CollectionOfElements;
import org.springframework.core.style.ToStringCreator;

/**
 * <p>OnmsGeolocation class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name = "geolocation")
@Entity
@Table(name="node_geolocation")
public class OnmsGeolocation implements Serializable, Comparable<OnmsGeolocation> {

    private static final long serialVersionUID = 4694348093332239377L;
   
    /** identifier field */
    private Integer m_id;
    private OnmsNode node;

    /** persistent field */
    private Double lat;
    private Double lon;
    

    /**
     * <p>Constructor for OnmsGeolocation.</p>
     *
     * @param lat a Double object.
     * @param lonr a Double object.
     */
    public OnmsGeolocation(Double lat, Double lon) {
       this.lat = lat;
       this.lon = lon;
    }

    /**
     * default constructor
     */
    public OnmsGeolocation() {
    }
   
   @Id
   @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
   @GeneratedValue(generator="opennmsSequence")    
   public Integer getId() {
      return m_id;
   }
   
   protected void setId(Integer id) {
      m_id = id;
   }   


    /**
     * <p>getNodeId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @XmlIDREF
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="nodeId")	 
    public OnmsNode getNode() {
        return node;
    }

    /**
     * <p>setNodeId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
   public void setNode(OnmsNode node) {
      this.node = node;
    }

    /**
     * <p>getLat</p>
     *
     * @return a {@link java.lang.Double} object.
     */
   @XmlAttribute(name="latitude")
   @Column(name="geolocationLatitude", unique=false, nullable=false)
   public Double getLat() {
        return this.lat;
    }
    /**
     * <p>setlat</p>
     *
     * @param lat a {@link java.lang.Double} object.
     */
    public void setLat(Double lat) {
       System.out.println(" ***************************************** GEO SETLAT ****************************************" + lat);
       this.lat = lat;
    }

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.Double} object.
     */
    @XmlElement(name="longitude")
    @Column(name="geolocationLongitude")
    public Double getLon() {
       System.out.println(" ***************************************** GEO SETLON ****************************************" + lon);
       
       return this.lon;
    }
   
     /**
      * <p>setDescription</p>
      *
      * @param lon a {@link java.lang.Double} object.
      */
    public void setLon(Double lon) {
       this.lon = lon;
    }
	
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return new ToStringCreator(this)
	 //.append("id",  getNodeId())
            .append("lat", getLat())
            .append("lon", getLon())
            .toString();
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (obj instanceof OnmsGeolocation) {
            OnmsGeolocation t = (OnmsGeolocation)obj;
            return (this.lat == t.lat && this.lon == t.lon);
        }
        return false;
    }

    public void mergeLocation(OnmsGeolocation newLocation) {
	this.setLat(newLocation.getLat());
	this.setLon(newLocation.getLon());	
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
   //public int hashCode() {
    //    return m_name.hashCode();
   // }

    /**
     * <p>compareTo</p>
     *
     * @param o a {@link org.opennms.netmgt.model.OnmsGeolocation} object.
     * @return a int.
     */
    public int compareTo(OnmsGeolocation o) {
       return (((o.lat - this.lat) < 0) ? -1 : 1);
    }

}
