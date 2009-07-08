/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.web.event;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public abstract class BaseAcknowledgeServlet extends HttpServlet {

	public static final String ACKNOWLEDGE_ACTION = "1";
	public static final String UNACKNOWLEDGE_ACTION = "2";
	/** The URL to redirect the client to in case of success. */
	protected String redirectSuccess;

	public BaseAcknowledgeServlet() {
		super();
	}

	/**
	 * Looks up the <code>dispath.success</code> parameter in the servlet's
	 * config. If not present, this servlet will throw an exception so it will
	 * be marked unavailable.
	 */
	public void init() throws ServletException {
	    ServletConfig config = this.getServletConfig();
	
	    this.redirectSuccess = config.getInitParameter("redirect.success");
	
	    if (this.redirectSuccess == null) {
	        throw new UnavailableException("Require a redirect.success init parameter.");
	    }
	}

	/**
	 * Convenience method for dynamically creating the redirect URL if
	 * necessary.
	 */
	protected String getRedirectString(HttpServletRequest request) {
	    String redirectValue = request.getParameter("redirect");
	
	    if (redirectValue != null) {
	        return (redirectValue);
	    }
	
	    redirectValue = this.redirectSuccess;
	    String redirectParms = request.getParameter("redirectParms");
	
	    if (redirectParms != null) {
	        StringBuffer buffer = new StringBuffer(this.redirectSuccess);
	        buffer.append("?");
	        buffer.append(redirectParms);
	        redirectValue = buffer.toString();
	    }
	
	    return (redirectValue);
	}

}