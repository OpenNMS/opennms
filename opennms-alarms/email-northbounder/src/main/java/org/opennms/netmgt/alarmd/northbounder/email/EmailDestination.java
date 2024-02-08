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
package org.opennms.netmgt.alarmd.northbounder.email;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.alarmd.api.Destination;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;

/**
 * Configuration for the various Email hosts to receive alarms via Email.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name = "email-destination")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmailDestination implements Destination {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The name. */
    @XmlElement(name = "name", required = true)
    private String m_name;

    /** The filters. */
    @XmlElement(name = "filter", required = false)
    private List<EmailFilter> m_filters = new ArrayList<>();

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Destination#isFirstOccurrenceOnly()
     */
    @Override
    public boolean isFirstOccurrenceOnly() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Destination#getName()
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.m_name = name;
    }

    /**
     * Gets the filters.
     *
     * @return the filters
     */
    public List<EmailFilter> getFilters() {
        return m_filters;
    }

    /**
     * Sets the filters.
     *
     * @param filters the new filters
     */
    public void setFilters(List<EmailFilter> filters) {
        this.m_filters = filters;
    }

    /**
     * Accepts.
     * <p>If the destination doesn't have filter, the method will return true.</p>
     * <p>If the method has filters, they will be evaluated. If no filters are satisfied, the method will return false.
     * Otherwise, the method will return true as soon as one filter is satisfied.</p>
     *
     * @param alarm the alarm
     * @return true, if successful
     */
    public boolean accepts(NorthboundAlarm alarm) {
        if (m_filters != null && m_filters.isEmpty() == false) {
            for (EmailFilter filter : m_filters) {
                if (filter.accepts(alarm)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

}
