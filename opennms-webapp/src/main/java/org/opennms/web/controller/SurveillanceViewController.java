
/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
 * reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included
 * code and modified
 * code that was published under the GNU General Public License. Copyrights
 * for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
 * reserved.
 *
 * Modifications:
 *
 * 2007 Feb 10: Format code (I'm seeing a pattern develop). - dj@opennms.org
 * 2006 Sep 15: Format code. - dj@opennms.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */


package org.opennms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.web.WebSecurityUtils;
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
 * @since 1.6.12
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
