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
