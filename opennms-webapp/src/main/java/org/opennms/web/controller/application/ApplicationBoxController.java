/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.application;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.ApplicationStatus;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsSeverity;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class ApplicationBoxController extends AbstractController implements InitializingBean {

    public static class ApplicationSummary {
        private OnmsApplication application;
        private OnmsSeverity severity;

        public ApplicationSummary(OnmsApplication application, OnmsSeverity severity) {
            this.application = application;
            this.severity = severity;
        }

        public OnmsApplication getApplication() {
            return application;
        }

        public OnmsSeverity getSeverity() {
            return severity;
        }

    }

    public static final int DEFAULT_ROW_COUNT = 10;
    private ApplicationDao applicationDao;
    private String m_successView;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final int numberOfRows = Integer.getInteger("opennms.applicationsWithProblems.count", DEFAULT_ROW_COUNT);
        final boolean all = "true".equalsIgnoreCase(request.getParameter("all"));

        // Get application status, but only consider everything > NORMAL
        List<ApplicationStatus> applicationStatus = applicationDao.getApplicationStatus();
        applicationStatus = applicationStatus.stream().filter(a -> a.getSeverity().isGreaterThan(OnmsSeverity.NORMAL)).collect(Collectors.toList());

        // Calculate status for application
        // Define if there is a "more"
        boolean more = !all && applicationStatus.size() - numberOfRows > 0;
        if (!all) {
            if (applicationStatus.size() > numberOfRows) {
                applicationStatus = applicationStatus.subList(0, numberOfRows);
            }
        }

        // Sort
        applicationStatus.sort((s1, s2) -> -1 * s1.getSeverity().compareTo(s2.getSeverity())); // desc sort

        // Prepare Model
        ModelAndView modelAndView = new ModelAndView(m_successView);
        modelAndView.addObject("more", more);
        modelAndView.addObject("summaries", applicationStatus);
        return modelAndView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }

    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    @Override
    public void afterPropertiesSet() {
        Objects.requireNonNull(m_successView, "successView must be set");
        Objects.requireNonNull(applicationDao, "applicationDao must be set");
    }
}
