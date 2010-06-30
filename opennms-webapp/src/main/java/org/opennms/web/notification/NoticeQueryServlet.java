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
// Modifications:
//
// 2008 Sep 28: Handle XSS security issues. - ranger@opennms.org
// 2007 Jul 24: Add serialVersionUID and Java 5 generics. - dj@opennms.org
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.notification;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.WebSecurityUtils;

/**
 * A servlet that handles querying the notifications table and and then forwards
 * the query's result to a JSP for display.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @version $Id: $
 * @since 1.6.12
 */
public class NoticeQueryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /** Constant <code>DEFAULT_LIMIT=25</code> */
    public static final int DEFAULT_LIMIT = 25;

    /** Constant <code>DEFAULT_MULTIPLE=0</code> */
    public static final int DEFAULT_MULTIPLE = 0;

    /**
     * {@inheritDoc}
     *
     * Parses the query string to determine what type of notice query to perform
     * (for example, what to filter on or sort by), then does the database query
     * (through the NoticeFactory) and then forwards the results to a JSP for
     * display.
     *
     * <p>
     * Sets the <em>notices</em> and <em>parms</em> request attributes for
     * the forwardee JSP (or whatever gets called).
     * </p>
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // handle the style sort parameter
        String sortStyleString = WebSecurityUtils.sanitizeString(request.getParameter("sortby"));
        NoticeFactory.SortStyle sortStyle = NoticeFactory.SortStyle.ID;
        if (sortStyleString != null) {
            Object temp = NoticeUtil.getSortStyle(sortStyleString);
            if (temp != null) {
                sortStyle = (NoticeFactory.SortStyle) temp;
            }
        }

        // handle the acknowledgment type parameter
        String ackTypeString = WebSecurityUtils.sanitizeString(request.getParameter("acktype"));
        NoticeFactory.AcknowledgeType ackType = NoticeFactory.AcknowledgeType.UNACKNOWLEDGED;
        if (ackTypeString != null) {
            Object temp = NoticeUtil.getAcknowledgeType(ackTypeString);
            if (temp != null) {
                ackType = (NoticeFactory.AcknowledgeType) temp;
            }
        }

        // handle the filter parameters
        String[] filterStrings = request.getParameterValues("filter");
        List<NoticeFactory.Filter> filterArray = new ArrayList<NoticeFactory.Filter>();
        if (filterStrings != null) {
            for (int i = 0; i < filterStrings.length; i++) {
                NoticeFactory.Filter filter = NoticeUtil.getFilter(WebSecurityUtils.sanitizeString(filterStrings[i]));
                if (filter != null) {
                    filterArray.add(filter);
                }
            }
        }

        // handle the optional limit parameter
        String limitString = request.getParameter("limit");
        int limit = DEFAULT_LIMIT;
        if (limitString != null) {
            try {
                int newlimit = WebSecurityUtils.safeParseInt(limitString);
                if (newlimit > 0) {
                    limit = newlimit;
                }
            } catch (NumberFormatException e) {
            }
        }

        // handle the optional multiple parameter
        String multipleString = request.getParameter("multiple");
        int multiple = DEFAULT_MULTIPLE;
        if (multipleString != null) {
            try {
                multiple = Math.max(0, WebSecurityUtils.safeParseInt(multipleString));
            } catch (NumberFormatException e) {
            }
        }

        try {
            // put the parameters in a convenient struct
            NoticeQueryParms parms = new NoticeQueryParms();
            parms.sortStyle = sortStyle;
            parms.ackType = ackType;
            parms.filters = filterArray;
            parms.limit = limit;
            parms.multiple = multiple;

            // query the notices with the new filters array
            Notification[] notices = NoticeFactory.getNotices(sortStyle, ackType, parms.getFilters(), limit, multiple * limit);

            // add the necessary data to the request so the
            // JSP (or whatever gets called) can create the view correctly
            request.setAttribute("notices", notices);
            request.setAttribute("parms", parms);

            // forward the request for proper display
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/notification/browser.jsp");
            dispatcher.forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("", e);
        }
    }

}
