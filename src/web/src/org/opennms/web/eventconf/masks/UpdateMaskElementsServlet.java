
package org.opennms.web.eventconf.masks;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that handles saving Mask Elements to an Event's mask
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class UpdateMaskElementsServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
	
    	if (user != null)
	{
		List maskElements = (List)user.getAttribute("maskElements.editMask.jsp");
		
		String[] names = request.getParameterValues("mask");
		
		maskElements.clear();
		if (names != null)
		{
			for (int i = 0; i < names.length; i++)
			{
				MaskElement newElement = new MaskElement();
				
				newElement.setElementName(names[i]);
				
				String values = request.getParameter("mask"+i+"Values");
				
				parseValues(values, newElement);
				maskElements.add(newElement);
			}
		}
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward( request, response );
    }
    
    /**
    */
    private void parseValues(String valuesBuffer, MaskElement element)
    {
	    StringTokenizer tokenizer = new StringTokenizer(valuesBuffer, "\n");
	    
	    while(tokenizer.hasMoreTokens())
	    {
		    String value = tokenizer.nextToken();
		    element.addElementValue(value);
	    }
    }
}
