//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.web.svclayer.outage;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

public class OutagesFilteringView {

	// String whoooha = "select 1154363839::int4::abstime;";

	// Possible values returned to me

	public String filterQuery(HttpServletRequest request) {

		String queryResult = "";
		Locale locale = Locale.getDefault();
		SimpleDateFormat d_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				locale);

		Enumeration parameterNames = request.getParameterNames();
	
		if (request.getQueryString() != null ) {
		
		StringTokenizer st = new StringTokenizer(request.getQueryString(), "&");
		
		
		while (st.hasMoreTokens()) {
	        String temp = st.nextToken();
	        String parameterName = temp.substring(0, temp.indexOf('='));
	        String parameterValue = temp.substring(temp.indexOf('=') + 1, temp.length());
	        
			// node
			if (parameterName.startsWith("nodeid")) {
				
				queryResult = queryResult + " AND outages.nodeid = '"
						+ parameterValue + "'";
			}

			if (parameterName.startsWith("not_nodeid")) {
				queryResult = queryResult + " AND outages.nodeid <> '"
						+ parameterValue + "\'";
			}

			if (parameterName.startsWith("ipaddr")) {
				queryResult = queryResult + " AND outages.ipaddr ='"
						+ parameterValue + "'";
			}

			if (parameterName.startsWith("not_ipaddr")) {
				queryResult = queryResult + " AND outages.ipaddr <> '"
						+ parameterValue + "'";
			}

			if (parameterName.startsWith("serviceid")) {
				queryResult = queryResult + " AND outages.serviceid ='"
						+ parameterValue + "'";
			}

			if (parameterName.startsWith("not_serviceid")) {
				queryResult = queryResult + " AND outages.serviceid <> '"
						+ parameterValue + "'";
			}

			if (parameterName.startsWith("smaller_iflostservice")) {
				Date date = new Date(Long.parseLong(parameterValue));
				queryResult = queryResult + " AND outages.iflostservice < "
						+ "'" + d_format.format(date) + "'";

			}

			if (parameterName.startsWith("bigger_iflostservice")) {
				Date date = new Date(Long.parseLong(parameterValue));
				queryResult = queryResult + " AND outages.iflostservice > "
						+ "'" + d_format.format(date) + "'";

			}

			if (parameterName.startsWith("smaller_ifregainedservice")) {
				Date date = new Date(Long.parseLong(parameterValue));
				queryResult = queryResult + " AND outages.iflostservice < "
						+ "'" + d_format.format(date) + "'";
			}

			if (parameterName.startsWith("bigger_ifregainedservice")) {
				Date date = new Date(Long.parseLong(parameterValue));
				queryResult = queryResult + " AND outages.iflostservice > "
						+ "'" + d_format.format(date) + "'";
			}

		}
		}
		
		return queryResult;
	}

}