/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
package org.opennms.web.svclayer;

import java.util.List;

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.web.svclayer.support.DefaultAdminApplicationService.ApplicationAndMemberServices;
import org.opennms.web.svclayer.support.DefaultAdminApplicationService.EditModel;
import org.opennms.web.svclayer.support.DefaultAdminApplicationService.ServiceEditModel;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AdminApplicationService {
    public ApplicationAndMemberServices getApplication(String applicationIdString);

    public List<OnmsMonitoredService> findAllMonitoredServices();
    
    public EditModel findApplicationAndAllMonitoredServices(String applicationIdString);

    @Transactional(readOnly = false)
    public void performEdit(String editAction, String editAction2, String[] toAdd, String[] toDelete);

    @Transactional(readOnly = false)
    public OnmsApplication addNewApplication(String name);

    public List<OnmsApplication> findAllApplications();

    @Transactional(readOnly = false)
    public void removeApplication(String applicationIdString);

    public List<OnmsApplication> findByMonitoredService(int id);

    @Transactional(readOnly = false)
    public void performServiceEdit(String ifServiceIdString, String editAction, String[] toAdd, String[] toDelete);

    public ServiceEditModel findServiceApplications(String ifServiceIdString);

}
