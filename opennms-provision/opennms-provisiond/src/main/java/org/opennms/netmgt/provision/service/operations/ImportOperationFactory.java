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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.provision.service.operations;

import org.opennms.netmgt.provision.service.ProvisionService;



/**
 * <p>ImportOperationFactory interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface ImportOperationFactory {

    /**
     * <p>createInsertOperation</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param building a {@link java.lang.String} object.
     * @param city a {@link java.lang.String} object.
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     * @return a {@link org.opennms.netmgt.provision.service.operations.SaveOrUpdateOperation} object.
     */
    public abstract SaveOrUpdateOperation createInsertOperation(String foreignSource, String foreignId, String nodeLabel, String building, String city, ProvisionService provisionService);

    /**
     * <p>createUpdateOperation</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param building a {@link java.lang.String} object.
     * @param city a {@link java.lang.String} object.
     * @param service a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     * @return a {@link org.opennms.netmgt.provision.service.operations.UpdateOperation} object.
     */
    public abstract UpdateOperation createUpdateOperation(Integer nodeId, String foreignSource, String foreignId, String nodeLabel, String building, String city, ProvisionService service);
    
    /**
     * <p>createDeleteOperation</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     * @return a {@link org.opennms.netmgt.provision.service.operations.ImportOperation} object.
     */
    public abstract ImportOperation createDeleteOperation(Integer nodeId, String foreignSource, String foreignId, ProvisionService provisionService);

}
