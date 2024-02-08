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
package org.opennms.web.svclayer.model;

import java.text.DecimalFormat;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.model.OnmsNode;

@XmlRootElement(name="rtc-node")
@XmlAccessorType(XmlAccessType.NONE)
public class RtcNode {
    private static final DecimalFormat AVAILABILITY_FORMAT = new DecimalFormat("0.000%");

    static {
        AVAILABILITY_FORMAT.setMultiplier(100);
    }

    @XmlElement(name="node")
    private OnmsNode m_node;

    @XmlAttribute(name="service-count")
    private int m_serviceCount;

    @XmlAttribute(name="down-service-count")
    private int m_downServiceCount;

    @XmlAttribute(name="availability")
    private double m_availability;

    public RtcNode(OnmsNode node, int serviceCount, int downServiceCount, double availability) {
        m_node = node;
        m_serviceCount = serviceCount;
        m_downServiceCount = downServiceCount;
        m_availability = availability;
    }

    public double getAvailability() {
        return m_availability;
    }

    public String getAvailabilityAsString() {
        return AVAILABILITY_FORMAT.format(m_availability);
    }

    public int getDownServiceCount() {
        return m_downServiceCount;
    }

    public OnmsNode getNode() {
        return m_node;
    }

    public int getServiceCount() {
        return m_serviceCount;
    }

    @Override
    public String toString() {
        return m_node.getLabel() + ": " + m_downServiceCount + " of " + m_serviceCount + ": " + getAvailabilityAsString();
    }
}