package org.opennms.web.admin.roles;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class for Servlet: RoleServlet
 *
 */
 public class UserRoleServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

    private static final String LIST = "/roles/list.jsp";
    private static final String VIEW = "/roles/view.jsp";
    
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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doIt(request, response);
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doIt(request, response);
	}

    public void init() throws ServletException {
        super.init();

        try {
            AppContext.init();
            
            getServletContext().setAttribute("roleManager", AppContext.getWebRoleManager());
            getServletContext().setAttribute("userManager", AppContext.getWebUserManager());
            getServletContext().setAttribute("groupManager", AppContext.getWebGroupManager());
        } catch (Exception e) {
            throw new ServletException("Error initializing RolesServlet", e);
        }
        

    }

    private WebRoleManager getRoleManager() {
        return AppContext.getWebRoleManager();
    }
    
    private WebUserManager getUserManager() {
        return AppContext.getWebUserManager();
    }

    private WebGroupManager getGroupManager() {
        return AppContext.getWebGroupManager();
    }

}