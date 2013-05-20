/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;


/**
 * <p>WorkEffort class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WorkEffort {
	
	private String m_name;
	private long m_totalTime;
	private long m_sectionCount;
	private ThreadLocal<WorkDuration> m_pendingSection = new ThreadLocal<WorkDuration>();
	
	/**
	 * <p>Constructor for WorkEffort.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public WorkEffort(String name) {
		m_name = name;
	}

	/**
	 * <p>begin</p>
	 */
	public void begin() {
		WorkDuration pending = new WorkDuration();
		pending.start();
		m_pendingSection.set(pending);
	}

	/**
	 * <p>end</p>
	 */
	public void end() {
		WorkDuration pending = m_pendingSection.get();
		m_sectionCount++;
		m_totalTime += pending.getLength();
	}
	
	/**
	 * <p>getTotalTime</p>
	 *
	 * @return a long.
	 */
	public long getTotalTime() {
		return m_totalTime;
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Total ").append(m_name).append(": ");
		buf.append((double)m_totalTime/(double)1000L).append(" thread-seconds");
		if (m_sectionCount > 0) {
			buf.append(" Avg ").append(m_name).append(": ");
			buf.append((double)m_totalTime/(double)m_sectionCount).append(" ms per node");
		}
		return buf.toString();
	}

}
