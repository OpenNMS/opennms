/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
