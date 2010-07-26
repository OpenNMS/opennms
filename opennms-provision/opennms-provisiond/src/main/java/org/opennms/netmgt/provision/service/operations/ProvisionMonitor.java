//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.provision.service.operations;

import java.util.List;

import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;

/**
 * <p>ProvisionMonitor interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface ProvisionMonitor {

	/**
	 * <p>beginProcessingOps</p>
	 *
	 * @param deleteCount a int.
	 * @param updateCount a int.
	 * @param insertCount a int.
	 */
	void beginProcessingOps(int deleteCount, int updateCount, int insertCount);

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
	 * @param oper a {@link org.opennms.netmgt.provision.service.operations.ImportOperation} object.
	 */
	void beginPreprocessing(ImportOperation oper);

	/**
	 * <p>finishPreprocessing</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.provision.service.operations.ImportOperation} object.
	 */
	void finishPreprocessing(ImportOperation oper);

	/**
	 * <p>beginPersisting</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.provision.service.operations.ImportOperation} object.
	 */
	void beginPersisting(ImportOperation oper);

	/**
	 * <p>finishPersisting</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.provision.service.operations.ImportOperation} object.
	 */
	void finishPersisting(ImportOperation oper);

	/**
	 * <p>beginSendingEvents</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.provision.service.operations.ImportOperation} object.
	 * @param events a {@link java.util.List} object.
	 */
	void beginSendingEvents(ImportOperation oper, List<Event> events);

	/**
	 * <p>finishSendingEvents</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.provision.service.operations.ImportOperation} object.
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
	 * <p>beginRelateNodes</p>
	 */
	void beginRelateNodes();

	/**
	 * <p>finishRelateNodes</p>
	 */
	void finishRelateNodes();

}
