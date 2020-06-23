/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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