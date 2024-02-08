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
import java.util.regex.Pattern;
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
import org.opennms.web.api.Authentication;

/**
 * Base servlet class for changing a user's password.
 */
@SuppressWarnings("java:S2068")
public abstract class AbstractBasePasswordChangeActionServlet extends HttpServlet {
    public static final String PASSWORD_REGEX = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%&.*+-]).{12,128})";
    public static final String SAME_CHARACTER_REGEX = "(.)\\1{5}";

    protected void initUserFactory(String servletName) throws ServletException {
        try {
            UserFactory.init();
        } catch (Throwable e) {
            throw new ServletException(servletName + ": Error initializing user factory." + e);
        }
    }

    protected void readonlyUserCheck(HttpServletRequest request, User user) throws ServletException {
        if (!request.isUserInRole(Authentication.ROLE_ADMIN) && user.getRoles().contains(Authentication.ROLE_READONLY)) {
            throw new ServletException("User " + user.getUserId() + " is read-only");
        }
    }

    protected boolean validatePassword(final String password) {
        boolean isPasswordComplexityValid = Pattern.compile(PASSWORD_REGEX)
            .matcher(password)
            .matches();

        boolean isPasswordWithSameCharacters = Pattern.compile(SAME_CHARACTER_REGEX)
            .matcher(password)
            .matches();

        return isPasswordComplexityValid && !isPasswordWithSameCharacters;
    }

    protected void verifyAndChangePassword(UserManager userFactory, HttpSession userSession, User user,
                                           HttpServletRequest request, HttpServletResponse response,
                                           String currentPassword, String newPassword,
                                           String redoUrl,
                                           String nextUrl,
                                           boolean redirectAfterSuccess) throws IOException, ServletException {
        if (!userFactory.comparePasswords(user.getUserId(), currentPassword)) {
            // passwords don't match, have user redo
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(redoUrl);
            dispatcher.forward(request, response);
        } else {
            if (this.validatePassword(newPassword)) {
                final Password pass = new Password();
                pass.setEncryptedPassword(userFactory.encryptedPassword(newPassword, true));
                pass.setSalt(true);
                user.setPassword(pass);

                userSession.setAttribute("user.newPassword.jsp", user);

                try {
                    userFactory.saveUser(user.getUserId(), user);
                } catch (Throwable e) {
                    throw new ServletException("Error saving user " + user.getUserId(), e);
                }

                // forward the request for proper display
                if (redirectAfterSuccess) {
                    response.sendRedirect(nextUrl);
                } else {
                    RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(nextUrl);
                    dispatcher.forward(request, response);
                }
            } else {
                throw new ServletException("Error saving user " + user.getUserId() + ":::Password complexity is not correct! Please use at least 12 characters, consisting of 1 special character, 1 upper case letter, 1 lower case letter and 1 number. Identical strings with 6 or more characters in a row are also not allowed.");
            }
        }
    }
}
