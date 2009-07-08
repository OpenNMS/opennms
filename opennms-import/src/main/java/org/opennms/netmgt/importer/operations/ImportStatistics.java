/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.importer.operations;

import java.util.List;

import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;

public interface ImportStatistics {

	void beginProcessingOps();

	void finishProcessingOps();

	void beginPreprocessingOps();

	void finishPreprocessingOps();

	void beginPreprocessing(ImportOperation oper);

	void finishPreprocessing(ImportOperation oper);

	void beginPersisting(ImportOperation oper);

	void finishPersisting(ImportOperation oper);

	void beginSendingEvents(ImportOperation oper, List<Event> events);

	void finishSendingEvents(ImportOperation oper, List<Event> events);

	void beginLoadingResource(Resource resource);

	void finishLoadingResource(Resource resource);

	void beginImporting();

	void finishImporting();

	void beginAuditNodes();

	void finishAuditNodes();

	void setDeleteCount(int deleteCount);

	void setInsertCount(int insertCount);

	void setUpdateCount(int updateCount);

	void beginRelateNodes();

	void finishRelateNodes();

}
