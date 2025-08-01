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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.User;

/**
 * A servlet that retrieves a user's password in preparation for changing the password
 *
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class NewPasswordEntryServlet extends HttpServlet {
    private static final long serialVersionUID = 8979500859478109256L;


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /** {@inheritDoc} */
    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession userSession = request.getSession(false);
		
		try {
            UserFactory.init();
        } catch (Throwable e) {
            throw new ServletException("NewPasswordEntryServlet: Error initialising user factory." + e);
        }
        UserManager userFactory = UserFactory.getInstance();
        
        if (userSession != null) {
            String userid = request.getRemoteUser();
            try {
                User user = userFactory.getUser(userid);
                userSession.setAttribute("user.newPassword.jsp", user);
            }
            catch (Throwable e) {
                throw new ServletException("Couldn't initialize UserFactory", e);
            }
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/account/selfService/newPassword.jsp");
            dispatcher.forward(request, response);
        }
	}
}
