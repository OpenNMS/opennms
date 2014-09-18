/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.collectd.jmx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name="jmx-collection")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class JmxCollection implements java.io.Serializable {

    @XmlAttribute(name="name", required=true)
    private String m_name;

    /**
     * @deprecated
     */
    @XmlTransient
    @Deprecated
    private int m_maxVarsPerPdu = 0;

    @XmlElement(name="rrd", required=true)
    private Rrd m_rrd;

    @XmlElement(name="mbean")
    @XmlElementWrapper(name="mbeans")
    private List<Mbean> m_mbeans = new ArrayList<>();

    @XmlElement(name="import-mbeans", required=false)
    private List<String> m_importMbeansList = new ArrayList<String>();

    /**
     * Gets the import MBeans list.
     *
     * @return the import MBeans list
     */
    @XmlTransient
    public List<String> getImportGroupsList() {
        return m_importMbeansList;
    }

    /**
     * Sets the import MBeans list.
     *
     * @param importMbeansList the new import MBeans list
     */
    public void setImportGroupsList(List<String> importMbeansList) {
        m_importMbeansList = importMbeansList;
    }

    /**
     * Checks for import MBeans.
     *
     * @return true, if successful
     */
    public boolean hasImportMbeans() {
        return m_importMbeansList != null && !m_importMbeansList.isEmpty();
    }

    /**
     * Overrides the java.lang.Object.equals method.
     *
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (obj instanceof JmxCollection) {
            JmxCollection temp = (JmxCollection) obj;

            boolean equals = Objects.equals(m_name, temp.m_name)
                    && Objects.equals(m_maxVarsPerPdu, temp.m_maxVarsPerPdu)
                    && Objects.equals(m_rrd, temp.m_rrd)
                    && Objects.equals(m_mbeans, temp.m_mbeans);
            return equals;
        }
        return false;
    }

    public int getMaxVarsPerPdu(
    ) {
        return m_maxVarsPerPdu;
    }

    public List<Mbean> getMbeans() {
        return m_mbeans;
    }

    public String getName() {
        return m_name;
    }

    public Rrd getRrd() {
        return m_rrd;
    }

    /**
     * @return true if at least one MaxVarsPerPdu has been added
     */
    public boolean hasMaxVarsPerPdu() {
        return m_maxVarsPerPdu != 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_maxVarsPerPdu, m_rrd, m_mbeans);
    }

    /**
     * Method getMbeanCount.
     *
     * @return the size of this collection
     */
    public int getMbeanCount() {
        return this.m_mbeans.size();
    }

    public void addMbean(Mbean mbean) {
        if (mbean != null) {
            m_mbeans.add(mbean);
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setMaxVarsPerPdu(final int maxVarsPerPdu) {
        m_maxVarsPerPdu = maxVarsPerPdu;
    }

    public void setMbeans(final List<Mbean> mbeans) {
        m_mbeans = mbeans;
    }

    public void setName(final String name) {
        m_name = name;
    }

    public void setRrd(final Rrd rrd) {
        m_rrd = rrd;
    }

    public void addMbeans(List<Mbean> mbeanList) {
        m_mbeans.addAll(mbeanList);
    }
}
