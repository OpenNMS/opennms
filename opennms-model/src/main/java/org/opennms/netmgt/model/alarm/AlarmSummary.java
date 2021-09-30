/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.alarm;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.opennms.core.utils.FuzzyDateFormatter;
import org.opennms.netmgt.model.OnmsSeverity;

/**
 * A data structure holding information on all alarms on a single node.
 *
 * @author <A HREF="mailto:agalue@opennms.org">Alejandro Galue</A>
 */
@XmlRootElement(name="alarm-summary")
@XmlAccessorType(XmlAccessType.NONE)
public class AlarmSummary implements Comparable<AlarmSummary>, Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name="node-id")
    private int nodeId;

    @XmlAttribute(name="node-label")
    private String nodeLabel;

    @XmlAttribute(name="date")
    private Date minLastEventDate;

    @XmlAttribute(name="severity")
    private OnmsSeverity maxSeverity;

    @XmlAttribute(name="count")
    private long alarmCount;

    public AlarmSummary() {
    }

    public AlarmSummary(final Integer nodeId, final String nodeLabel, final Date minLastEventDate, final OnmsSeverity maxSeverity, final Long alarmCount) {
        super();
        this.nodeId = nodeId;
        if (nodeLabel == null) {
            this.nodeLabel = String.valueOf(nodeId);
        } else {
            this.nodeLabel = nodeLabel;
        }
        this.minLastEventDate = minLastEventDate;
        this.maxSeverity = maxSeverity;
        this.alarmCount = alarmCount;
    }

    public int getNodeId() {
        return nodeId;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public Date getMinLastEventDate() {
        return minLastEventDate;
    }

    public long getAlarmCount() {
        return alarmCount;
    }

    public OnmsSeverity getMaxSeverity() {
        return maxSeverity;
    }

    public String getFuzzyTimeDown() {
        return minLastEventDate == null ? "N/A" : FuzzyDateFormatter.calculateDifference(this.minLastEventDate, new Date());
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[AlarmSummary: ");
        buffer.append(this.nodeId);
        buffer.append(":");
        buffer.append(this.nodeLabel);
        buffer.append(" has ");
        buffer.append(this.alarmCount);
        buffer.append(" alarms since ");
        buffer.append(this.minLastEventDate);
        buffer.append("]");
        return (buffer.toString());
    }

    /*
     * The alarm summaries will be ordered by the oldest one first
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final AlarmSummary that) {
        return new CompareToBuilder()
        .append(this.getMinLastEventDate(), that.getMinLastEventDate())
        .append(this.getNodeLabel(), that.getNodeLabel())
        .toComparison();
    }

};
