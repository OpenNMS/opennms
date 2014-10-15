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

package org.opennms.web.controller.distributed;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.svclayer.DistributedPollerService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>LocationMonitorListController class.</p>
 *
 * @author brozow
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationMonitorPauseAllController extends AbstractController implements InitializingBean {
    private DistributedPollerService m_distributedPollerService;
    private String m_successView;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        m_distributedPollerService.pauseAllLocationMonitors();
        return new ModelAndView(getSuccessView());

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
    public void setDistributedPollerService(
            DistributedPollerService distributedPollerService) {
        m_distributedPollerService = distributedPollerService;
    }

    /**
     * @param successView the successView to set
     */
    public void setSuccessView(String successView) {
        m_successView = successView;
    }

    /**
     * @return the successView
     */
    public String getSuccessView() {
        return m_successView;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (m_distributedPollerService == null) {
            throw new IllegalStateException("distributedPollerService property has not been set");
        }
        if (m_successView == null) {
            throw new IllegalStateException("successView property has not been set");
        }
    }

}
