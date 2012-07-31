/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.ksc;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.web.servlet.MissingParameterException;
import org.opennms.web.springframework.security.Authentication;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>FormProcMainController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class FormProcMainController extends AbstractController implements InitializingBean {

    public enum Actions {
        View,
        Customize,
        CreateFrom,
        Delete,
        Create
    }

    private KSC_PerformanceReportFactory m_kscReportFactory;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String action = request.getParameter("report_action");

        if (action == null) {
            throw new MissingParameterException("report_action");
        }

        if (Actions.View.toString().equals(action)) {
            ModelAndView modelAndView = new ModelAndView("redirect:/KSC/customView.htm");
            modelAndView.addObject("report", getReportIndex(request));
            modelAndView.addObject("type", "custom");
            return modelAndView;
          
        } else if (( request.isUserInRole( Authentication.ROLE_ADMIN ) || !request.isUserInRole(Authentication.ROLE_READONLY) ) && (request.getRemoteUser() != null)) {
            // Fetch the KscReportEditor or create one if there isn't one already
            KscReportEditor editor = KscReportEditor.getFromSession(request.getSession(), false);

            if (Actions.Customize.toString().equals(action)) {
                editor.loadWorkingReport(getKscReportFactory(), getReportIndex(request));
                return new ModelAndView("redirect:/KSC/customReport.htm");
            } else if (Actions.CreateFrom.toString().equals(action)) {
                editor.loadWorkingReportDuplicate(getKscReportFactory(), getReportIndex(request));
                return new ModelAndView("redirect:/KSC/customReport.htm");
            } else if (Actions.Delete.toString().equals(action)) {
                getKscReportFactory().deleteReportAndSave(getReportIndex(request)); 
                return new ModelAndView("redirect:/KSC/index.htm");
            } else if (Actions.Create.toString().equals(action)) {
                editor.loadNewWorkingReport();
               return new ModelAndView("redirect:/KSC/customReport.htm");
            }
        }
        throw new ServletException ("Invalid Parameter contents for report_action: " + action);
    }

    private static int getReportIndex(HttpServletRequest request) {
        String report = WebSecurityUtils.sanitizeString(request.getParameter("report"));
        if (report == null) {
            throw new MissingParameterException("report");
        } 
        return WebSecurityUtils.safeParseInt(report);
    }

    /**
     * <p>getKscReportFactory</p>
     *
     * @return a {@link org.opennms.netmgt.config.KSC_PerformanceReportFactory} object.
     */
    public KSC_PerformanceReportFactory getKscReportFactory() {
        return m_kscReportFactory;
    }

    /**
     * <p>setKscReportFactory</p>
     *
     * @param kscReportFactory a {@link org.opennms.netmgt.config.KSC_PerformanceReportFactory} object.
     */
    public void setKscReportFactory(KSC_PerformanceReportFactory kscReportFactory) {
        m_kscReportFactory = kscReportFactory;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_kscReportFactory != null, "property kscReportFactory must be set");
    }
}
