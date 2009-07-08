/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.service;


public class WorkEffort {
	
	private String m_name;
	private long m_totalTime;
	private long m_sectionCount;
	private ThreadLocal<WorkDuration> m_pendingSection = new ThreadLocal<WorkDuration>();
	
	public WorkEffort(String name) {
		m_name = name;
	}

	public void begin() {
		WorkDuration pending = new WorkDuration();
		pending.start();
		m_pendingSection.set(pending);
	}

	public void end() {
		WorkDuration pending = m_pendingSection.get();
		m_sectionCount++;
		m_totalTime += pending.getLength();
	}
	
	public long getTotalTime() {
		return m_totalTime;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Total ").append(m_name).append(": ");
		buf.append((double)m_totalTime/(double)1000L).append(" thread-seconds ");
		if (m_sectionCount > 0) {
			buf.append("Avg ").append(m_name).append(": ");
			buf.append((double)m_totalTime/(double)m_sectionCount).append(" ms per node");
		}
		return buf.toString();
	}

}