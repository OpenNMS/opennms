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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * This element is used for converting events into alarms.
 */
@XmlRootElement(name="alarm-data")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
public class AlarmData implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="reduction-key", required=true)
    private String m_reductionKey;

    @XmlAttribute(name="alarm-type")
    private Integer m_alarmType;

    @XmlAttribute(name="clear-key")
    private String m_clearKey;

    @XmlAttribute(name="auto-clean")
    private Boolean m_autoClean;

    @XmlAttribute(name="x733-alarm-type")
    private String m_x733AlarmType;

    @XmlAttribute(name="x733-probable-cause")
    private Integer m_x733ProbableCause;

    @XmlElement(name="update-field", required=false)
    private List<UpdateField> m_updateFields = new ArrayList<>();


    public String getReductionKey() {
        return m_reductionKey;
    }

    public void setReductionKey(final String reductionKey) {
        m_reductionKey = ConfigUtils.normalizeAndInternString(reductionKey);
    }

    public Integer getAlarmType() {
        return m_alarmType;
    }

    public void setAlarmType(final Integer alarmType) {
        m_alarmType = ConfigUtils.assertMinimumInclusive(ConfigUtils.assertNotNull(alarmType, "alarm-type"), 1, "alarm-type");
    }

    public String getClearKey() {
        return m_clearKey;
    }

    public void setClearKey(final String clearKey) {
        m_clearKey = ConfigUtils.normalizeAndInternString(clearKey);
    }

    public Boolean getAutoClean() {
        return m_autoClean == null? Boolean.FALSE : m_autoClean; // XSD default is false
    }

    public void setAutoClean(final Boolean autoClean) {
        m_autoClean = autoClean;
    }

    public String getX733AlarmType() {
        return m_x733AlarmType;
    }

    public void setX733AlarmType(final String x733AlarmType) {
        m_x733AlarmType = ConfigUtils.normalizeAndInternString(x733AlarmType);
    }

    public Integer getX733ProbableCause() {
        return m_x733ProbableCause;
    }

    public void setX733ProbableCause(final Integer x733ProbableCause) {
        m_x733ProbableCause = x733ProbableCause;
    }

    public List<UpdateField> getUpdateFields() {
        return Collections.unmodifiableList(m_updateFields);
    }

    public void setUpdateFields(final List<UpdateField> updateFields) {
        if (m_updateFields == updateFields) return;
        m_updateFields.clear();
        if (updateFields != null) m_updateFields.addAll(updateFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_reductionKey,
                            m_alarmType,
                            m_clearKey,
                            m_autoClean,
                            m_x733AlarmType,
                            m_x733ProbableCause,
                            m_updateFields);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AlarmData) {
            final AlarmData that = (AlarmData) obj;
            return Objects.equals(this.m_reductionKey, that.m_reductionKey) &&
                    Objects.equals(this.m_alarmType, that.m_alarmType) &&
                    Objects.equals(this.m_clearKey, that.m_clearKey) &&
                    Objects.equals(this.m_autoClean, that.m_autoClean) &&
                    Objects.equals(this.m_x733AlarmType, that.m_x733AlarmType) &&
                    Objects.equals(this.m_x733ProbableCause, that.m_x733ProbableCause) &&
                    Objects.equals(this.m_updateFields, that.m_updateFields);
        }
        return false;
    }


}