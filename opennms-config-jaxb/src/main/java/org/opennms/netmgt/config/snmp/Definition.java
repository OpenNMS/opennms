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

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.snmp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Provides a mechanism for associating one or more specific IP addresses
 * and/or IP address ranges with a set of SNMP parms which will be used in
 * place of the default values during SNMP data collection.
 */

@XmlRootElement(name="definition")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"m_ranges","m_specifics","m_ipMatches"})
public class Definition extends Configuration implements Serializable {
    private static final long serialVersionUID = -4574695650213727237L;

    /**
     * IP address range to which this definition
     *  applies.
     */
    @XmlElement(name="range")
    private List<Range> m_ranges = new ArrayList<Range>();

    /**
     * Specific IP address to which this definition
     *  applies.
     */
    @XmlElement(name="specific")
    private List<String> m_specifics = new ArrayList<String>();

    /**
     * Match Octets (as in IPLIKE)
     */
    @XmlElement(name="ip-match")
    private List<String> m_ipMatches = new ArrayList<String>();

    public Definition() {
        super();
    }

    public void addIpMatch(final String ipMatch) throws IndexOutOfBoundsException {
        m_ipMatches.add(ipMatch);
    }

    public void addRange(final Range range) throws IndexOutOfBoundsException {
        m_ranges.add(range);
    }

    public void addSpecific(final String specific) throws IndexOutOfBoundsException {
        m_specifics.add(specific);
    }

    public List<String> getIpMatches() {
        return Collections.unmodifiableList(m_ipMatches);
    }

    public List<Range> getRanges() {
        return Collections.unmodifiableList(m_ranges);
    }

    public List<String> getSpecifics() {
        return Collections.unmodifiableList(m_specifics);
    }

    public boolean removeIpMatch(final String ipMatch) {
        return m_ipMatches.remove(ipMatch);
    }

    public boolean removeRange(final Range range) {
        return m_ranges.remove(range);
    }

    public boolean removeSpecific(final String specific) {
        return m_specifics.remove(specific);
    }

    public void setIpMatches(final List<String> ipMatches) {
        m_ipMatches = new ArrayList<String>(ipMatches);
    }

    public void setRanges(final List<Range> ranges) {
        m_ranges = new ArrayList<Range>(ranges);
    }

    public void setSpecifics(final List<String> specifics) {
        m_specifics = new ArrayList<String>(specifics);
    }

}
