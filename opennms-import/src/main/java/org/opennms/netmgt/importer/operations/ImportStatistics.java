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

package org.opennms.netmgt.importer.operations;

import java.util.List;

import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;

/**
 * <p>ImportStatistics interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface ImportStatistics {

	/**
	 * <p>beginProcessingOps</p>
	 */
	void beginProcessingOps();

	/**
	 * <p>finishProcessingOps</p>
	 */
	void finishProcessingOps();

	/**
	 * <p>beginPreprocessingOps</p>
	 */
	void beginPreprocessingOps();

	/**
	 * <p>finishPreprocessingOps</p>
	 */
	void finishPreprocessingOps();

	/**
	 * <p>beginPreprocessing</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.importer.operations.ImportOperation} object.
	 */
	void beginPreprocessing(ImportOperation oper);

	/**
	 * <p>finishPreprocessing</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.importer.operations.ImportOperation} object.
	 */
	void finishPreprocessing(ImportOperation oper);

	/**
	 * <p>beginPersisting</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.importer.operations.ImportOperation} object.
	 */
	void beginPersisting(ImportOperation oper);

	/**
	 * <p>finishPersisting</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.importer.operations.ImportOperation} object.
	 */
	void finishPersisting(ImportOperation oper);

	/**
	 * <p>beginSendingEvents</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.importer.operations.ImportOperation} object.
	 * @param events a {@link java.util.List} object.
	 */
	void beginSendingEvents(ImportOperation oper, List<Event> events);

	/**
	 * <p>finishSendingEvents</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.importer.operations.ImportOperation} object.
	 * @param events a {@link java.util.List} object.
	 */
	void finishSendingEvents(ImportOperation oper, List<Event> events);

	/**
	 * <p>beginLoadingResource</p>
	 *
	 * @param resource a {@link org.springframework.core.io.Resource} object.
	 */
	void beginLoadingResource(Resource resource);

	/**
	 * <p>finishLoadingResource</p>
	 *
	 * @param resource a {@link org.springframework.core.io.Resource} object.
	 */
	void finishLoadingResource(Resource resource);

	/**
	 * <p>beginImporting</p>
	 */
	void beginImporting();

	/**
	 * <p>finishImporting</p>
	 */
	void finishImporting();

	/**
	 * <p>beginAuditNodes</p>
	 */
	void beginAuditNodes();

	/**
	 * <p>finishAuditNodes</p>
	 */
	void finishAuditNodes();

	/**
	 * <p>setDeleteCount</p>
	 *
	 * @param deleteCount a int.
	 */
	void setDeleteCount(int deleteCount);

	/**
	 * <p>setInsertCount</p>
	 *
	 * @param insertCount a int.
	 */
	void setInsertCount(int insertCount);

	/**
	 * <p>setUpdateCount</p>
	 *
	 * @param updateCount a int.
	 */
	void setUpdateCount(int updateCount);

	/**
	 * <p>beginRelateNodes</p>
	 */
	void beginRelateNodes();

	/**
	 * <p>finishRelateNodes</p>
	 */
	void finishRelateNodes();

}
