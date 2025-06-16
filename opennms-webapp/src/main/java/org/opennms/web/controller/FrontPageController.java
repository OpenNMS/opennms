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

import org.opennms.web.api.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>FrontPageController class.</p>
 *
 * @author ranger
 * @since 1.8.1
 */
@Controller
@RequestMapping("/frontPage.htm")
public class FrontPageController {

    @RequestMapping(method = RequestMethod.GET)
    protected ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.isUserInRole(Authentication.ROLE_DASHBOARD) || "true".equals(System.getProperty("org.opennms.dashboard.redirect", "false").toLowerCase())) {
            return new ModelAndView("redirect:/dashboard.jsp");
        } else {
            return new ModelAndView("redirect:/index.jsp");
        }
    }
}
