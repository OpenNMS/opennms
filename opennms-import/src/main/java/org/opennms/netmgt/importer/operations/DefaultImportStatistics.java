/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

/**
 * <p>DefaultImportStatistics class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.importer.operations;

import java.util.List;

import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;
public class DefaultImportStatistics implements ImportStatistics {
	/**
	 * <p>beginProcessingOps</p>
	 */
	public void beginProcessingOps() {
	}

	/**
	 * <p>finishProcessingOps</p>
	 */
	public void finishProcessingOps() {
	}

	/**
	 * <p>beginPreprocessingOps</p>
	 */
	public void beginPreprocessingOps() {
	}

	/**
	 * <p>finishPreprocessingOps</p>
	 */
	public void finishPreprocessingOps() {
	}

	/** {@inheritDoc} */
	public void beginPreprocessing(ImportOperation oper) {
	}

	/** {@inheritDoc} */
	public void finishPreprocessing(ImportOperation oper) {
	}

	/** {@inheritDoc} */
	public void beginPersisting(ImportOperation oper) {
	}

	/** {@inheritDoc} */
	public void finishPersisting(ImportOperation oper) {
	}

	/** {@inheritDoc} */
	public void beginSendingEvents(ImportOperation oper, List<Event> events) {
	}

	/** {@inheritDoc} */
	public void finishSendingEvents(ImportOperation oper, List<Event> events) {
	}

	/** {@inheritDoc} */
	public void beginLoadingResource(Resource resource) {
	}

	/** {@inheritDoc} */
	public void finishLoadingResource(Resource resource) {
	}

	/**
	 * <p>beginImporting</p>
	 */
	public void beginImporting() {
	}

	/**
	 * <p>finishImporting</p>
	 */
	public void finishImporting() {
	}

	/**
	 * <p>beginAuditNodes</p>
	 */
	public void beginAuditNodes() {
	}

	/**
	 * <p>finishAuditNodes</p>
	 */
	public void finishAuditNodes() {
	}

	/** {@inheritDoc} */
	public void setDeleteCount(int deleteCount) {
	}

	/** {@inheritDoc} */
	public void setInsertCount(int insertCount) {
	}

	/** {@inheritDoc} */
	public void setUpdateCount(int updateCount) {
	}

	/**
	 * <p>beginRelateNodes</p>
	 */
	public void beginRelateNodes() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>finishRelateNodes</p>
	 */
	public void finishRelateNodes() {
		// TODO Auto-generated method stub
		
	}

}
