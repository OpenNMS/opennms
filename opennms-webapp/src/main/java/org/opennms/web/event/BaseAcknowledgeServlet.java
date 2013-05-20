/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.event;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Abstract BaseAcknowledgeServlet class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class BaseAcknowledgeServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4059726823978789453L;

	/** Constant <code>ACKNOWLEDGE_ACTION="1"</code> */
	public static final String ACKNOWLEDGE_ACTION = "1";
	/** Constant <code>UNACKNOWLEDGE_ACTION="2"</code> */
	public static final String UNACKNOWLEDGE_ACTION = "2";
	/** The URL to redirect the client to in case of success. */
	protected String redirectSuccess;

	/**
	 * <p>Constructor for BaseAcknowledgeServlet.</p>
	 */
	public BaseAcknowledgeServlet() {
		super();
	}

	/**
	 * Looks up the <code>dispath.success</code> parameter in the servlet's
	 * config. If not present, this servlet will throw an exception so it will
	 * be marked unavailable.
	 *
	 * @throws javax.servlet.ServletException if any.
	 */
        @Override
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
	 *
	 * @param request a {@link javax.servlet.http.HttpServletRequest} object.
	 * @return a {@link java.lang.String} object.
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
