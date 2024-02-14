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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.web.api.Authentication;
import org.opennms.web.element.ElementUtil;
import org.opennms.web.element.Service;
import org.opennms.web.svclayer.AdminApplicationService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>ServiceApplicationBoxController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ServiceApplicationBoxController extends AbstractController {
    private AdminApplicationService m_adminApplicationService; 

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Service service = ElementUtil.getServiceByParams(request, getServletContext());

        List<OnmsApplication> applications = m_adminApplicationService.findByMonitoredService(service.getId());
        
        ModelAndView modelAndView =
            new ModelAndView("/includes/serviceApplication-box", "applications",
                             applications);
        modelAndView.addObject("service", service);
        if (request.isUserInRole(Authentication.ROLE_ADMIN)) {
            modelAndView.addObject("isAdmin", "true");
        }
        return modelAndView;
    }

    /**
     * <p>getAdminApplicationService</p>
     *
     * @return a {@link org.opennms.web.svclayer.AdminApplicationService} object.
     */
    public AdminApplicationService getAdminApplicationService() {
        return m_adminApplicationService;
    }

    /**
     * <p>setAdminApplicationService</p>
     *
     * @param adminApplicationService a {@link org.opennms.web.svclayer.AdminApplicationService} object.
     */
    public void setAdminApplicationService(AdminApplicationService adminApplicationService) {
        m_adminApplicationService = adminApplicationService;
    }

}
