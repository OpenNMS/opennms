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
	private List<String> m_rras = new ArrayList<>();

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
