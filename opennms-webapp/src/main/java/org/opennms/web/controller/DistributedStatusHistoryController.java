//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.WebSecurityUtils;
import org.opennms.web.svclayer.DistributedStatusService;
import org.opennms.web.svclayer.support.DistributedStatusHistoryModel;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>DistributedStatusHistoryController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class DistributedStatusHistoryController extends AbstractController {
    private DistributedStatusService m_distributedStatusService;

    /**
     * <p>getDistributedStatusService</p>
     *
     * @return a {@link org.opennms.web.svclayer.DistributedStatusService} object.
     */
    public DistributedStatusService getDistributedStatusService() {
        return m_distributedStatusService;
    }

    /**
     * <p>setDistributedStatusService</p>
     *
     * @param statusService a {@link org.opennms.web.svclayer.DistributedStatusService} object.
     */
    public void setDistributedStatusService(DistributedStatusService statusService) {
        m_distributedStatusService = statusService;
    }

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String locationName = WebSecurityUtils.sanitizeString(request.getParameter("location"));
        String monitorId = WebSecurityUtils.sanitizeString(request.getParameter("monitorId"));
        String applicationName = WebSecurityUtils.sanitizeString(request.getParameter("application"));
        String timeSpan = WebSecurityUtils.sanitizeString(request.getParameter("timeSpan"));
        String previousLocation = WebSecurityUtils.sanitizeString(request.getParameter("previousLocation"));
        DistributedStatusHistoryModel model =
            m_distributedStatusService.createHistoryModel(locationName,
                                                          monitorId,
                                                          applicationName,
                                                          timeSpan,
                                                          previousLocation);
        return new ModelAndView("distributedStatusHistory", "historyModel", model);
    }
}
