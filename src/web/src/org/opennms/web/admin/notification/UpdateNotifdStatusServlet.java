
package org.opennms.web.admin.notification;

import java.io.IOException;
import java.util.*;
import java.text.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.*;

/**
 * A servlet that handles updating the status of the notifications
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class UpdateNotifdStatusServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
        try
	{
		System.out.println("status = " + request.getParameter("status"));
                if (request.getParameter("status").equals("on"))
                {
                        NotifdConfigFactory.getInstance().turnNotifdOn();
                }
                else
                {
                        NotifdConfigFactory.getInstance().turnNotifdOff();
                }
	}
	catch (Exception e)
	{
		new ServletException("Could not update notification status: " + e.getMessage(), e);
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/index.jsp");
        dispatcher.forward( request, response );
    }
}
