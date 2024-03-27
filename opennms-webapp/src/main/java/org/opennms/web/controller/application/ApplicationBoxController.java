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
package org.opennms.web.controller.application;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.sysprops.SystemProperties;
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
        final int numberOfRows = SystemProperties.getInteger("opennms.applicationsWithProblems.count", DEFAULT_ROW_COUNT);
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
