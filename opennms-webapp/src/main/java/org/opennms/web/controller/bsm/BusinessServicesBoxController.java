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

package org.opennms.web.controller.bsm;

import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        int numberOfRows = Integer.getInteger("opennms.businessServicesWithProblems.count", DEFAULT_ROW_COUNT);
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
