/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
