/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 30, 2006
 *
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
/**
 * <p>AdminApplicationService interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
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
    /**
     * <p>getApplication</p>
     *
     * @param applicationIdString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.svclayer.support.DefaultAdminApplicationService.ApplicationAndMemberServices} object.
     */
    public ApplicationAndMemberServices getApplication(String applicationIdString);

    /**
     * <p>findAllMonitoredServices</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsMonitoredService> findAllMonitoredServices();
    
    /**
     * <p>findApplicationAndAllMonitoredServices</p>
     *
     * @param applicationIdString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.svclayer.support.DefaultAdminApplicationService.EditModel} object.
     */
    public EditModel findApplicationAndAllMonitoredServices(String applicationIdString);

    /**
     * <p>performEdit</p>
     *
     * @param editAction a {@link java.lang.String} object.
     * @param editAction2 a {@link java.lang.String} object.
     * @param toAdd an array of {@link java.lang.String} objects.
     * @param toDelete an array of {@link java.lang.String} objects.
     */
    @Transactional(readOnly = false)
    public void performEdit(String editAction, String editAction2, String[] toAdd, String[] toDelete);

    /**
     * <p>addNewApplication</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsApplication} object.
     */
    @Transactional(readOnly = false)
    public OnmsApplication addNewApplication(String name);

    /**
     * <p>findAllApplications</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsApplication> findAllApplications();

    /**
     * <p>removeApplication</p>
     *
     * @param applicationIdString a {@link java.lang.String} object.
     */
    @Transactional(readOnly = false)
    public void removeApplication(String applicationIdString);

    /**
     * <p>findByMonitoredService</p>
     *
     * @param id a int.
     * @return a {@link java.util.List} object.
     */
    public List<OnmsApplication> findByMonitoredService(int id);

    /**
     * <p>performServiceEdit</p>
     *
     * @param ifServiceIdString a {@link java.lang.String} object.
     * @param editAction a {@link java.lang.String} object.
     * @param toAdd an array of {@link java.lang.String} objects.
     * @param toDelete an array of {@link java.lang.String} objects.
     */
    @Transactional(readOnly = false)
    public void performServiceEdit(String ifServiceIdString, String editAction, String[] toAdd, String[] toDelete);

    /**
     * <p>findServiceApplications</p>
     *
     * @param ifServiceIdString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.svclayer.support.DefaultAdminApplicationService.ServiceEditModel} object.
     */
    public ServiceEditModel findServiceApplications(String ifServiceIdString);

}
