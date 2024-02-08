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

    @XmlElement(name="managed-object", required=false)
    private ManagedObject m_managedObject;


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

    public ManagedObject getManagedObject() {
        return m_managedObject;
    }

    public void setManagedObject(ManagedObject managedObject) {
        this.m_managedObject = managedObject;
    }
    @Override
    public int hashCode() {
        return Objects.hash(m_reductionKey,
                            m_alarmType,
                            m_clearKey,
                            m_autoClean,
                            m_x733AlarmType,
                            m_x733ProbableCause,
                            m_updateFields,
                            m_managedObject);
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
                    Objects.equals(this.m_updateFields, that.m_updateFields) &&
                    Objects.equals(this.m_managedObject, that.m_managedObject);
        }
        return false;
    }


}