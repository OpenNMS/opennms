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
/**
 * <p>NoOpProvisionMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.provision.service.operations;

import java.util.List;

import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;
public class NoOpProvisionMonitor implements ProvisionMonitor {
	/** {@inheritDoc} */
	public void beginProcessingOps(int deleteCount, int updateCount, int insertCount) {
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

	/**
	 * <p>beginRelateNodes</p>
	 */
	public void beginRelateNodes() {
	}

	/**
	 * <p>finishRelateNodes</p>
	 */
	public void finishRelateNodes() {
	}

}
