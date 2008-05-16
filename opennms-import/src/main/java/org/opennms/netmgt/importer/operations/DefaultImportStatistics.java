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
 * 
 */
package org.opennms.netmgt.importer.operations;

import java.util.List;

import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;

public class DefaultImportStatistics implements ImportStatistics {
	public void beginProcessingOps() {
	}

	public void finishProcessingOps() {
	}

	public void beginPreprocessingOps() {
	}

	public void finishPreprocessingOps() {
	}

	public void beginPreprocessing(ImportOperation oper) {
	}

	public void finishPreprocessing(ImportOperation oper) {
	}

	public void beginPersisting(ImportOperation oper) {
	}

	public void finishPersisting(ImportOperation oper) {
	}

	public void beginSendingEvents(ImportOperation oper, List<Event> events) {
	}

	public void finishSendingEvents(ImportOperation oper, List<Event> events) {
	}

	public void beginLoadingResource(Resource resource) {
	}

	public void finishLoadingResource(Resource resource) {
	}

	public void beginImporting() {
	}

	public void finishImporting() {
	}

	public void beginAuditNodes() {
	}

	public void finishAuditNodes() {
	}

	public void setDeleteCount(int deleteCount) {
	}

	public void setInsertCount(int insertCount) {
	}

	public void setUpdateCount(int updateCount) {
	}

	public void beginRelateNodes() {
		// TODO Auto-generated method stub
		
	}

	public void finishRelateNodes() {
		// TODO Auto-generated method stub
		
	}

}