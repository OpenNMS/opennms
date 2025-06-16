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
package org.opennms.netmgt.config.reporting;


import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * An offset period used as a base to determine a real
 *  date when running the report
 */
@XmlRootElement(name = "date-parm")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("reporting.xsd")
public class DateParm implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * the name of this parameter as passed to the report engine
     */
    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * the name of this parameter as displayed in the webui
     */
    @XmlAttribute(name = "display-name", required = true)
    private String m_displayName;

    /**
     * flag to use absolute date if possible
     */
    @XmlAttribute(name = "use-absolute-date")
    private Boolean m_useAbsoluteDate;

    @XmlElement(name = "default-interval", required = true)
    private String m_defaultInterval;

    @XmlElement(name = "default-count", required = true)
    private Integer m_defaultCount;

    @XmlElement(name = "default-time")
    private DefaultTime m_defaultTime;

    public DateParm() {
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getDisplayName() {
        return m_displayName;
    }

    public void setDisplayName(final String displayName) {
        m_displayName = ConfigUtils.assertNotEmpty(displayName, "display-name");
    }

    public Optional<Boolean> getUseAbsoluteDate() {
        return Optional.ofNullable(m_useAbsoluteDate);
    }

    public void setUseAbsoluteDate(final Boolean useAbsoluteDate) {
        m_useAbsoluteDate = useAbsoluteDate;
    }

    public String getDefaultInterval() {
        return m_defaultInterval;
    }

    public void setDefaultInterval(final String defaultInterval) {
        m_defaultInterval = ConfigUtils.assertNotEmpty(defaultInterval, "default-interval");
    }

    public Integer getDefaultCount() {
        return m_defaultCount;
    }

    public void setDefaultCount(final Integer defaultCount) {
        m_defaultCount = ConfigUtils.assertNotNull(defaultCount, "default-count");
    }

    public Optional<DefaultTime> getDefaultTime() {
        return Optional.ofNullable(m_defaultTime);
    }

    public void setDefaultTime(final DefaultTime defaultTime) {
        m_defaultTime = defaultTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_displayName, 
                            m_useAbsoluteDate, 
                            m_defaultInterval, 
                            m_defaultCount, 
                            m_defaultTime);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof DateParm) {
            final DateParm that = (DateParm)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_displayName, that.m_displayName)
                    && Objects.equals(this.m_useAbsoluteDate, that.m_useAbsoluteDate)
                    && Objects.equals(this.m_defaultInterval, that.m_defaultInterval)
                    && Objects.equals(this.m_defaultCount, that.m_defaultCount)
                    && Objects.equals(this.m_defaultTime, that.m_defaultTime);
        }
        return false;
    }

}
