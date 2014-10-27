/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer;

import org.opennms.web.command.LocationMonitorIdCommand;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;

/**
 * <p>DistributedPollerService interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Transactional(readOnly = true)
public interface DistributedPollerService {
    /**
     * <p>getLocationMonitorList</p>
     *
     * @return a {@link org.opennms.web.svclayer.LocationMonitorListModel} object.
     */
    public LocationMonitorListModel getLocationMonitorList();

    /**
     * <p>getLocationMonitorDetails</p>
     *
     * @param command a {@link org.opennms.web.command.LocationMonitorIdCommand} object.
     * @param errors a {@link org.springframework.validation.BindException} object.
     * @return a {@link org.opennms.web.svclayer.LocationMonitorListModel} object.
     */
    public LocationMonitorListModel getLocationMonitorDetails(LocationMonitorIdCommand command, BindException errors);

    /**
     * <p>pauseLocationMonitor</p>
     *
     * @param command a {@link org.opennms.web.command.LocationMonitorIdCommand} object.
     * @param errors a {@link org.springframework.validation.BindException} object.
     */
    @Transactional(readOnly = false)
    public void pauseLocationMonitor(LocationMonitorIdCommand command, BindException errors);
    
    /**
     * <p>resumeLocationMonitor</p>
     *
     * @param command a {@link org.opennms.web.command.LocationMonitorIdCommand} object.
     * @param errors a {@link org.springframework.validation.BindException} object.
     */
    @Transactional(readOnly = false)
    public void resumeLocationMonitor(LocationMonitorIdCommand command, BindException errors);
    
    /**
     * <p>deleteLocationMonitor</p>
     *
     * @param command a {@link org.opennms.web.command.LocationMonitorIdCommand} object.
     * @param errors a {@link org.springframework.validation.BindException} object.
     */
    @Transactional(readOnly = false)
    public void deleteLocationMonitor(LocationMonitorIdCommand command, BindException errors);

    /**
     * Pause all the locations monitors
     */
    @Transactional(readOnly = false)
    public void pauseAllLocationMonitors();

    /**
     * Resume all the locations monitors (those that have been stopped are not resumed)
     */
    @Transactional(readOnly = false)
    public void resumeAllLocationMonitors();
}
