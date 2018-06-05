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

import static org.opennms.netmgt.xml.eventconf.EventMatchers.field;
import static org.opennms.netmgt.xml.eventconf.EventMatchers.valueEqualsMatcher;
import static org.opennms.netmgt.xml.eventconf.EventMatchers.valueMatchesRegexMatcher;
import static org.opennms.netmgt.xml.eventconf.EventMatchers.valueStartsWithMatcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * The mask element
 */
@XmlRootElement(name="maskelement")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder={"m_name", "m_values"})
public class Maskelement implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The UEI xml tag
     */
    public static final String TAG_UEI = "uei";

    /**
     * The event source xml tag
     */
    public static final String TAG_SOURCE = "source";

    /**
     * The event nodeid xml tag
     */
    public static final String TAG_NODEID = "nodeid";

    /**
     * The event host xml tag
     */
    public static final String TAG_HOST = "host";

    /**
     * The event interface xml tag
     */
    public static final String TAG_INTERFACE = "interface";

    /**
     * The event snmp host xml tag
     */
    public static final String TAG_SNMPHOST = "snmphost";

    /**
     * The event service xml tag
     */
    public static final String TAG_SERVICE = "service";

    /**
     * The SNMP EID xml tag
     */
    public static final String TAG_SNMP_EID = "id";

    /**
     * The SNMP specific xml tag
     */
    public static final String TAG_SNMP_SPECIFIC = "specific";

    /**
     * The SNMP generic xml tag
     */
    public static final String TAG_SNMP_GENERIC = "generic";

    /**
     * The SNMP community xml tag
     */
    public static final String TAG_SNMP_COMMUNITY = "community";

    @XmlElement(name="mename", required=true)
    private String m_name;

    @XmlElement(name="mevalue", required=true)
    private List<String> m_values = new ArrayList<>();

    public void addMevalue(final String value) {
        m_values.add(value.intern());
    }

    /**
     * <p>
     * The mask element name. Must be from the following subset:
     * </p>
     * <dl>
     * <dt>uei</dt><dd>the OpenNMS Universal Event Identifier</dd>
     * <dt>source</dt><dd>source of the event; "trapd" for received SNMP traps;
     * warning: these aren't standardized</dd>
     * <dt>host</dt><dd>host related to the event; for SNMP traps this is the
     * IP source address of the host that sent the trap to OpenNMS</dd>
     * <dt>snmphost</dt><dd>SNMP host related to  the event; for SNMPv1 traps
     * this is IP address reported in the trap; for SNMPv2 traps and later this
     * is the same as "host"</dd>
     * <dt>nodeid</dt><dd>the OpenNMS node identifier for the node related
     * to this event</dd>
     * <dt>interface</dt><dd>interface related to the event; for SNMP
     * traps this is the same as "snmphost"</dd>
     * <dt>service</dt><dd>Service name</dd>
     * <dt>id</dt><dd>enterprise ID in an SNMP trap</dd>
     * <dt>specific</dt><dd>specific value in an SNMP trap</dd>
     * <dt>generic</dt><dd>generic value in an SNMP trap</dd>
     * <dt>community</dt><dd>community string in an SNMP trap</dd>
     * </dl>
     */
    public String getMename() {
        return m_name;
    }

    public List<String> getMevalues() {
        return m_values;
    }

    public boolean removeMevalue(final String value) {
        return m_values.remove(value);
    }

    public void setMename(final String mename) {
        m_name = ConfigUtils.assertNotEmpty(mename, "mename").intern();
    }

    public void setMevalues(final List<String> values) {
        if (values == m_values) return;
        m_values.clear();
        for (final String value : values) {
            m_values.add(value.intern());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_values);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Maskelement) {
            final Maskelement that = (Maskelement) obj;
            return Objects.equals(this.m_name, that.m_name) &&
                    Objects.equals(this.m_values, that.m_values);
        }
        return false;
    }



    public EventMatcher constructMatcher() {
        List<EventMatcher> valueMatchers = new ArrayList<EventMatcher>(m_values.size());
        for(String value : m_values) {
            if (value == null) continue;
            if (value.startsWith("~")) {
                valueMatchers.add(valueMatchesRegexMatcher(field(m_name), value));
            } else if (value.endsWith("%")) {
                valueMatchers.add(valueStartsWithMatcher(field(m_name), value));
            } else {
                valueMatchers.add(valueEqualsMatcher(field(m_name), value));
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
