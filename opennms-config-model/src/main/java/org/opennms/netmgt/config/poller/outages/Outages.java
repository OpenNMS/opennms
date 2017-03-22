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

package org.opennms.netmgt.config.poller.outages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;


/**
 * Top-level element for the poll-outages.xml configuration file.
 */

@XmlRootElement(name="outages", namespace="http://xmlns.opennms.org/xsd/config/poller/outages")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("poll-outages.xsd")
public class Outages implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * A scheduled outage
     */
    private Map<String, Outage> m_outages = new LinkedHashMap<String,Outage>();

    public Outages() {
    }

    @XmlElement(name="outage")
    public List<Outage> getOutages() {
        return new ArrayList<Outage>(m_outages.values());
    }

    public void setOutages(final List<Outage> outages) {
        final Map<String, Outage> m = new LinkedHashMap<String, Outage>();
        for(final Outage o : outages) {
            m.put(o.getName(), o);
        }
        m_outages = m;
    }

    public Outage getOutage(final String name) {
        return m_outages.get(name);
    }

    public void addOutage(final Outage outage) {
        m_outages.put(outage.getName(), outage);
    }

    public boolean removeOutage(final Outage outage) {
        final Outage removed = m_outages.remove(outage.getName());
        return removed != null;
    }

    public void removeOutage(final String outageName) {
        m_outages.remove(outageName);
    }

    public boolean replaceOutage(final Outage oldOutage, final Outage newOutage) {
        String match = null;

        for (final Map.Entry<String,Outage> entry : m_outages.entrySet()) {
            if (entry.getValue().equals(oldOutage)) {
                match = entry.getKey();
                break;
            }
        }

        if (match != null) {
            m_outages.put(match, newOutage);
            return true;
        }

        return false;
    }

    public int hashCode() {
        return Objects.hash(m_outages);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;

        if (obj instanceof Outages) {
            final Outages that = (Outages)obj;
            return Objects.equals(this.m_outages, that.m_outages);
        }
        return false;
    }

}
