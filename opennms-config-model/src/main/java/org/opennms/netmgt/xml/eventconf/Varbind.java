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
@XmlType(propOrder={"m_vbnumber", "m_vboid", "m_values"})
public class Varbind implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final List<String> TEXTUAL_CONVENTIONS = Arrays.asList("PhysAddress","MacAddress","TruthValue","TestAndIncr","AutonomousType","InstancePointer","VariablePointer","RowPointer","RowStatus","TimeStamp","TimeInterval","DateAndTime","StorageType","TDomain","TAddress");

    @XmlAttribute(name="textual-convention", required=false)
    private String m_textualConvention;

    @XmlElement(name="vbnumber", required=false)
    private Integer m_vbnumber;

    @XmlElement(name="vboid", required=false)
    private String m_vboid;

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
        m_vbnumber = vbnumber;
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


    public String getVboid() {
        return m_vboid;
    }

    public void setVboid(final String vboid) {
        this.m_vboid = vboid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_textualConvention, m_vbnumber, m_vboid, m_values);
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
                    Objects.equals(this.m_vboid, that.m_vboid) &&
                    Objects.equals(this.m_values, that.m_values);
        }
        return false;
    }

    public EventMatcher constructMatcher() {
        if (m_vbnumber == null && m_vboid == null) return EventMatchers.trueMatcher();

        if (m_vbnumber != null) {
            final List<EventMatcher> valueMatchers = new ArrayList<EventMatcher>(m_values.size());
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
                final EventMatcher[] matchers = valueMatchers.toArray(new EventMatcher[valueMatchers.size()]);
                return EventMatchers.or(matchers);
            }
        } else {
            final List<EventMatcher> valueMatchers = new ArrayList<EventMatcher>(m_values.size());
            for(final String value : m_values) {
                if (value == null) {
                    continue;
                }
                if (value.startsWith("~")) {
                    valueMatchers.add(valueMatchesRegexMatcher(varbind(m_vboid), value));
                } else if (value.endsWith("%")) {
                    valueMatchers.add(valueStartsWithMatcher(varbind(m_vboid), value));
                } else {
                    valueMatchers.add(valueEqualsMatcher(varbind(m_vboid), value));
                }
            }

            if (valueMatchers.size() == 1) {
                return valueMatchers.get(0);
            } else {
                final EventMatcher[] matchers = valueMatchers.toArray(new EventMatcher[valueMatchers.size()]);
                return EventMatchers.or(matchers);
            }
        }
    }
}
