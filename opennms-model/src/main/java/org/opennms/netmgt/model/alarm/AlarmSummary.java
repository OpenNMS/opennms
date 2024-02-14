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
