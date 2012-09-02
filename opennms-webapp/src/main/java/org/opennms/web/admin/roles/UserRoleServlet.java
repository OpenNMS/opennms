/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.admin.roles;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.WebRoleContext;
import org.opennms.netmgt.config.WebCalendar;
import org.opennms.netmgt.config.WebRole;
import org.opennms.netmgt.config.WebRoleManager;

 /**
  * Servlet implementation class for Servlet: RoleServlet
  *
  * @author ranger
  * @version $Id: $
  * @since 1.8.1
  */
 public class UserRoleServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
    /**
     * 
     */
    private static final long serialVersionUID = 1078908981395901414L;
    private static final String LIST = "/roles/list.jsp";
    private static final String VIEW = "/roles/view.jsp";
    
    /**
     * <p>Constructor for UserRoleServlet.</p>
     */
    public UserRoleServlet() {
		super();
	}
    
    private interface Action {
        public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException;
    }
    
    private class ListAction implements Action {
        public String execute(HttpServletRequest request, HttpServletResponse response) {
            return LIST;
        }
        
    }
    
    private class ViewAction implements Action {
        
        public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException {
            try {
                WebRole role = (WebRole)request.getAttribute("role");
                if (role == null) {
                    role = getRoleManager().getRole(request.getParameter("role"));
                    request.setAttribute("role", role);
                }
                String dateSpec = request.getParameter("month");
                Date month = (dateSpec == null ? new Date() : new SimpleDateFormat("MM-yyyy").parse(dateSpec));
                WebCalendar calendar = role.getCalendar(month);
                request.setAttribute("calendar", calendar);
                return VIEW;
            } catch (ParseException e) {
                throw new ServletException("Unable to parse date: "+e.getMessage(), e);
            }
        }
        
    }
    
    /**
     * <p>doIt</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param response a {@link javax.servlet.http.HttpServletResponse} object.
     * @throws javax.servlet.ServletException if any.
     * @throws java.io.IOException if any.
     */
    protected void doIt(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String reqUrl = request.getServletPath();
        request.setAttribute("reqUrl", reqUrl);
        Action action = getAction(request, response);
        String display = action.execute(request, response);
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(display);
        dispatcher.forward(request, response);
    }

    private Action getAction(HttpServletRequest request, HttpServletResponse response) {
        String op = request.getParameter("operation");
        if ("view".equals(op))
            return new ViewAction();
        else
            return new ListAction();
    }
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	/** {@inheritDoc} */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doIt(request, response);
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	/** {@inheritDoc} */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doIt(request, response);
	}

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    public void init() throws ServletException {
        super.init();

        try {
            WebRoleContext.init();
            
            getServletContext().setAttribute("roleManager", WebRoleContext.getWebRoleManager());
            getServletContext().setAttribute("userManager", WebRoleContext.getWebUserManager());
            getServletContext().setAttribute("groupManager", WebRoleContext.getWebGroupManager());
        } catch (Throwable e) {
            throw new ServletException("Error initializing RolesServlet", e);
        }
    }

    private WebRoleManager getRoleManager() {
        return WebRoleContext.getWebRoleManager();
    }
}
