/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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
 * <p>LocationMonitorResumeController class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationMonitorResumeController extends AbstractCommandController implements InitializingBean {
    
    private DistributedPollerService m_distributedPollerService;
    private String m_successView;
    private String m_errorView;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LocationMonitorIdCommand cmd = (LocationMonitorIdCommand) command;
        if (!errors.hasErrors()) {
            getDistributedPollerService().resumeLocationMonitor(cmd, errors);
        }
        
        if (errors.hasErrors()) {
            return new ModelAndView(getErrorView(), "errors", errors);
        } else {
            return new ModelAndView(getSuccessView());
        }
    }
    
    /**
     * <p>getDistributedPollerService</p>
     *
     * @return a {@link org.opennms.web.svclayer.DistributedPollerService} object.
     */
    public DistributedPollerService getDistributedPollerService() {
        return m_distributedPollerService;
    }

    /**
     * <p>setDistributedPollerService</p>
     *
     * @param distributedPollerService a {@link org.opennms.web.svclayer.DistributedPollerService} object.
     */
    public void setDistributedPollerService(DistributedPollerService distributedPollerService) {
        m_distributedPollerService = distributedPollerService;
    }

    /**
     * <p>getSuccessView</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSuccessView() {
        return m_successView;
    }

    /**
     * <p>setSuccessView</p>
     *
     * @param successView a {@link java.lang.String} object.
     */
    public void setSuccessView(String successView) {
        m_successView = successView;
    }

    /**
     * <p>getErrorView</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getErrorView() {
        return m_errorView;
    }

    /**
     * <p>setErrorView</p>
     *
     * @param errorView a {@link java.lang.String} object.
     */
    public void setErrorView(String errorView) {
        m_errorView = errorView;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
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
