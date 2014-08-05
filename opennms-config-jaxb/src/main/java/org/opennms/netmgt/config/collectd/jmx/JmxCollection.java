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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="jmx-collection")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
// TODO mvr remove fishy methods
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

    @XmlElement(name="mbeans")
    private Mbeans m_mbeans;

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
        if ( this == obj )
            return true;

        if (obj instanceof JmxCollection) {

            JmxCollection temp = (JmxCollection)obj;
            if (m_name != null) {
                if (temp.m_name == null) return false;
                else if (!(this.m_name.equals(temp.m_name)))
                    return false;
            } else if (temp.m_name != null) {
                return false;
            }

            if (this.m_maxVarsPerPdu != temp.m_maxVarsPerPdu) {
                return false;
            }
            if (this.m_rrd != null) {
                if (temp.m_rrd == null) {
                    return false;
                } else if (!(this.m_rrd.equals(temp.m_rrd))) {
                    return false;
                }
            } else if (temp.m_rrd != null) {
                return false;
            }
            if (m_mbeans != null) {
                if (temp.m_mbeans == null) {
                    return false;
                } else if (!(this.m_mbeans.equals(temp.m_mbeans))) {
                    return false;
                }
            } else if (temp.m_mbeans != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    public int getMaxVarsPerPdu(
    ) {
        return m_maxVarsPerPdu;
    }

    public Mbeans getMbeans(
    ) {
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

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     *
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int result = 17;

        long tmp;
        if (m_name != null) {
           result = 37 * result + m_name.hashCode();
        }
        result = 37 * result + m_maxVarsPerPdu;
        if (m_rrd != null) {
           result = 37 * result + m_rrd.hashCode();
        }
        if (m_mbeans != null) {
           result = 37 * result + m_mbeans.hashCode();
        }

        return result;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setMaxVarsPerPdu(final int maxVarsPerPdu) {
        m_maxVarsPerPdu = maxVarsPerPdu;
    }

    public void setMbeans(final Mbeans mbeans) {
        m_mbeans = mbeans;
    }

    public void setName(final String name) {
        m_name = name;
    }

    public void setRrd(final Rrd rrd) {
        m_rrd = rrd;
    }

}
