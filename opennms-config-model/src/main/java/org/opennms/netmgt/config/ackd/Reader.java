/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.ackd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "reader")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("ackd-configuration.xsd")
public class Reader implements Serializable {
    private static final long serialVersionUID = 2L;

    public static final boolean DEFAULT_ENABLED_FLAG = true;

    /**
     * The reader name is the value returned by the getName() method required
     * by the AckReader interface. Readers are currently wired in using
     * Spring.
     */
    @XmlAttribute(name = "reader-name")
    private String m_readerName;

    /**
     * Field m_enabled.
     */
    @XmlAttribute(name = "enabled")
    private Boolean m_enabled;

    /**
     * A very basic configuration for defining simple input to a schedule
     */
    @XmlElement(name = "reader-schedule")
    private ReaderSchedule m_readerSchedule;

    /**
     * Parameters to be used for collecting this service. Parameters are
     * specific to the service monitor.
     */
    @XmlElement(name = "parameter")
    private List<Parameter> m_parameters = new ArrayList<>();


    public Reader() {
    }

    public Reader(final String name, final boolean enabled, final ReaderSchedule schedule, final List<Parameter> parameters) {
        setReaderName(name);
        setEnabled(enabled);
        setReaderSchedule(schedule);
        setParameters(parameters);
    }

    public String getReaderName() {
        return m_readerName;
    }

    public void setReaderName(final String readerName) {
        m_readerName = readerName;
    }

    public boolean getEnabled() {
        return m_enabled == null ? DEFAULT_ENABLED_FLAG : m_enabled;
    }

    public void setEnabled(final Boolean enabled) {
        m_enabled = enabled;
    }

    public ReaderSchedule getReaderSchedule() {
        return m_readerSchedule;
    }

    public void setReaderSchedule(final ReaderSchedule readerSchedule) {
        m_readerSchedule = readerSchedule;
    }

    public List<Parameter> getParameters() {
        return m_parameters;
    }

    public void setParameters(final List<Parameter> parameters) {
        if (m_parameters == parameters) {
            return;
        }
        m_parameters.clear();
        if (parameters != null) m_parameters.addAll(parameters);
    }

    public void addParameter(final Parameter parameter) {
        m_parameters.add(parameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_readerName, m_enabled, m_readerSchedule, m_parameters);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Reader) {
            final Reader that = (Reader) obj;
            return Objects.equals(this.m_readerName, that.m_readerName) &&
                    Objects.equals(this.m_enabled, that.m_enabled) &&
                    Objects.equals(this.m_readerSchedule, that.m_readerSchedule) &&
                    Objects.equals(this.m_parameters, that.m_parameters);
        }
        return false;
    }
}
