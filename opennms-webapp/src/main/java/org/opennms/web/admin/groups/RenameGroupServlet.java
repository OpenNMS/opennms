/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.admin.groups;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.GroupManager;

/**
 * A servlet that handles renaming an existing group
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class RenameGroupServlet extends HttpServlet {
    private static final long serialVersionUID = -607871783547629551L;

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
        	GroupFactory.init();
        } catch (Throwable e) {
        	throw new ServletException("RenameGroupServlet: Error initialising group factory." + e);
        }
        GroupManager groupFactory = GroupFactory.getInstance();
        
    	String groupName = request.getParameter("groupName");
        String newName = request.getParameter("newName");
        if (newName == null) {
        	newName = "";
        }
        
        // now save to the xml file
        boolean hasGroup = false;
        try {
        	hasGroup = groupFactory.hasGroup(newName);
        } catch (Throwable e) {
        	throw new ServletException("Can't determine if group " + newName + " already exists in groups.xml.", e);
        }
        if (hasGroup) {
        	response.sendRedirect("list.jsp?action=cantrename");
        } else {
	        try {
	            groupFactory.renameGroup(groupName, newName);
	        } catch (Throwable e) {
	            throw new ServletException("Error renaming group " + groupName + " to " + newName, e);
	        }
	
	        response.sendRedirect("list.jsp");
        }
    }

}
