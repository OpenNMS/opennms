/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

import java.io.File;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.opennms.netmgt.model.RrdRepository;

/**
 * The Class TcaDataCollectionConfig.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="tca-datacollection-config")
public class TcaDataCollectionConfig implements Serializable, Comparable<TcaDataCollectionConfig> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5432437490676491057L;

    /** The Constant TCA_DATACOLLECTION_CONFIG_FILE. */
    public static final String TCA_DATACOLLECTION_CONFIG_FILE = "tca-datacollection-config.xml";

	/** The RRD Repository. */
	@XmlAttribute(name="rrdRepository", required=true)
	private String m_rrdRepository;

	/** The RRD configuration object. */
	@XmlElement(name="rrd", required=true)
	private TcaRrd m_rrd;

	/**
	 * Instantiates a new TCA data collection configuration.
	 */
	public TcaDataCollectionConfig() {

	}

	/**
	 * Gets the RRD repository.
	 *
	 * @return the RRD repository
	 */
	@XmlTransient
	public String getRrdRepository() {
		return m_rrdRepository;
	}

	/**
	 * Sets the RRD repository.
	 *
	 * @param rrdRepository the new RRD repository
	 */
	public void setRrdRepository(String rrdRepository) {
		m_rrdRepository = rrdRepository;
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
	public void setXmlRrd(TcaRrd rrd) {
		m_rrd = rrd;
	}

	/**
	 * Builds the RRD repository.
	 *
	 * @return the RRD repository
	 */
	public RrdRepository buildRrdRepository() {
		RrdRepository repo = new RrdRepository();
		repo.setRrdBaseDir(new File(getRrdRepository()));
		repo.setRraList(m_rrd.getRras());
		repo.setStep(m_rrd.getStep());
		repo.setHeartBeat(m_rrd.getStep());
		return repo;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TcaDataCollectionConfig obj) {
		return new CompareToBuilder()
		.append(getRrdRepository(), obj.getRrdRepository())
		.append(getRrd(), obj.getRrd())
		.toComparison();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TcaDataCollectionConfig) {
			TcaDataCollectionConfig other = (TcaDataCollectionConfig) obj;
			return new EqualsBuilder()
			.append(getRrdRepository(), other.getRrdRepository())
			.append(getRrd(), other.getRrd())
			.isEquals();
		}
		return false;
	}
}
