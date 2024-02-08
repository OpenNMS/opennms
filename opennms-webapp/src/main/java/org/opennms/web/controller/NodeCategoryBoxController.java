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

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.web.api.Authentication;
import org.opennms.web.servlet.MissingParameterException;
import org.opennms.web.svclayer.AdminCategoryService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>NodeCategoryBoxController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NodeCategoryBoxController extends AbstractController {
    private AdminCategoryService m_adminCategoryService; 

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String nodeIdString = request.getParameter("node");

        if (nodeIdString == null) {
            throw new MissingParameterException("node");
        }

        int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);

        List<OnmsCategory> categories = m_adminCategoryService.findByNode(nodeId);
        
        ModelAndView modelAndView =
            new ModelAndView("/includes/nodeCategory-box", "categories",
                             categories);
        if (request.isUserInRole(Authentication.ROLE_ADMIN)) {
            modelAndView.addObject("isAdmin", "true");
        }
        return modelAndView;
    }

    /**
     * <p>getAdminCategoryService</p>
     *
     * @return a {@link org.opennms.web.svclayer.AdminCategoryService} object.
     */
    public AdminCategoryService getAdminCategoryService() {
        return m_adminCategoryService;
    }

    /**
     * <p>setAdminCategoryService</p>
     *
     * @param adminCategoryService a {@link org.opennms.web.svclayer.AdminCategoryService} object.
     */
    public void setAdminCategoryService(AdminCategoryService adminCategoryService) {
        m_adminCategoryService = adminCategoryService;
    }

}
