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
package org.opennms.web.controller.bsm;

import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceSearchCriteriaBuilder;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class BusinessServicesBoxController extends AbstractController implements InitializingBean {

    public static final int DEFAULT_ROW_COUNT = 10;
    private BusinessServiceManager businessServiceManager;
    private String m_successView;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int numberOfRows = SystemProperties.getInteger("opennms.businessServicesWithProblems.count", DEFAULT_ROW_COUNT);
        boolean all = "true".equalsIgnoreCase(request.getParameter("all"));

        BusinessServiceSearchCriteriaBuilder criteriaBuilder = new BusinessServiceSearchCriteriaBuilder()
                .order(BusinessServiceSearchCriteriaBuilder.Order.Severity)
                .greaterSeverity(Status.NORMAL)
                .desc();

        List<BusinessService> bsList = criteriaBuilder.apply(businessServiceManager, businessServiceManager.getAllBusinessServices());
        boolean more = !all && bsList.size() - numberOfRows > 0;
        if (!all) {
            if (bsList.size() > numberOfRows) {
                bsList = bsList.subList(0, numberOfRows);
            }
        }
        ModelAndView modelAndView = new ModelAndView(m_successView);
        modelAndView.addObject("more", more);
        modelAndView.addObject("services", bsList);
        return modelAndView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        this.businessServiceManager = businessServiceManager;
    }

    @Override
    public void afterPropertiesSet() {
        Objects.requireNonNull(m_successView, "successView must be set");
        Objects.requireNonNull(businessServiceManager, "businessServiceManager must be set");
    }
}
