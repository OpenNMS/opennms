/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.alarm;

import java.util.Date;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.opennms.core.utils.FuzzyDateFormatter;
import org.opennms.netmgt.model.OnmsSeverity;

/**
 * A data structure holding information on all alarms on a single node.
 *
 * @author <A HREF="mailto:agalue@opennms.org">Alejandro Galue</A>
 */
public class AlarmSummary implements Comparable<AlarmSummary> {

    private final int nodeId;
    private final String nodeLabel;
    private final Date minLastEventDate;
    private final OnmsSeverity maxSeverity;
    private final long alarmCount;

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
        final StringBuffer buffer = new StringBuffer();
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
