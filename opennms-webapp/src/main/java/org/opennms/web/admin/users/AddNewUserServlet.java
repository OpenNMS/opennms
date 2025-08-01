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
import org.opennms.netmgt.config.users.Password;
import org.opennms.netmgt.config.users.User;

/**
 * A servlet that handles adding a new user
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class AddNewUserServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 9221831285444697701L;

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            UserFactory.init();
        } catch (Throwable e) {
            throw new ServletException("AddNewUserServlet: Error initialising user factory." + e);
        }
        UserManager userFactory = UserFactory.getInstance();

        String userID = request.getParameter("userID");

        if (userID != null && userID.matches(".*[&<>\"`']+.*")) {
            throw new ServletException("User ID must not contain any HTML markup.");
        }

        String password = request.getParameter("pass1");

        boolean hasUser = false;
        try {
            hasUser = userFactory.hasUser(userID);
        } catch (Throwable e) {
            throw new ServletException("can't determine if user " + userID + " already exists in users.xml.", e);
        }

        if (hasUser) {
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/users/newUser.jsp?action=redo");
            dispatcher.forward(request, response);
        } else {
            final Password pass = new Password();
            pass.setEncryptedPassword(UserFactory.getInstance().encryptedPassword(password, true));
            pass.setSalt(true);

            final User newUser = new User();
            newUser.setUserId(userID);
            newUser.setPassword(pass);

            final HttpSession userSession = request.getSession(false);
            userSession.setAttribute("user.modifyUser.jsp", newUser);

            // forward the request for proper display
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/users/modifyUser.jsp");
            dispatcher.forward(request, response);
        }
    }
}
