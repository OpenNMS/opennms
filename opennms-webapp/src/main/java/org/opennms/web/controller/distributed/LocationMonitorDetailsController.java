/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.web.controller.distributed;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.command.LocationMonitorIdCommand;
import org.opennms.web.springframework.security.Authentication;
import org.opennms.web.svclayer.DistributedPollerService;
import org.opennms.web.svclayer.LocationMonitorListModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class LocationMonitorDetailsController extends AbstractCommandController implements InitializingBean {
    
    private DistributedPollerService m_distributedPollerService;
    private String m_successView;

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LocationMonitorIdCommand cmd = (LocationMonitorIdCommand) command;
        LocationMonitorListModel model = getDistributedPollerService().getLocationMonitorDetails(cmd, errors);
        ModelAndView modelAndView = new ModelAndView(getSuccessView(), "model", model);
        
        if (request.isUserInRole(Authentication.ADMIN_ROLE)) {
            modelAndView.addObject("isAdmin", "true");
        }
        
        return modelAndView;

    }
    
    public DistributedPollerService getDistributedPollerService() {
        return m_distributedPollerService;
    }

    public void setDistributedPollerService(DistributedPollerService distributedPollerService) {
        m_distributedPollerService = distributedPollerService;
    }

    public String getSuccessView() {
        return m_successView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }

    public void afterPropertiesSet() throws Exception {
        if (m_distributedPollerService == null) {
            throw new IllegalStateException("distributedPollerService property cannot be null");
        }
        
        if (m_successView == null) {
            throw new IllegalStateException("successView property cannot be null");
        }
    }
}
