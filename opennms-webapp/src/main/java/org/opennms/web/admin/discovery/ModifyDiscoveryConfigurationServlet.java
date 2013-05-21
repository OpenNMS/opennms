/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.admin.discovery;
import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;



/**
 * A servlet that handles updating the status of the notifications
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class ModifyDiscoveryConfigurationServlet extends HttpServlet {

	/**
     * 
     */
    private static final long serialVersionUID = -3782436743630940629L;

	/** Constant <code>log</code> */
	protected static ThreadCategory log = ThreadCategory.getInstance("WEB");

	/** {@inheritDoc} */
    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	    log.info("Loading Discovery configuration.");
	    DiscoveryConfiguration config=getDiscoveryConfig();
	    HttpSession sess = request.getSession();
        //sess.removeAttribute("discoveryConfiguration");
        sess.setAttribute("discoveryConfiguration",config);
        response.sendRedirect("edit-config.jsp");
        //RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/discovery/index.jsp");
        //dispatcher.forward(request, response);
    }
	
	/** {@inheritDoc} */
    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	/**
	 * <p>getDiscoveryConfig</p>
	 *
	 * @return a {@link org.opennms.netmgt.config.discovery.DiscoveryConfiguration} object.
	 */
	public static DiscoveryConfiguration getDiscoveryConfig() {
        DiscoveryConfiguration config = null;
        try {
             DiscoveryConfigFactory.reload();
             config = DiscoveryConfigFactory.getInstance().getConfiguration();
        } catch (final Exception e) {
            new ServletException("Could not load configuration: " + e.getMessage(), e);
        }
        return config;
	}
}
