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
import org.opennms.web.svclayer.AdminApplicationService;
import org.opennms.web.svclayer.support.DefaultAdminApplicationService.EditModel;
import org.opennms.web.svclayer.support.DefaultAdminApplicationService.ServiceEditModel;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * <p>ApplicationController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ApplicationController extends AbstractController {

    private AdminApplicationService m_adminApplicationService;

    private String getNonEmptyParameter(HttpServletRequest request, String parameter) {
    	if (request != null) {
    		String p = request.getParameter(parameter);
    		if (p != null && !p.equals("")) {
    			return p;
    		}
    	}
    	return null;
    }
    
    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String removeApplicationIdString = getNonEmptyParameter(request, "removeApplicationId");
        String newApplicationName = getNonEmptyParameter(request, "newApplicationName");
        String applicationIdString = getNonEmptyParameter(request, "applicationid");
        String editString = getNonEmptyParameter(request, "edit");
        String ifServiceIdString = getNonEmptyParameter(request, "ifserviceid");
        
        if (removeApplicationIdString != null) {
            m_adminApplicationService.removeApplication(removeApplicationIdString);
            return new ModelAndView(new RedirectView("/admin/applications.htm", true));
        }
        
        if (newApplicationName != null) {
            m_adminApplicationService.addNewApplication(newApplicationName);
            
            /*
             * We could be smart and take the user straight to the edit page
             * for this new application, which would be great, however it's
             * not so great if the site has a huge number of available
             * applications and they need to edit application member services
             * from the service pages.  So, we don't do it.
             */
            return new ModelAndView(new RedirectView("/admin/applications.htm", true));
        }

        if (applicationIdString != null && "services".equals(editString)) {
            final String editAction = getNonEmptyParameter(request, "action");
            if (editAction != null) {
                final String[] serviceAdds = request.getParameterValues("serviceAdds");
                final String[] serviceDeletes = request.getParameterValues("serviceDeletes");

                m_adminApplicationService.performEditServices(applicationIdString,
                                                              editAction,
                                                              serviceAdds,
                                                              serviceDeletes);

                final ModelAndView modelAndView = new ModelAndView(new RedirectView("/admin/applications.htm", true));
                modelAndView.addObject("applicationid", applicationIdString);
                modelAndView.addObject("edit", "services");
                return modelAndView;
            }

            final EditModel model = m_adminApplicationService.findApplicationAndAllMonitoredServices(applicationIdString);

            return new ModelAndView("/admin/editApplication",
                                    "model",
                                    model);
        }

        if (applicationIdString != null && "locations".equals(editString)) {
            final String editAction = getNonEmptyParameter(request, "action");
            if (editAction != null) {
                final String[] locationAdds = request.getParameterValues("locationAdds");
                final String[] locationDeletes = request.getParameterValues("locationDeletes");

                m_adminApplicationService.performEditLocations(applicationIdString,
                                                               editAction,
                                                               locationAdds,
                                                               locationDeletes);

                final ModelAndView modelAndView = new ModelAndView(new RedirectView("/admin/applications.htm", true));
                modelAndView.addObject("applicationid", applicationIdString);
                modelAndView.addObject("edit", "locations");
                return modelAndView;
            }

            final EditModel model = m_adminApplicationService.findApplicationAndAllMonitoredServices(applicationIdString);

            return new ModelAndView("/admin/editApplication",
                                    "model",
                                    model);
        }

        if (applicationIdString != null) {
            return new ModelAndView("/admin/showApplication",
                                    "model",
                                    m_adminApplicationService.getApplication(applicationIdString));
        }
        
        if (ifServiceIdString != null && editString != null) {
            String editAction = getNonEmptyParameter(request, "action");
            if (editAction != null) {
                String[] toAdd = request.getParameterValues("toAdd");
                String[] toDelete = request.getParameterValues("toDelete");

                m_adminApplicationService.performServiceEdit(ifServiceIdString,
                                                       editAction,
                                                       toAdd,
                                                       toDelete);
                
                ModelAndView modelAndView = 
                    new ModelAndView(new RedirectView("/admin/applications.htm", true));
                modelAndView.addObject("ifserviceid", ifServiceIdString);
                modelAndView.addObject("edit", "edit");
                return modelAndView;
            }

            ServiceEditModel model =
                m_adminApplicationService.findServiceApplications(ifServiceIdString);

            return new ModelAndView("/admin/editServiceApplications",
                                    "model",
                                    model);
        }

        List<OnmsApplication> sortedApplications
            = m_adminApplicationService.findAllApplications();
        
        return new ModelAndView("/admin/applications",
                                "applications",
                                sortedApplications);
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
    public void setAdminApplicationService(
            AdminApplicationService adminApplicationService) {
        m_adminApplicationService = adminApplicationService;
    }

}
