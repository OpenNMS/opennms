/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
