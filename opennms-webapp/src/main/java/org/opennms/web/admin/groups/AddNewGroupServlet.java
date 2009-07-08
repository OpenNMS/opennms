/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2002-2004, 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.web.admin.groups;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.groups.Group;

/**
 * A servlet that handles adding a new group
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class AddNewGroupServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    		GroupFactory.init();
    	} catch (Exception e) {
    		throw new ServletException("AddNewGroupServlet: Error initialising group factory." + e);
    	}
    	GroupManager groupFactory = GroupFactory.getInstance();
    	
        String groupName = request.getParameter("groupName");
        String groupComment = request.getParameter("groupComment");
        if (groupComment == null) {
        	groupComment = "";
        }

        boolean hasGroup = false;
        try {
        	hasGroup = groupFactory.hasGroup(groupName);
        } catch (Exception e) {
        	throw new ServletException("Can't determine if group " + groupName + " already exists in groups.xml.", e);
        }
        
        if (hasGroup) {
        	RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/groups/newGroup.jsp?action=redo");
            dispatcher.forward(request, response);
        } else {
        	Group newGroup = new Group();
        	newGroup.setName(groupName);
        	newGroup.setComments(groupComment);

        	HttpSession groupSession = request.getSession(false);
            groupSession.setAttribute("group.modifyGroup.jsp", newGroup);
            
            // forward the request for proper display
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/groups/modifyGroup.jsp");
            dispatcher.forward(request, response);
        }
    }

    
}
