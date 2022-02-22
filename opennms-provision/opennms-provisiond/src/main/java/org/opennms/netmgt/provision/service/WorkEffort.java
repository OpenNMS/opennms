/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;


/**
 * <p>WorkEffort class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WorkEffort {

    private String name;
    private long totalTime;
    private long sectionCount;
    private ThreadLocal<WorkDuration> pendingSection = new ThreadLocal<>();

    /**
     * <p>Constructor for WorkEffort.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public WorkEffort(String name) {
        this.name = name;
    }

    /**
     * <p>begin</p>
     */
    public void begin() {
        WorkDuration pending = new WorkDuration();
        pending.start();
        pendingSection.set(pending);
    }

    /**
     * <p>end</p>
     */
    public void end() {
        WorkDuration pending = pendingSection.get();
        sectionCount++;
        totalTime += pending.getLength();
    }

    /**
     * <p>getTotalTime</p>
     *
     * @return a long.
     */
    public long getTotalTime() {
        return totalTime;
    }

    public long getSectionCount() {
        return sectionCount;
    }

    public double getAverage() {
        return sectionCount > 0 ? (double) totalTime / (double) sectionCount : 0;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("Total ").append(name).append(": ");
        buf.append((double) totalTime / (double) 1000L).append(" thread-seconds");
        if (sectionCount > 0) {
            buf.append(" Avg ").append(name).append(": ");
            buf.append(getAverage() / (double) 1000L).append(" ms per node");
        }
        return buf.toString();
    }
}