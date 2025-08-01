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
package org.opennms.netmgt.xml.eventconf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * The event correlation information
 */
@XmlRootElement(name="correlation")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
public class Correlation implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The state determines if event is correlated
     */
    @XmlAttribute(name="state")
    private StateType m_state;

    /**
     * The correlation path
     */
    @XmlAttribute(name="path")
    @XmlJavaTypeAdapter(PathTypeAdapter.class)
    private PathType m_path;

    /**
     * A canceling UEI for this event
     */
    @XmlElement(name="cuei")
    private List<String> m_cueis = new ArrayList<>();

    /**
     * The minimum count for this event
     */
    @XmlElement(name="cmin")
    private String m_cmin;

    /**
     * The maximum count for this event
     */
    @XmlElement(name="cmax")
    private String m_cmax;

    /**
     * The correlation time for this event
     */
    @XmlElement(name="ctime")
    private String m_ctime;

    public StateType getState() {
        return m_state == null? StateType.OFF : m_state; // XSD default is off
    }

    public void setState(final StateType state) {
        m_state = state;
    }

    public PathType getPath() {
        return m_path == null? PathType.SUPPRESS_DUPLICATES : m_path; // XSD default is suppressDuplicates
    }

    public void setPath(final PathType path) {
        m_path = path;
    }

    public List<String> getCueis() {
        return m_cueis;
    }

    public void setCueis(final List<String> cueis) {
        if (cueis == m_cueis) return;
        m_cueis.clear();
        for (final String cuei : cueis) {
            m_cueis.add(cuei.intern());
        }
    }

    public void addCuei(final String cuei) {
        m_cueis.add(ConfigUtils.normalizeAndInternString(cuei));
    }

    public boolean removeCuei(final String cuei) {
        return m_cueis.remove(cuei);
    }

    public String getCmin() {
        return m_cmin;
    }

    public void setCmin(final String cmin) {
        m_cmin = ConfigUtils.normalizeAndInternString(cmin);
    }

    public String getCmax() {
        return m_cmax;
    }

    public void setCmax(final String cmax) {
        m_cmax = ConfigUtils.normalizeAndInternString(cmax);
    }

    public String getCtime() {
        return m_ctime;
    }

    public void setCtime(final String ctime) {
        m_ctime = ConfigUtils.normalizeAndInternString(ctime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_state, m_path, m_cueis, m_cmin, m_cmax, m_ctime);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Correlation) {
            final Correlation that = (Correlation) obj;
            return Objects.equals(this.m_state, that.m_state) &&
                    Objects.equals(this.m_path, that.m_path) &&
                    Objects.equals(this.m_cueis, that.m_cueis) &&
                    Objects.equals(this.m_cmin, that.m_cmin) &&
                    Objects.equals(this.m_cmax, that.m_cmax) &&
                    Objects.equals(this.m_ctime, that.m_ctime);
        }
        return false;
    }

}
