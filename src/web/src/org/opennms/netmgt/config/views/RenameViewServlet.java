
package org.opennms.netmgt.config.views;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.*;
import org.opennms.netmgt.config.views.*;

/**
 * A servlet that handles renaming an existing view
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class RenameViewServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
        String viewName = request.getParameter("viewName");
        String newName = request.getParameter("newName");
        
        //now save to the xml file
        try {
            ViewFactory.init();
            ViewFactory viewFactory = ViewFactory.getInstance();
            viewFactory.renameView(viewName, newName);
        }
        catch( Exception e) {
            throw new ServletException( "Error renaming view " + viewName + " to " + newName, e );
        }
        
        response.sendRedirect( "list.jsp" );
    }
}
