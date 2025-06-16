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
package org.opennms.protocols.xml.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * The Class XmlRrd.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="rrd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlRrd implements Serializable, Comparable<XmlRrd>, Cloneable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 143526958273169546L;

    /** The step. */
    @XmlAttribute(name="step")
    private Integer m_step;

    /** The XML RRAs list. */
    @XmlElement(name="rra")
    private List<String> m_xmlRras = new ArrayList<>();

    /**
     * Instantiates a new XML RRD.
     */
    public XmlRrd() {

    }

    public XmlRrd(XmlRrd copy) {
        m_step = copy.m_step;
        m_xmlRras.addAll(copy.m_xmlRras);
    }

    /**
     * Gets the step.
     *
     * @return the step
     */
    public Integer getStep() {
        return m_step;
    }

    /**
     * Sets the step.
     *
     * @param step the new step
     */
    public void setStep(Integer step) {
        m_step = step;
    }

    /**
     * Gets the XML RRAs.
     *
     * @return the XML RRAs
     */
    public List<String> getXmlRras() {
        return m_xmlRras;
    }

    /**
     * Sets the XML RRAs.
     *
     * @param xmlRras the new XML RRAs
     */
    public void setXmlRras(List<String> xmlRras) {
        m_xmlRras = xmlRras;
    }

    /**
     * Adds a new RRA.
     *
     * @param rra the RRA
     */
    public void addRra(String rra) {
        m_xmlRras.add(rra);
    }

    /**
     * Removes a RRA.
     *
     * @param rra the RRA
     */
    public void removeRra(String rra) {
        m_xmlRras.remove(rra);
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(XmlRrd obj) {
        return new CompareToBuilder()
        .append(getStep(), obj.getStep())
        .append(getXmlRras().toArray(), obj.getXmlRras().toArray())
        .toComparison();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof XmlRrd) {
            XmlRrd other = (XmlRrd) obj;
            return new EqualsBuilder()
            .append(getStep(), other.getStep())
            .append(getXmlRras().toArray(), other.getXmlRras().toArray())
            .isEquals();
        }
        return false;
    }

    @Override
    public XmlRrd clone() {
        return new XmlRrd(this);
    }
}
