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
        @Override
	public void beginProcessingOps() {
	}

	/**
	 * <p>finishProcessingOps</p>
	 */
        @Override
	public void finishProcessingOps() {
	}

	/**
	 * <p>beginPreprocessingOps</p>
	 */
        @Override
	public void beginPreprocessingOps() {
	}

	/**
	 * <p>finishPreprocessingOps</p>
	 */
        @Override
	public void finishPreprocessingOps() {
	}

	/** {@inheritDoc} */
        @Override
	public void beginPreprocessing(ImportOperation oper) {
	}

	/** {@inheritDoc} */
        @Override
	public void finishPreprocessing(ImportOperation oper) {
	}

	/** {@inheritDoc} */
        @Override
	public void beginPersisting(ImportOperation oper) {
	}

	/** {@inheritDoc} */
        @Override
	public void finishPersisting(ImportOperation oper) {
	}

	/** {@inheritDoc} */
        @Override
	public void beginSendingEvents(ImportOperation oper, List<Event> events) {
	}

	/** {@inheritDoc} */
        @Override
	public void finishSendingEvents(ImportOperation oper, List<Event> events) {
	}

	/** {@inheritDoc} */
        @Override
	public void beginLoadingResource(Resource resource) {
	}

	/** {@inheritDoc} */
        @Override
	public void finishLoadingResource(Resource resource) {
	}

	/**
	 * <p>beginImporting</p>
	 */
        @Override
	public void beginImporting() {
	}

	/**
	 * <p>finishImporting</p>
	 */
        @Override
	public void finishImporting() {
	}

	/**
	 * <p>beginAuditNodes</p>
	 */
        @Override
	public void beginAuditNodes() {
	}

	/**
	 * <p>finishAuditNodes</p>
	 */
        @Override
	public void finishAuditNodes() {
	}

	/** {@inheritDoc} */
        @Override
	public void setDeleteCount(int deleteCount) {
	}

	/** {@inheritDoc} */
        @Override
	public void setInsertCount(int insertCount) {
	}

	/** {@inheritDoc} */
        @Override
	public void setUpdateCount(int updateCount) {
	}

	/**
	 * <p>beginRelateNodes</p>
	 */
        @Override
	public void beginRelateNodes() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>finishRelateNodes</p>
	 */
        @Override
	public void finishRelateNodes() {
		// TODO Auto-generated method stub
		
	}

}
