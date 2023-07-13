/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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
        boolean isPasswordComplexityValid = Pattern.compile(this.PASSWORD_REGEX)
            .matcher(password)
            .matches();

        boolean isPasswordWithSameCharacters = Pattern.compile(this.SAME_CHARACTER_REGEX)
            .matcher(password)
            .matches();

        return isPasswordComplexityValid && !isPasswordWithSameCharacters;
    }

    protected void verifyAndChangePassword(UserManager userFactory, HttpSession userSession, User user,
                                           HttpServletRequest request, HttpServletResponse response,
                                           String currentPassword, String newPassword,
                                           String redoUrl) throws IOException, ServletException {
        if (!userFactory.comparePasswords(user.getUserId(), currentPassword)) {
            // passwords don't match, have user redo
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/account/selfService/passwordGate.jsp?action=redo");
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
                RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/account/selfService/passwordChanged.jsp");
                dispatcher.forward(request, response);
            } else {
                throw new ServletException("Error saving user " + user.getUserId() + ":::Password complexity is not correct! Please use at least 12 characters, consisting of 1 special character, 1 upper case letter, 1 lower case letter and 1 number. Identical strings with 6 or more characters in a row are also not allowed.");
            }
        }
    }
}
