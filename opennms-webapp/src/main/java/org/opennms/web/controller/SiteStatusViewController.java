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

package org.opennms.web.controller;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.AggregateStatusView;
import org.opennms.web.svclayer.SiteStatusViewService;
import org.opennms.web.svclayer.model.AggregateStatus;
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
