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
package org.opennms.web.admin.users;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.User;
import org.opennms.web.api.Authentication;

/**
 * A servlet that handles saving the user stored in the web user's HTTP session.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @since 1.8.1
 */
public class SaveUserServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -2138716651602916013L;

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession user = request.getSession(false);

        if (user != null) {
            UserManager userFactory = UserFactory.getInstance();
            User newUser = (User) user.getAttribute("user.modifyUser.jsp");
            if (newUser.getRoles().contains(Authentication.ROLE_READONLY) && !request.isUserInRole(Authentication.ROLE_ADMIN)) {
                throw new ServletException("Error: user " + newUser.getUserId() + " is read-only!");
            }

            // now save to the XML file
            try {
                userFactory.saveUser(newUser.getUserId(), newUser);
            } catch (Throwable e) {
                throw new ServletException("Error saving user " + newUser.getUserId(), e);
            }
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/users/list.jsp");
        dispatcher.forward(request, response);
    }
}
