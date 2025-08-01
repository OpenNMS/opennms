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
    private List<String> m_importMbeansList = new ArrayList<>();

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
