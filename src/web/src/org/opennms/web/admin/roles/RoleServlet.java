package org.opennms.web.admin.roles;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class for Servlet: RoleServlet
 *
 */
 public class RoleServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
    private static final String LIST = "/admin/userGroupView/roles/list.jsp";

    
    public RoleServlet() {
		super();
        
	}
    
    private interface Action {
        public String execute(HttpServletRequest request, HttpServletResponse response);
    }
    
    private class ListAction implements Action {
        public String execute(HttpServletRequest request, HttpServletResponse response) {
            HttpSession userSession = request.getSession(false);

            List list = new LinkedList();
            for(int i = 0; i < 11; i++) {
              list.add(new WebRole());  
            }
            userSession.setAttribute("roleList", list);
            
            return LIST;
        }
        
    }
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Action action = new ListAction();
        String display = action.execute(request, response);
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(display);
        dispatcher.forward(request, response);
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}   	  	    
}