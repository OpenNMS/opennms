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