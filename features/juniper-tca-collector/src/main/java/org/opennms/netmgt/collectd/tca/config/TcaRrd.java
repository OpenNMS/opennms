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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * The Class TcaRrd.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="rrd")
public class TcaRrd implements Serializable, Comparable<TcaRrd> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 439792690711379417L;

	/** The collection step is a fixed constant for the TCA Collector. */
	private Integer m_step = 1;

	/** The XML RRAs list. */
	@XmlElement(name="rra", required=true)
	private List<String> m_rras = new ArrayList<String>();

	/**
	 * Instantiates a new TCA RRD.
	 */
	public TcaRrd() {

	}

	/**
	 * Gets the step.
	 *
	 * @return the step
	 */
	@XmlTransient
	public Integer getStep() {
		return m_step;
	}

	/**
	 * Gets the RRAs.
	 *
	 * @return the RRAs
	 */
	@XmlTransient
	public List<String> getRras() {
		return m_rras;
	}

	/**
	 * Sets the RRAs.
	 *
	 * @param rras the new RRAs
	 */
	public void setRras(List<String> rras) {
		m_rras = rras;
	}

	/**
	 * Adds a new RRA.
	 *
	 * @param rra the RRA
	 */
	public void addRra(String rra) {
		m_rras.add(rra);
	}

	/**
	 * Removes a RRA.
	 *
	 * @param rra the RRA
	 */
	public void removeRra(String rra) {
		m_rras.remove(rra);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
        @Override
	public int compareTo(TcaRrd obj) {
		return new CompareToBuilder()
		.append(getStep(), obj.getStep())
		.append(getRras().toArray(), obj.getRras().toArray())
		.toComparison();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TcaRrd) {
			TcaRrd other = (TcaRrd) obj;
			return new EqualsBuilder()
			.append(getStep(), other.getStep())
			.append(getRras().toArray(), other.getRras().toArray())
			.isEquals();
		}
		return false;
	}
}
