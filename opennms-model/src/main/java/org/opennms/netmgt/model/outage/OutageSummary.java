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
package org.opennms.netmgt.model.outage;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.opennms.core.utils.FuzzyDateFormatter;

/**
 * A data structure holding information on all outages on a single IP address.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @since 1.8.1
 */
@XmlRootElement(name="outage-summary")
@XmlAccessorType(XmlAccessType.NONE)
public class OutageSummary implements Comparable<OutageSummary>, Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name="node-id")
    protected int nodeId;

    @XmlAttribute(name="node-label")
    protected String nodeLabel;

    @XmlAttribute(name="time-down")
    protected Date timeDown;

    @XmlAttribute(name="time-up")
    protected Date timeUp;

    @XmlAttribute(name="time-now")
    protected Date timeNow;

    public OutageSummary() {
    }

    /**
     * <p>Constructor for OutageSummary.</p>
     *
     * @param nodeId a int.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param timeDown a {@link java.util.Date} object.
     * @param timeUp a {@link java.util.Date} object.
     * @param timeNow a {@link java.util.Date} object.
     */
    public OutageSummary(final int nodeId, final String nodeLabel, final Date timeDown, final Date timeUp, final Date timeNow) {
        if (timeDown == null) {
            throw new IllegalArgumentException(String.format("timeDown cannot be null.  nodeId=%d, nodeLabel=%s, timeDown=%s, timeUp=%s, timeNow=%s", nodeId, nodeLabel, timeDown, timeUp, timeNow));
        }

        if (nodeLabel == null) {
            this.nodeLabel = String.valueOf(nodeId);
        } else {
            this.nodeLabel = nodeLabel;
        }

        this.nodeId = nodeId;
        this.timeDown = timeDown;
        this.timeUp = timeUp;
        this.timeNow = timeNow;
    }

    /**
     * <p>Constructor for OutageSummary.</p>
     *
     * @param nodeId a int.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param timeDown a {@link java.util.Date} object.
     * @param timeUp a {@link java.util.Date} object.
     */
    public OutageSummary(final int nodeId, final String nodeLabel, final Date timeDown, final Date timeUp) {
        this(nodeId, nodeLabel, timeDown, timeUp, new Date());
    }

    /**
     * <p>Constructor for OutageSummary.</p>
     *
     * @param nodeId a int.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param timeDown a {@link java.util.Date} object.
     */
    public OutageSummary(final int nodeId, final String nodeLabel, final Date timeDown) {
        this(nodeId, nodeLabel, timeDown, null, new Date());
    }

    /**
     * <p>Getter for the field <code>nodeId</code>.</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return (this.nodeId);
    }

    /**
     * <p>getHostname</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHostname() {
        return (this.nodeLabel);
    }

    /**
     * <p>Getter for the field <code>nodeLabel</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return (this.nodeLabel);
    }

    /**
     * <p>Getter for the field <code>timeDown</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getTimeDown() {
        return (this.timeDown);
    }

    /**
     * <p>Getter for the field <code>timeUp</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getTimeUp() {
        return (this.timeUp);
    }

    /**
     * <p>getFuzzyTimeDown</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFuzzyTimeDown() {
        // mmm... I *love* Get Fuzzy!
        return FuzzyDateFormatter.calculateDifference(this.getTimeDown(), new Date());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("<OutageSummary: ");
        buffer.append(this.nodeId);
        buffer.append(":");
        buffer.append(this.nodeLabel);
        buffer.append(", down at ");
        buffer.append(this.timeDown);
        if (this.timeUp != null) {
            buffer.append(", back up at ");
            buffer.append(this.timeUp);
        }
        return (buffer.toString());
    }

    @Override
    public int compareTo(final OutageSummary that) {
        return new CompareToBuilder()
        .append(this.getTimeDown(), that.getTimeDown())
        .append(this.getTimeUp(), that.getTimeUp())
        .append(this.getHostname(), that.getHostname())
        .append(this.getNodeLabel(), that.getNodeLabel())
        .toComparison();
    }

};
