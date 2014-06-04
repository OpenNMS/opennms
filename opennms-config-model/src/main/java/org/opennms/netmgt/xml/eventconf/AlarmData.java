/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.eventconf;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * This element is used for converting events into alarms.
 */
@XmlRootElement(name="alarm-data")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
public class AlarmData implements Serializable {
	private static final long serialVersionUID = -4111377873947389525L;

	// @NotNull
	@XmlAttribute(name="reduction-key", required=true)
    private String m_reductionKey;

	// @Min(1)
	@XmlAttribute(name="alarm-type")
    private Integer m_alarmType;

    @XmlAttribute(name="clear-key")
    private String m_clearKey;

    @XmlAttribute(name="auto-clean")
    private Boolean m_autoClean;

    // @Pattern(regexp="(CommunicationsAlarm|ProcessingErrorAlarm|EnvironmentalAlarm|QualityOfServiceAlarm|EquipmentAlarm|IntegrityViolation|SecurityViolation|TimeDomainViolation|OperationalViolation|PhysicalViolation)")
    @XmlAttribute(name="x733-alarm-type")
    private String m_x733AlarmType;

    @XmlAttribute(name="x733-probable-cause")
    private Integer m_x733ProbableCause;
    
    @XmlElement(name="update-field", required=false)
    private List<UpdateField> m_updateFields = new ArrayList<UpdateField>();


    public void deleteAlarmType() {
        m_alarmType = null;
    }

    public void deleteX733ProbableCause() {
        m_x733ProbableCause= null;
    }

    public Integer getAlarmType() {
        return m_alarmType;
    }

    public Boolean getAutoClean() {
        return m_autoClean == null? Boolean.FALSE : m_autoClean; // XSD default is false
    }

    public String getClearKey() {
        return m_clearKey;
    }

    public String getReductionKey() {
        return m_reductionKey;
    }

    public String getX733AlarmType() {
        return m_x733AlarmType;
    }

    public Integer getX733ProbableCause() {
        return m_x733ProbableCause;
    }

    public boolean hasAlarmType() {
        return m_alarmType != null;
    }

    public boolean hasX733ProbableCause() {
        return m_x733ProbableCause != null;
    }

    public boolean hasAutoClean() {
        return m_autoClean != null;
    }

    public boolean isAutoClean() {
        return m_autoClean;
    }

    /**
     * @return true if this object is valid according to the schema
     */
    public boolean isValid() {
        try {
            validate();
        } catch (final ValidationException vex) {
            return false;
        }
        return true;
    }

    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    public void setAlarmType(final Integer alarmType) {
        m_alarmType = alarmType;
    }

    public void setAutoClean(final Boolean autoClean) {
        m_autoClean = autoClean;
    }

    public void setClearKey(final String clearKey) {
        m_clearKey = clearKey == null? null : clearKey.intern();
    }

    public void setReductionKey(final String reductionKey) {
        m_reductionKey = reductionKey == null? null : reductionKey.intern();
    }

    public void setX733AlarmType(final String x733AlarmType) {
        m_x733AlarmType = x733AlarmType == null? null : x733AlarmType.intern();
    }

    public void setX733ProbableCause(final Integer x733ProbableCause) {
        m_x733ProbableCause = x733ProbableCause;
    }

    public static AlarmData unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (AlarmData) Unmarshaller.unmarshal(AlarmData.class, reader);
    }

    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

	public void deleteAutoClean() {
		m_autoClean = null;
	}

	public boolean hasUpdateFields() {
	    return m_updateFields.isEmpty() ? false : true;
	}

    public List<UpdateField> getUpdateFieldList() {
        return Collections.unmodifiableList(m_updateFields);
    }

    public void setUpdateFieldList(final List<UpdateField> updateFields) {
    	if (m_updateFields == updateFields) return;
    	m_updateFields.clear();
    	m_updateFields.addAll(updateFields);
    }
    
    public void deleteUpdateFieldList() {
        m_updateFields.clear();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_alarmType == null) ? 0 : m_alarmType.hashCode());
		result = prime * result + ((m_autoClean == null) ? 0 : m_autoClean.hashCode());
		result = prime * result + ((m_clearKey == null) ? 0 : m_clearKey.hashCode());
		result = prime * result + ((m_reductionKey == null) ? 0 : m_reductionKey.hashCode());
		result = prime * result + ((m_updateFields == null) ? 0 : m_updateFields.hashCode());
		result = prime * result + ((m_x733AlarmType == null) ? 0 : m_x733AlarmType.hashCode());
		result = prime * result + ((m_x733ProbableCause == null) ? 0 : m_x733ProbableCause.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof AlarmData)) return false;
		final AlarmData other = (AlarmData) obj;
		if (m_alarmType == null) {
			if (other.m_alarmType != null) return false;
		} else if (!m_alarmType.equals(other.m_alarmType)) {
			return false;
		}
		if (m_autoClean == null) {
			if (other.m_autoClean != null) return false;
		} else if (!m_autoClean.equals(other.m_autoClean)) {
			return false;
		}
		if (m_clearKey == null) {
			if (other.m_clearKey != null) return false;
		} else if (!m_clearKey.equals(other.m_clearKey)) {
			return false;
		}
		if (m_reductionKey == null) {
			if (other.m_reductionKey != null) return false;
		} else if (!m_reductionKey.equals(other.m_reductionKey)) {
			return false;
		}
		if (m_updateFields == null) {
			if (other.m_updateFields != null) return false;
		} else if (!m_updateFields.equals(other.m_updateFields)) {
			return false;
		}
		if (m_x733AlarmType == null) {
			if (other.m_x733AlarmType != null) return false;
		} else if (!m_x733AlarmType.equals(other.m_x733AlarmType)) {
			return false;
		}
		if (m_x733ProbableCause == null) {
			if (other.m_x733ProbableCause != null) return false;
		} else if (!m_x733ProbableCause.equals(other.m_x733ProbableCause)) {
			return false;
		}
		return true;
	}

    
}
