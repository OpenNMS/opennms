/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.web.admin.discovery;
import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;



/**
 * A servlet that handles updating the status of the notifications
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class ModifyDiscoveryConfigurationServlet extends HttpServlet {

    protected static Category log = ThreadCategory.getInstance("WEB");

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	    log.info("Loading Discovery configuration.");
	    DiscoveryConfiguration config=null;
		try {
             DiscoveryConfigFactory.reload();
             config = DiscoveryConfigFactory.getInstance().getConfiguration();
        } catch (Exception e) {
            new ServletException("Could not load configuration: " + e.getMessage(), e);
        }
        HttpSession sess = request.getSession();
        //sess.removeAttribute("discoveryConfiguration");
        sess.setAttribute("discoveryConfiguration",config);
        response.sendRedirect("edit-config.jsp");
        //RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/discovery/index.jsp");
        //dispatcher.forward(request, response);
    }
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
