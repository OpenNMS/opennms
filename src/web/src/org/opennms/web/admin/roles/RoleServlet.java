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
 public class RoleServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
    private static final String TEST = "/admin/userGroupView/test.jsp";
    private static final String LIST = "/admin/userGroupView/roles/list.jsp";
    private static final String VIEW = "/admin/userGroupView/roles/view.jsp";
    private static final String EDIT_DETAILS = "/admin/userGroupView/roles/editDetails.jsp";
    private static final String EDIT_SCHED = "/admin/userGroupView/roles/editSchedule.jsp";
    
    public RoleServlet() {
		super();
	}
    
    private interface Action {
        public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException;
    }
    
    private class ListAction implements Action {
        public String execute(HttpServletRequest request, HttpServletResponse response) {
            request.setAttribute("roleList", getRoleManager().getRoles());
            return LIST;
        }
        
    }
    
    private class DeleteAction implements Action {
        public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException {
            getRoleManager().delete(request.getParameter("role"));
            Action list = new ListAction();
            return list.execute(request, response);
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
    
    private class EditDetailsAction implements Action {
        
        public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException {
            WebRole role = getRoleManager().getRole(request.getParameter("role"));
            request.setAttribute("role", role);
            return EDIT_DETAILS;
        }
        
    }
    
    private class NewAction implements Action {
        
        public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException {
            WebRole role = new WebRole();
            role.setName("NewRole");
            request.setAttribute("role", role);
            return EDIT_DETAILS;
        }
        
    }
    
    private class SaveDetailsAction implements Action {
        
        public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException {
            if (request.getParameter("save") != null) {
                String roleName = request.getParameter("role");
                WebRole role = getRoleManager().getRole(roleName);
                if (role == null) {
                    // this is a new role so create a new on and add it to the roleManager
                    role = new WebRole();
                    getRoleManager().addRole(role);
                }
                request.setAttribute("role", role);
                role.setName(request.getParameter("roleName"));
                role.setDefaultUser(request.getParameter("roleUser"));
                role.setMembershipGroup(request.getParameter("roleGroup"));
                role.setDescription(request.getParameter("roleDescr"));
                getRoleManager().save();
            }
            return new ViewAction().execute(request, response);
        }
        
    }
    
    private class EditScheduleAction implements Action {
        
        public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException {
            try {
                WebRole role = getRoleManager().getRole(request.getParameter("role"));
                request.setAttribute("role", role);
                String dateSpec = request.getParameter("month");
                Date month = (dateSpec == null ? new Date() : new SimpleDateFormat("MM-yyyy").parse(dateSpec));
                WebCalendar calendar = role.getCalendar(month);
                request.setAttribute("calendar", calendar);
                return EDIT_SCHED;
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
        if ("delete".equals(op))
            return new DeleteAction();
        else if ("view".equals(op))
            return new ViewAction();
        else if ("new".equals(op))
            return new NewAction();
        else if ("editDetails".equals(op))
            return new EditDetailsAction();
        else if ("saveDetails".equals(op))
            return new SaveDetailsAction();
        else if ("editSchedule".equals(op))
            return new EditScheduleAction();
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

        getServletContext().setAttribute("roleManager", new WebRoleManager());
        getServletContext().setAttribute("userManager", new WebUserManager());
        getServletContext().setAttribute("groupManager", new WebGroupManager());
        

    }

    private WebRoleManager getRoleManager() {
        return (WebRoleManager)getServletContext().getAttribute("roleManager");
    }
    
    
}