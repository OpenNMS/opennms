/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.tca.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * The Class TcaDataCollectionConfig.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="tca-collection")
public class TcaDataCollection implements Serializable, Comparable<TcaDataCollection> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4105044141350925553L;

	/** The Collection name. */
	@XmlAttribute(name="name", required=true)
	private String m_name;

	/** The RRD configuration object. */
	@XmlElement(name="rrd", required=true)
	private TcaRrd m_rrd;

	/**
	 * Instantiates a new TCA data collection.
	 */
	public TcaDataCollection() {

	}

	/**
	 * Gets the collection name.
	 *
	 * @return the collection name
	 */
	@XmlTransient
	public String getName() {
		return m_name;
	}

	/**
	 * Sets the collection name.
	 *
	 * @param name the collection name
	 */
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * Gets the RRD.
	 *
	 * @return the RRD
	 */
	@XmlTransient
	public TcaRrd getRrd() {
		return m_rrd;
	}

	/**
	 * Sets the RRD.
	 *
	 * @param rrd the new RRD
	 */
	public void setRrd(TcaRrd rrd) {
		m_rrd = rrd;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
        @Override
	public int compareTo(TcaDataCollection obj) {
		return new CompareToBuilder()
		.append(getName(), obj.getName())
		.append(getRrd(), obj.getRrd())
		.toComparison();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TcaDataCollection) {
			TcaDataCollection other = (TcaDataCollection) obj;
			return new EqualsBuilder()
			.append(getName(), other.getName())
			.append(getRrd(), other.getRrd())
			.isEquals();
		}
		return false;
	}
}
