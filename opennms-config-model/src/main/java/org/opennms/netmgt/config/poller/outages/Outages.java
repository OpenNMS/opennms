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
