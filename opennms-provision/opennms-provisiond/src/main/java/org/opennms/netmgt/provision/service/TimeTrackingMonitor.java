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

/**
 * <p>TimeTrackingMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class TimeTrackingMonitor implements ProvisionMonitor {

	private WorkDuration m_importDuration = new WorkDuration("Importing");
	private WorkDuration m_auditDuration = new WorkDuration("Auditing");
	private WorkDuration m_loadingDuration = new WorkDuration("Loading");
	private WorkDuration m_processingDuration = new WorkDuration("Processing");
	private WorkDuration m_preprocessingDuration = new WorkDuration("Scanning");
	private WorkDuration m_relateDuration = new WorkDuration("Relating");
	private WorkEffort m_preprocessingEffort = new WorkEffort("Scan Effort");
	private WorkEffort m_processingEffort = new WorkEffort("Write Effort");
	private WorkEffort m_eventEffort = new WorkEffort("Event Sending Effort");
	private int m_deleteCount;
	private int m_insertCount;
	private int m_updateCount;
	private int m_eventCount;

	/** {@inheritDoc} */
	public void beginProcessingOps(int deleteCount, int updateCount, int insertCount) {
	    m_deleteCount = deleteCount;
	    m_updateCount = updateCount;
	    m_insertCount = insertCount;
		m_processingDuration.start();
	}

	/**
	 * <p>finishProcessingOps</p>
	 */
	public void finishProcessingOps() {
		m_processingDuration.end();
	}

	/**
	 * <p>beginPreprocessingOps</p>
	 */
	public void beginPreprocessingOps() {
		m_preprocessingDuration.start();
	}

	/**
	 * <p>finishPreprocessingOps</p>
	 */
	public void finishPreprocessingOps() {
		m_preprocessingDuration.end();
	}

	/** {@inheritDoc} */
	public void beginPreprocessing(ImportOperation oper) {
		if (oper instanceof SaveOrUpdateOperation) {
			m_preprocessingEffort.begin();
		}
	}

	/** {@inheritDoc} */
	public void finishPreprocessing(ImportOperation oper) {
		if (oper instanceof SaveOrUpdateOperation) {
			m_preprocessingEffort.end();
		}
	}

	/** {@inheritDoc} */
	public void beginPersisting(ImportOperation oper) {
		m_processingEffort.begin();
		
	}

	/** {@inheritDoc} */
	public void finishPersisting(ImportOperation oper) {
		m_processingEffort.end();
	}

	/** {@inheritDoc} */
	public void beginSendingEvents(ImportOperation oper, List<Event> events) {
		if (events != null) m_eventCount += events.size();
		m_eventEffort.begin();
	}

	/** {@inheritDoc} */
	public void finishSendingEvents(ImportOperation oper, List<Event> events) {
		m_eventEffort.end();
	}

	/** {@inheritDoc} */
	public void beginLoadingResource(Resource resource) {
		m_loadingDuration.setName("Loading Resource: "+resource);
		m_loadingDuration.start();
	}

	/** {@inheritDoc} */
	public void finishLoadingResource(Resource resource) {
		m_loadingDuration.end();
	}

	/**
	 * <p>beginImporting</p>
	 */
	public void beginImporting() {
		m_importDuration.start();
	}

	/**
	 * <p>finishImporting</p>
	 */
	public void finishImporting() {
		m_importDuration.end();
	}

	/**
	 * <p>beginAuditNodes</p>
	 */
	public void beginAuditNodes() {
		m_auditDuration.start();
	}

	/**
	 * <p>finishAuditNodes</p>
	 */
	public void finishAuditNodes() {
		m_auditDuration.end();
	}
	
	/**
	 * <p>beginRelateNodes</p>
	 */
	public void beginRelateNodes() {
		m_relateDuration.start();
	}

	/**
	 * <p>finishRelateNodes</p>
	 */
	public void finishRelateNodes() {
		m_relateDuration.end();
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
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
