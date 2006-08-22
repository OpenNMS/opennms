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
