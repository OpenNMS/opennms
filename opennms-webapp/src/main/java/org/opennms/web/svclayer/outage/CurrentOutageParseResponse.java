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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.model.OnmsOutage;

public class CurrentOutageParseResponse {
	
	static SuppressOutages m_suppress = new SuppressOutages ();
	
	
	public void ParseResponse (HttpServletRequest request) {
		return;
	}
	
	  public static  Map findSelectedOutagesIDs(HttpServletRequest request, OutageService outageService) {
		  
		  Map<String, String> myOutages = new HashMap<String, String>();
		  ArrayList outages = new ArrayList();
	       Enumeration parameterNames = request.getParameterNames();

			while (parameterNames.hasMoreElements()) {
	            String parameterName = (String) parameterNames.nextElement();
	            if (parameterName.startsWith("chkbx_")) {
	                String OutageId = StringUtils.substringAfter(parameterName, "chkbx_");
	                String parameterValue = request.getParameter(parameterName);
	                if (parameterValue.equals(SuppressOutageCheckBoxConstants.SELECTED)) {
	                    if (!outages.contains(OutageId)) {
	                        m_suppress.suppress(Integer.parseInt(OutageId), request.getParameter("suppresstime_" + OutageId),
	                        	outageService, request.getRemoteUser().toString());
	                        
	                        myOutages.remove(OutageId);
	                    }
	            
	                    
	                } else {
	                		myOutages.remove(OutageId);
	                }
	            }
	        }

	        return myOutages;
	    }

	    public static Collection getSelectedoutages(Collection outages, Collection selectedoutagesIds) {
	        Collection result = new ArrayList();

	        for (Iterator iter = selectedoutagesIds.iterator(); iter.hasNext();) {
	            String selectedOutage = (String) iter.next();
	            result.add(getOutage(outages, selectedOutage));
	        }

	        return result;
	    }
	    

	    private static List getOutage(Collection outages, String selectedOutage) {
//	        for (Iterator iter = outages.iterator(); iter.hasNext();) {
//	            President president = (President) iter.next();
//	            Integer presidentId = president.getPresidentId();
//	            if (presidentId.toString().equals(selectedPresident)) {
//	                return president;
//	            }
//	        }

	        return null;
	    }

	    
	    
}
