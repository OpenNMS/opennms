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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "parameters")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("reporting.xsd")
public class Parameters implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name = "string-parm")
    private List<StringParm> m_stringParms = new ArrayList<>();

    @XmlElement(name = "date-parm")
    private List<DateParm> m_dateParms = new ArrayList<>();

    @XmlElement(name = "int-parm")
    private List<IntParm> m_intParms = new ArrayList<>();

    public List<StringParm> getStringParms() {
        return m_stringParms;
    }

    public void setStringParms(final List<StringParm> stringParms) {
        if (stringParms == m_stringParms) return;
        m_stringParms.clear();
        if (stringParms != null) m_stringParms.addAll(stringParms);
    }

    public void addStringParm(final StringParm stringParm) {
        m_stringParms.add(stringParm);
    }

    public boolean removeStringParm(final StringParm stringParm) {
        return m_stringParms.remove(stringParm);
    }

    public List<DateParm> getDateParms() {
        return m_dateParms;
    }

    public void setDateParms(final List<DateParm> dateParms) {
        if (dateParms == m_dateParms) return;
        m_dateParms.clear();
        if (dateParms != null) m_dateParms.addAll(dateParms);
    }

    public void addDateParm(final DateParm dateParm) {
        m_dateParms.add(dateParm);
    }

    public boolean removeDateParm(final DateParm dateParm) {
        return m_dateParms.remove(dateParm);
    }

    public List<IntParm> getIntParms() {
        return m_intParms;
    }

    public void setIntParms(final List<IntParm> intParms) {
        if (intParms == m_intParms) return;
        m_intParms.clear();
        if (intParms != m_intParms) m_intParms.addAll(intParms);
    }

    public void addIntParm(final IntParm intParm) {
        m_intParms.add(intParm);
    }

    public boolean removeIntParm(final IntParm intParm) {
        return m_intParms.remove(intParm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_stringParms, 
                            m_dateParms, 
                            m_intParms);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Parameters) {
            final Parameters that = (Parameters)obj;
            return Objects.equals(this.m_stringParms, that.m_stringParms)
                    && Objects.equals(this.m_dateParms, that.m_dateParms)
                    && Objects.equals(this.m_intParms, that.m_intParms);
        }
        return false;
    }

}
