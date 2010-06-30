//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 2008 Daniel J. Gregor, Jr..  All Rights Reserved.
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
//      http://www.opennms.com/
//
package org.opennms.web.acegisecurity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.acegisecurity.Authentication;
import org.acegisecurity.ui.logout.LogoutHandler;

/**
 * <p>SessionNotatingLogoutHandler class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class SessionNotatingLogoutHandler implements LogoutHandler {
    /** Constant <code>SESSION_ATTRIBUTE="EVENT_SENDING_LOGOUT_HANDLER_LOGGED_OUT"</code> */
    public static final String SESSION_ATTRIBUTE = "EVENT_SENDING_LOGOUT_HANDLER_LOGGED_OUT";

    /** {@inheritDoc} */
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(SESSION_ATTRIBUTE, Boolean.TRUE);
        }
    }
}
