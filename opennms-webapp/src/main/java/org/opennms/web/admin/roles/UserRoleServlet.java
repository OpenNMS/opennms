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
    
    private static class ListAction implements Action {
        @Override
        public String execute(HttpServletRequest request, HttpServletResponse response) {
            return LIST;
        }
        
    }
    
    private class ViewAction implements Action {
        
        @Override
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
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doIt(request, response);
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	/** {@inheritDoc} */
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doIt(request, response);
	}

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    @Override
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
