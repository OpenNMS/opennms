<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
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

--%>

<%-- 
  This page is included by other JSPs to create a table containing
  the service level availability for a particular service.  
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
	    java.io.IOException,
		org.exolab.castor.xml.MarshalException,
		org.exolab.castor.xml.ValidationException,
		org.opennms.web.category.*,
		org.opennms.web.element.*
	"
%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%!
    private CategoryModel m_model;
    
    private double m_normalThreshold;
    private double m_warningThreshold;
    

    public void init() throws ServletException {
        try {
            m_model = CategoryModel.getInstance();
            
            m_normalThreshold  = m_model.getCategoryNormalThreshold(CategoryModel.OVERALL_AVAILABILITY_CATEGORY);
            m_warningThreshold = m_model.getCategoryWarningThreshold(CategoryModel.OVERALL_AVAILABILITY_CATEGORY);            
        } catch (IOException e) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        } catch (MarshalException e) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        } catch (ValidationException e) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }
    }
%>

<%
    Service service = ElementUtil.getServiceByParams(request, getServletContext());
    
    String styleClass;
    String statusContent;

    if (service.isManaged()) {
        //find the availability value for this node
        double rtcValue =
            m_model.getServiceAvailability(service.getNodeId(),
	                                       service.getIpAddress(),
                                           service.getServiceId());
        
        styleClass = CategoryUtil.getCategoryClass(m_normalThreshold,
                                                   m_warningThreshold,
                                                   rtcValue);
    	statusContent = CategoryUtil.formatValue(rtcValue) + "%";
    } else {
        styleClass = "Indeterminate";
		statusContent = ElementUtil.getServiceStatusString(service);
    }
    
%>

<h3>Overall Availability</h3>
<table>
  <tr class="<%= styleClass %>"/>
    <td class="divider bright"><%= statusContent %></td>
  </tr>
</table>
