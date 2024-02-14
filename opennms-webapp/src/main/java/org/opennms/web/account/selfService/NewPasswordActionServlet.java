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
package org.opennms.web.account.selfService;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.User;

/**
 * A servlet that handles changing a user's password
 */
public class NewPasswordActionServlet extends AbstractBasePasswordChangeActionServlet {
    private static final long serialVersionUID = 1L;

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        initUserFactory("NewPasswordActionServlet");

        HttpSession userSession = request.getSession(false);
        UserManager userFactory = UserFactory.getInstance();

        User user = (User) userSession.getAttribute("user.newPassword.jsp");
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");

        readonlyUserCheck(request, user);

        verifyAndChangePassword(userFactory, userSession, user,
            request, response, currentPassword, newPassword,
            "/account/selfService/newPassword.jsp?action=redo",
            "/account/selfService/passwordChanged.jsp",
            false);
    }
}
