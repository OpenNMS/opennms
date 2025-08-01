/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
