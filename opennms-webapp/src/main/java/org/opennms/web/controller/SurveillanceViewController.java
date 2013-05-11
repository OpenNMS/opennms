
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.web.svclayer.ProgressMonitor;
import org.opennms.web.svclayer.SimpleWebTable;
import org.opennms.web.svclayer.SurveillanceService;
import org.opennms.web.svclayer.SurveillanceViewError;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Spring MVC controller servlet for surveillance-view page.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class SurveillanceViewController extends AbstractController implements InitializingBean {
    private static final String VIEW_NAME_PARAMETER = "viewName";

    private static final String PROGRESS_MONITOR_KEY = "surveillanceViewProgressMonitor";

    private SurveillanceService m_service;

    /**
     * <p>setService</p>
     *
     * @param svc a {@link org.opennms.web.svclayer.SurveillanceService} object.
     */
    public void setService(SurveillanceService svc) {
        m_service = svc;
    }
    
    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_service != null, "service property must be set");
    }

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest req,
            HttpServletResponse resp) throws Exception {

        if ( ! m_service.isViewName(req.getParameter(VIEW_NAME_PARAMETER)) ) {
            SurveillanceViewError viewError = createSurveillanceViewError( WebSecurityUtils.sanitizeString(req.getParameter(VIEW_NAME_PARAMETER)) );
            return new ModelAndView("surveillanceViewError", "error", viewError);
        }

        HttpSession session = req.getSession();
        resp.setHeader("Refresh", m_service.getHeaderRefreshSeconds(WebSecurityUtils.sanitizeString(req.getParameter(VIEW_NAME_PARAMETER))));
        ProgressMonitor progressMonitor = (ProgressMonitor) session.getAttribute(PROGRESS_MONITOR_KEY);

        if (progressMonitor == null) {
            progressMonitor = createProgressMonitor(WebSecurityUtils.sanitizeString(req.getParameter(VIEW_NAME_PARAMETER)));
            session.setAttribute(PROGRESS_MONITOR_KEY, progressMonitor);
        }

        if (progressMonitor.isError()) {
            session.removeAttribute(PROGRESS_MONITOR_KEY);
            Throwable t = progressMonitor.getThrowable();
            throw new Exception("SurveillanceView Builder Thread threw exception: ["
                    + t.getClass().getName() + "] "
                    + t.getMessage(), t);
        }

        if (progressMonitor.isFinished()) {
            session.removeAttribute(PROGRESS_MONITOR_KEY);
            SimpleWebTable table = (SimpleWebTable) progressMonitor.getResult();
            ModelAndView modelAndView = new ModelAndView("surveillanceView", "webTable", table);
            modelAndView.addObject("viewNames", m_service.getViewNames());
            return modelAndView;
        }

        return new ModelAndView("progressBar", "progress", progressMonitor);

    }

    private ProgressMonitor createProgressMonitor(final String viewName) {
        ProgressMonitor progressMonitor;
        final ProgressMonitor monitor = new ProgressMonitor();

        Thread bgRunner = new Thread("SurveillanceView Builder") {

            @Override
            public void run() {
                try {
                    m_service.createSurveillanceTable(viewName, monitor);
                } catch (Throwable t) {
                    monitor.errorOccurred(t);
                }
            }

        };
        bgRunner.start();
        progressMonitor = monitor;
        return progressMonitor;
    }

    private SurveillanceViewError createSurveillanceViewError(final String viewName) {
        SurveillanceViewError viewError = new SurveillanceViewError();
        if (viewName == null) {
            viewError.setShortDescr("No default view");
            viewError.setLongDescr("No view name was specified, and no default view exists in the system.");
        } else {
            viewError.setShortDescr("No such view");
            viewError.setLongDescr("The requested view '" + viewName + "' does not exist in the system.");
        }

        return viewError;
    }

}
