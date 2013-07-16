/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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
public class XmlRrd implements Serializable, Comparable<XmlRrd> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 143526958273169546L;

    /** The step. */
    @XmlAttribute(name="step")
    private Integer m_step;

    /** The XML RRAs list. */
    @XmlElement(name="rra")
    private List<String> m_xmlRras = new ArrayList<String>();

    /**
     * Instantiates a new XML RRD.
     */
    public XmlRrd() {

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
}
