//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//

package org.opennms.web.admin.views;

import java.io.IOException;
import java.util.*;
import java.text.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.*;
import org.opennms.netmgt.config.views.*;

/**
 * A servlet that handles saving a view
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class UpdateViewServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession userSession = request.getSession(false);
	
    	if (userSession != null)
	{
		View newView = (View)userSession.getAttribute("view.modifyView.jsp");
		
		newView.setMembership(null);
		
		String users[] = request.getParameterValues("selectedUsers");
		Membership membership = new Membership();
		if (users != null)
		{
			for (int i = 0; i < users.length; i++)
			{	
				Member member = new Member();
				member.setType("user");
				member.setContent(users[i]);
				membership.addMember(member);
			}
		}
		
		String groups[] = request.getParameterValues("selectedGroups");
		if (groups != null)
		{
			for (int i = 0; i < groups.length; i++)
			{
				Member member = new Member();
				member.setType("group");
				member.setContent(groups[i]);
				membership.addMember(member);
			}
		}
                if (membership.getMemberCount()>0)
                        newView.setMembership(membership);
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward( request, response );
    }
}
