/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.outage;

import java.util.Date;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.opennms.core.utils.FuzzyDateFormatter;

/**
 * A data structure holding information on all outages on a single IP address.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @since 1.8.1
 */
public class OutageSummary implements Comparable<OutageSummary> {
    protected final int nodeId;
    protected final String nodeLabel;
    protected final Date timeDown;
    protected final Date timeUp;
    protected final Date timeNow;

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
        final StringBuffer buffer = new StringBuffer();
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
