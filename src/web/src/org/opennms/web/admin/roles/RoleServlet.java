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
    private static final String ADD_ENTRY = "/admin/userGroupView/roles/addEntry.jsp";
    
    public RoleServlet() {
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
    
    private class DeleteAction implements Action {
        public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException {
            getRoleManager().deleteRole(request.getParameter("role"));
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
    
    private class AddEntryAction implements Action {
        
        public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException {
            try {
                WebRole role = getRoleManager().getRole(request.getParameter("role"));
                request.setAttribute("role", role);
                Date date = new SimpleDateFormat("MM-dd-yyyy").parse(request.getParameter("date"));
                request.setAttribute("date", date);
                return ADD_ENTRY;
            } catch (ParseException e) {
                throw new ServletException("Unable to parse date: "+e.getMessage(), e);
            }
        }
        
    }
    
    private class SaveEntryAction implements Action {
        
        public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException {
            try {
                WebRole role = getRoleManager().getRole(request.getParameter("role"));
                request.setAttribute("role", role);
                Date startDate = getDateParameters("start", request);
                Date endDate = getDateParameters("end", request);
                request.setAttribute("startDate", startDate);
                request.setAttribute("endDate", endDate);
                return TEST;
            } catch (ParseException e) {
                throw new ServletException("Unable to parse date: "+e.getMessage(), e);
            }
        }

        private Date getDateParameters(String prefix, HttpServletRequest request) throws ParseException {
            StringBuffer buf = new StringBuffer();
            buf.append(request.getParameter(prefix+"Month"));
            buf.append('-');
            buf.append(request.getParameter(prefix+"Date"));
            buf.append('-');
            buf.append(request.getParameter(prefix+"Year"));
            buf.append(' ');
            buf.append(request.getParameter(prefix+"Hour"));
            buf.append(':');
            buf.append(request.getParameter(prefix+"Minute"));
            buf.append(' ');
            buf.append(request.getParameter(prefix+"AmOrPm"));
            return new SimpleDateFormat("M-d-yyyy h:m a").parse(buf.toString());
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
                }
                request.setAttribute("role", role);
                role.setName(request.getParameter("roleName"));
                role.setDefaultUser(getUserManager().getUser(request.getParameter("roleUser")));
                role.setMembershipGroup(getGroupManager().getGroup(request.getParameter("roleGroup")));
                role.setDescription(request.getParameter("roleDescr"));
                getRoleManager().saveRole(role);
            }
            return new ViewAction().execute(request, response);
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
        else if ("addEntry".equals(op))
            return new AddEntryAction();
        else if ("saveEntry".equals(op))
            return new SaveEntryAction();
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