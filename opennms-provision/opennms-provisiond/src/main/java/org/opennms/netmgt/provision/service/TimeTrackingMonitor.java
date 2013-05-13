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
        @Override
	public void beginProcessingOps(int deleteCount, int updateCount, int insertCount) {
	    m_deleteCount = deleteCount;
	    m_updateCount = updateCount;
	    m_insertCount = insertCount;
		m_processingDuration.start();
	}

	/**
	 * <p>finishProcessingOps</p>
	 */
        @Override
	public void finishProcessingOps() {
		m_processingDuration.end();
	}

	/**
	 * <p>beginPreprocessingOps</p>
	 */
        @Override
	public void beginPreprocessingOps() {
		m_preprocessingDuration.start();
	}

	/**
	 * <p>finishPreprocessingOps</p>
	 */
        @Override
	public void finishPreprocessingOps() {
		m_preprocessingDuration.end();
	}

	/** {@inheritDoc} */
        @Override
	public void beginPreprocessing(ImportOperation oper) {
		if (oper instanceof SaveOrUpdateOperation) {
			m_preprocessingEffort.begin();
		}
	}

	/** {@inheritDoc} */
        @Override
	public void finishPreprocessing(ImportOperation oper) {
		if (oper instanceof SaveOrUpdateOperation) {
			m_preprocessingEffort.end();
		}
	}

	/** {@inheritDoc} */
        @Override
	public void beginPersisting(ImportOperation oper) {
		m_processingEffort.begin();
		
	}

	/** {@inheritDoc} */
        @Override
	public void finishPersisting(ImportOperation oper) {
		m_processingEffort.end();
	}

	/** {@inheritDoc} */
        @Override
	public void beginSendingEvents(ImportOperation oper, List<Event> events) {
		if (events != null) m_eventCount += events.size();
		m_eventEffort.begin();
	}

	/** {@inheritDoc} */
        @Override
	public void finishSendingEvents(ImportOperation oper, List<Event> events) {
		m_eventEffort.end();
	}

	/** {@inheritDoc} */
        @Override
	public void beginLoadingResource(Resource resource) {
		m_loadingDuration.setName("Loading Resource: "+resource);
		m_loadingDuration.start();
	}

	/** {@inheritDoc} */
        @Override
	public void finishLoadingResource(Resource resource) {
		m_loadingDuration.end();
	}

	/**
	 * <p>beginImporting</p>
	 */
        @Override
	public void beginImporting() {
		m_importDuration.start();
	}

	/**
	 * <p>finishImporting</p>
	 */
        @Override
	public void finishImporting() {
		m_importDuration.end();
	}

	/**
	 * <p>beginAuditNodes</p>
	 */
        @Override
	public void beginAuditNodes() {
		m_auditDuration.start();
	}

	/**
	 * <p>finishAuditNodes</p>
	 */
        @Override
	public void finishAuditNodes() {
		m_auditDuration.end();
	}
	
	/**
	 * <p>beginRelateNodes</p>
	 */
        @Override
	public void beginRelateNodes() {
		m_relateDuration.start();
	}

	/**
	 * <p>finishRelateNodes</p>
	 */
        @Override
	public void finishRelateNodes() {
		m_relateDuration.end();
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		StringBuffer stats = new StringBuffer();
		stats.append("Deletes: ").append(m_deleteCount).append(", ");
		stats.append("Updates: ").append(m_updateCount).append(", ");
		stats.append("Inserts: ").append(m_insertCount).append("\n");
		stats.append(m_importDuration).append(", ");
		stats.append(m_loadingDuration).append(", ");
		stats.append(m_auditDuration).append('\n');
		stats.append(m_preprocessingDuration).append(", ");
		stats.append(m_processingDuration).append(", ");
		stats.append(m_relateDuration).append("\n");
		stats.append(m_preprocessingEffort).append(", ");
		stats.append(m_processingEffort).append(", ");
		stats.append(m_eventEffort);
		if (m_eventCount > 0) {
			stats.append(", Avg ").append((double)m_eventEffort.getTotalTime()/(double)m_eventCount).append(" ms per event");
		}
		
		return stats.toString();
	}

}
