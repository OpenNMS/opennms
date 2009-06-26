/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 14, 2007
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
package org.opennms.web.controller.distributed;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.command.LocationMonitorIdCommand;
import org.opennms.web.svclayer.DistributedPollerService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class LocationMonitorDeleteController extends AbstractCommandController implements InitializingBean {
    
    private DistributedPollerService m_distributedPollerService;
    private String m_successView;
    private String m_errorView;

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LocationMonitorIdCommand cmd = (LocationMonitorIdCommand) command;
        if (!errors.hasErrors()) {
            getDistributedPollerService().deleteLocationMonitor(cmd, errors);
        }
        
        if (errors.hasErrors()) {
            return new ModelAndView(getErrorView(), "errors", errors);
        } else {
            return new ModelAndView(getSuccessView());
        }
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

    public String getErrorView() {
        return m_errorView;
    }

    public void setErrorView(String errorView) {
        m_errorView = errorView;
    }

    public void afterPropertiesSet() throws Exception {
        if (m_distributedPollerService == null) {
            throw new IllegalStateException("distributedPollerService property cannot be null");
        }
        
        if (m_successView == null) {
            throw new IllegalStateException("successView property cannot be null");
        }
        
        if (m_errorView == null) {
            throw new IllegalStateException("errorView property cannot be null");
        }
    }

}
