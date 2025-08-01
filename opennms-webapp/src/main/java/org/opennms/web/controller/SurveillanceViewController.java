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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.web.svclayer.SurveillanceService;
import org.opennms.web.svclayer.model.ProgressMonitor;
import org.opennms.web.svclayer.model.SimpleWebTable;
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
        resp.setHeader("Refresh", String.valueOf(m_service.getHeaderRefreshSeconds(WebSecurityUtils.sanitizeString(req.getParameter(VIEW_NAME_PARAMETER)))));
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
