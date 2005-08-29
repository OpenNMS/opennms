package org.opennms.web.admin.roles;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.BasicScheduleUtils;
import org.opennms.netmgt.config.common.Time;
import org.opennms.netmgt.config.groups.Schedule;

/**
 * Servlet implementation class for Servlet: RoleServlet
 *
 */
 public class RoleServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

    private static final String LIST = "/admin/userGroupView/roles/list.jsp";
    private static final String VIEW = "/admin/userGroupView/roles/view.jsp";
    private static final String EDIT_DETAILS = "/admin/userGroupView/roles/editDetails.jsp";
    private static final String ADD_ENTRY = "/admin/userGroupView/roles/editSpecific.jsp";
    private static final String EDIT_WEEKLY = "/admin/userGroupView/roles/editWeekly.jsp";
    private static final String EDIT_MONTHLY = "/admin/userGroupView/roles/editMonthly.jsp";
    private static final String EDIT_SPECIFIC = "/admin/userGroupView/roles/editSpecific.jsp";
    
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
                request.setAttribute("scheduledUser", role.getDefaultUser().getName());
                Date date = new SimpleDateFormat("MM-dd-yyyy").parse(request.getParameter("date"));
                request.setAttribute("start", date);
                request.setAttribute("end", date);
                request.setAttribute("schedIndex", "-1");
                request.setAttribute("timeIndex", "-1");
                return ADD_ENTRY;
            } catch (ParseException e) {
                throw new ServletException("Unable to parse date: "+e.getMessage(), e);
            }
        }
        
    }
    
    private class EditEntryAction implements Action {
        
        public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException {
            WebRole role = getRoleManager().getRole(request.getParameter("role"));
            request.setAttribute("role", role);
            
            int schedIndex = Integer.parseInt(request.getParameter("schedIndex"));
            request.setAttribute("schedIndex", request.getParameter("schedIndex"));
            
            int timeIndex = Integer.parseInt(request.getParameter("timeIndex"));
            request.setAttribute("timeIndex", request.getParameter("timeIndex"));
            
            Schedule schedule = role.getSchedule(schedIndex);
            request.setAttribute("schedule", schedule);
            Time time = role.getTime(schedIndex, timeIndex);
            request.setAttribute("time", time);

            request.setAttribute("scheduledUser", schedule.getName());

            if (BasicScheduleUtils.isWeekly(time))
                return EDIT_WEEKLY;
            else if (BasicScheduleUtils.isMonthly(time))
                return EDIT_MONTHLY;
            else {
                request.setAttribute("start", BasicScheduleUtils.getSpecificTime(time.getBegins()));
                request.setAttribute("end", BasicScheduleUtils.getSpecificTime(time.getEnds()));
                return EDIT_SPECIFIC;
            }
            
        }
        
    }
    
    private class SaveEntryAction implements Action {
        
        public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException {
            try {
                WebRole role = getRoleManager().getRole(request.getParameter("role"));
                request.setAttribute("role", role);
                if (request.getParameter("save") != null) {
                    
                    int schedIndex = getIntParameter("schedIndex", request);
                    int timeIndex = getIntParameter("timeIndex", request);
                    Date startDate = getDateParameters("start", request);
                    Date endDate = getDateParameters("end", request);
                    if (startDate.equals(endDate)) {
                        request.setAttribute("error", "The start time and the end time must not be the same!");
                        request.setAttribute("scheduledUser", request.getParameter("roleUser"));
                        request.setAttribute("start", startDate);
                        request.setAttribute("end", endDate);
                        request.setAttribute("schedIndex", request.getParameter("schedIndex"));
                        request.setAttribute("timeIndex", request.getParameter("timeIndex"));
                        return EDIT_SPECIFIC;
                    }
                    
                    String user = request.getParameter("roleUser");
                    
                    WebSchedEntry entry = new WebSchedEntry(schedIndex, timeIndex, user, startDate, endDate);
                    
                    role.addEntry(entry);
                    
                    getRoleManager().saveRole(role);
                }
                return new ViewAction().execute(request, response);
                
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
        
        public int getIntParameter(String name, HttpServletRequest request) {
            return Integer.parseInt(request.getParameter(name));
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
            WebRole role = getRoleManager().createRole();
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
                    role = getRoleManager().createRole();
                }
                role.setName(request.getParameter("roleName"));
                role.setDefaultUser(getUserManager().getUser(request.getParameter("roleUser")));
                role.setMembershipGroup(getGroupManager().getGroup(request.getParameter("roleGroup")));
                role.setDescription(request.getParameter("roleDescr"));
                getRoleManager().saveRole(role);
                request.setAttribute("role", getRoleManager().getRole(request.getParameter("roleName")));
                return new ViewAction().execute(request, response);
            } else {
                return new ListAction().execute(request, response);
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
        else if ("addEntry".equals(op))
            return new AddEntryAction();
        else if ("editEntry".equals(op))
            return new EditEntryAction();
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