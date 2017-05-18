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

import static org.opennms.netmgt.xml.eventconf.EventMatchers.valueEqualsMatcher;
import static org.opennms.netmgt.xml.eventconf.EventMatchers.valueMatchesRegexMatcher;
import static org.opennms.netmgt.xml.eventconf.EventMatchers.valueStartsWithMatcher;
import static org.opennms.netmgt.xml.eventconf.EventMatchers.varbind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name="varbind")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder={"m_vbnumber", "m_values"})
public class Varbind implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final List<String> TEXTUAL_CONVENTIONS = Arrays.asList("PhysAddress","MacAddress","TruthValue","TestAndIncr","AutonomousType","InstancePointer","VariablePointer","RowPointer","RowStatus","TimeStamp","TimeInterval","DateAndTime","StorageType","TDomain","TAddress");

    @XmlAttribute(name="textual-convention", required=false)
    private String m_textualConvention;

    @XmlElement(name="vbnumber", required=true)
    private Integer m_vbnumber;

    @XmlElement(name="vbvalue", required=true)
    private List<String> m_values = new ArrayList<>();

    public String getTextualConvention() {
        return m_textualConvention;
    }

    public void setTextualConvention(final String textualConvention) {
        m_textualConvention = ConfigUtils.assertOnlyContains(textualConvention, TEXTUAL_CONVENTIONS, "textual-convention");
        if (m_textualConvention != null) {
            m_textualConvention = m_textualConvention.intern();
        }
    }

    public Integer getVbnumber() {
        return m_vbnumber;
    }

    public void setVbnumber(final Integer vbnumber) {
        m_vbnumber = ConfigUtils.assertNotNull(vbnumber, "vbnumber");
    }

    public List<String> getVbvalues() {
        return m_values;
    }

    public void setVbvalues(final List<String> values) {
        if (values == m_values) return;
        m_values.clear();
        if (values != null) m_values.addAll(values);
    }

    public void addVbvalue(final String value) throws IndexOutOfBoundsException {
        m_values.add(value == null? null : value.intern());
    }

    public boolean removeVbvalue(final String value) {
        return m_values.remove(value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_textualConvention, m_vbnumber, m_values);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Varbind) {
            final Varbind that = (Varbind) obj;
            return Objects.equals(this.m_textualConvention, that.m_textualConvention) &&
                    Objects.equals(this.m_vbnumber, that.m_vbnumber) &&
                    Objects.equals(this.m_values, that.m_values);
        }
        return false;
    }

    public EventMatcher constructMatcher() {
        if (m_vbnumber == null) return EventMatchers.trueMatcher();

        List<EventMatcher> valueMatchers = new ArrayList<EventMatcher>(m_values.size());
        for(final String value : m_values) {
            if (value == null) continue;
            if (value.startsWith("~")) {
                valueMatchers.add(valueMatchesRegexMatcher(varbind(m_vbnumber), value));
            } else if (value.endsWith("%")) {
                valueMatchers.add(valueStartsWithMatcher(varbind(m_vbnumber), value));
            } else {
                valueMatchers.add(valueEqualsMatcher(varbind(m_vbnumber), value));
            }
        }

        if (valueMatchers.size() == 1) {
            return valueMatchers.get(0);
        } else {
            EventMatcher[] matchers = valueMatchers.toArray(new EventMatcher[valueMatchers.size()]);
            return EventMatchers.or(matchers);
        }

    }	

}
