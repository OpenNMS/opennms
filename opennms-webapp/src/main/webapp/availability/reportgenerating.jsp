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
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 2003 Jan 31: Added RRA information to poller packages.
// 2002 Nov 26: Fixed breadcrumbs issue.
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
//      http://www.opennms.com///

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		org.opennms.netmgt.config.*
	"
%>

<%!
    protected UserManager userFactory;

    public void init() throws ServletException {
        try { 				
            UserFactory.init();
            this.userFactory = UserFactory.getInstance();
        }
        catch(Exception e) {
            this.log("could not initialize the UserFactory", e);
            throw new ServletException("could not initialize the UserFactory", e);
        }
    }
%>

<%
    String email = this.userFactory.getEmail(request.getRemoteUser());
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Generating Report" />
  <jsp:param name="headTitle" value="Availability" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='availability/index.jsp'>Availability Report</a>" />
  <jsp:param name="breadcrumb" value="Report Generation" />
</jsp:include>

<h3>Availability Report Generating</h3>

      <p>
        The availability report you requested is now being generated.  This is a very 
        comprehensive report, and so can take up to a couple of hours to generate.
        It will be emailed to your email address (<%=email%>) as soon as it is 
        finished.
      </p>
      
      <p>
        <a href="availability/index.jsp">Go to the Availability Report page</a>
      </p>
      <p>
        <a href="report/index.jsp">Go to the Report menu</a>
      </p>      

<jsp:include page="/includes/footer.jsp" flush="false" />
