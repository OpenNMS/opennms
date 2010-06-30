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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.web.controller;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.AggregateStatusView;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.svclayer.AggregateStatus;
import org.opennms.web.svclayer.SiteStatusViewService;
import org.opennms.web.svclayer.SiteStatusViewError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * Controller servlet for presenting aggregate (propogated) status of nodes
 * using a list of aggregate status definitions (container object for lists
 * of categories).
 *
 * @author david
 * @version $Id: $
 * @since 1.8.1
 */
public class SiteStatusViewController extends AbstractController {

    private static final int FIVE_MINUTES = 5*60;
    
    private static SiteStatusViewService m_service;

    /**
     * <p>Constructor for SiteStatusViewController.</p>
     */
    public SiteStatusViewController() {
        setSupportedMethods(new String[] {METHOD_GET});
        setCacheSeconds(FIVE_MINUTES);
    }
    
    /**
     * <p>setService</p>
     *
     * @param svc a {@link org.opennms.web.svclayer.SiteStatusViewService} object.
     */
    public void setService(SiteStatusViewService svc) {
        m_service = svc;
    }
    
    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        ModelAndView mav = new ModelAndView("siteStatus");
        String statusView = req.getParameter("statusView");
        String statusSite = req.getParameter("statusSite");
        String nodeId = req.getParameter("nodeid");
        AggregateStatusView view = null;
        try {
            view = m_service.createAggregateStatusView(statusView);
        } catch (ObjectRetrievalFailureException e) {
            SiteStatusViewError viewError = createSiteStatusViewError((String)e.getIdentifier(), e.getMessage());
            return new ModelAndView("siteStatusError", "error", viewError);
        }

        Collection<AggregateStatus> aggrStati;
        
        if (nodeId != null && WebSecurityUtils.safeParseInt(nodeId) > 0) {
            aggrStati = m_service.createAggregateStatusesUsingNodeId(WebSecurityUtils.safeParseInt(nodeId), statusView);
        } else if (statusSite == null) {
            aggrStati = m_service.createAggregateStatuses(view);
        } else {
            aggrStati = m_service.createAggregateStatuses(view, statusSite);
            //Don't persist this, convenience for display only.
            view.setColumnValue(statusSite);
        }
        
        mav.addObject("view", view);
        mav.addObject("stati", aggrStati);
        return mav;
    }

    private SiteStatusViewError createSiteStatusViewError(String shortDescr, String longDescr) {
        SiteStatusViewError viewError = new SiteStatusViewError();
        viewError.setShortDescr(shortDescr);
        viewError.setLongDescr(longDescr);
        return viewError;
    }

}
