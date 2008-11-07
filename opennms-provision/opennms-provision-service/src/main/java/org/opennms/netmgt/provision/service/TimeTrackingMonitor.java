/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service;

import java.util.List;

import org.opennms.netmgt.provision.service.operations.ImportOperation;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.opennms.netmgt.provision.service.operations.SaveOrUpdateOperation;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;

public class TimeTrackingMonitor implements ProvisionMonitor {

	private Duration m_importDuration = new Duration("Importing");
	private Duration m_auditDuration = new Duration("Auditing");
	private Duration m_loadingDuration = new Duration("Loading");
	private Duration m_processingDuration = new Duration("Processing");
	private Duration m_preprocessingDuration = new Duration("Scanning");
	private Duration m_relateDuration = new Duration("Relating");
	private WorkEffort m_preprocessingEffort = new WorkEffort("Scan Effort");
	private WorkEffort m_processingEffort = new WorkEffort("Write Effort");
	private WorkEffort m_eventEffort = new WorkEffort("Event Sending Effort");
	private int m_deleteCount;
	private int m_insertCount;
	private int m_updateCount;
	private int m_eventCount;

	public void beginProcessingOps() {
		m_processingDuration.start();
	}

	public void finishProcessingOps() {
		m_processingDuration.end();
	}

	public void beginPreprocessingOps() {
		m_preprocessingDuration.start();
	}

	public void finishPreprocessingOps() {
		m_preprocessingDuration.end();
	}

	public void beginPreprocessing(ImportOperation oper) {
		if (oper instanceof SaveOrUpdateOperation) {
			m_preprocessingEffort.begin();
		}
	}

	public void finishPreprocessing(ImportOperation oper) {
		if (oper instanceof SaveOrUpdateOperation) {
			m_preprocessingEffort.end();
		}
	}

	public void beginPersisting(ImportOperation oper) {
		m_processingEffort.begin();
		
	}

	public void finishPersisting(ImportOperation oper) {
		m_processingEffort.end();
	}

	public void beginSendingEvents(ImportOperation oper, List<Event> events) {
		if (events != null) m_eventCount += events.size();
		m_eventEffort.begin();
	}

	public void finishSendingEvents(ImportOperation oper, List<Event> events) {
		m_eventEffort.end();
	}

	public void beginLoadingResource(Resource resource) {
		m_loadingDuration.setName("Loading Resource: "+resource);
		m_loadingDuration.start();
	}

	public void finishLoadingResource(Resource resource) {
		m_loadingDuration.end();
	}

	public void beginImporting() {
		m_importDuration.start();
	}

	public void finishImporting() {
		m_importDuration.end();
	}

	public void beginAuditNodes() {
		m_auditDuration.start();
	}

	public void finishAuditNodes() {
		m_auditDuration.end();
	}
	
	public void setDeleteCount(int deleteCount) {
		m_deleteCount = deleteCount;
	}

	public void setInsertCount(int insertCount) {
		m_insertCount = insertCount;
	}

	public void setUpdateCount(int updateCount) {
		m_updateCount = updateCount;
	}

	public void beginRelateNodes() {
		m_relateDuration.start();
	}

	public void finishRelateNodes() {
		m_relateDuration.end();
	}
	
	public String toString() {
		StringBuffer stats = new StringBuffer();
		stats.append("Deletes: ").append(m_deleteCount).append(' ');
		stats.append("Updates: ").append(m_updateCount).append(' ');
		stats.append("Inserts: ").append(m_insertCount).append('\n');
		stats.append(m_importDuration).append(' ');
		stats.append(m_loadingDuration).append(' ');
		stats.append(m_auditDuration).append('\n');
		stats.append(m_preprocessingDuration).append(' ');
		stats.append(m_processingDuration).append(' ');
		stats.append(m_relateDuration).append(' ');
		stats.append(m_preprocessingEffort).append(' ');
		stats.append(m_processingEffort).append(' ');
		stats.append(m_eventEffort).append(' ');
		if (m_eventCount > 0) {
			stats.append("Avg ").append((double)m_eventEffort.getTotalTime()/(double)m_eventCount).append(" ms per event");
		}
		
		return stats.toString();
	}

}