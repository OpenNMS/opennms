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
// 2005 Oct 01: Convert to use CSS for layout. -- DJ Gregor
// 2002 Nov 12: Add response time graphs to webUI.
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

--%>

<%@page language="java" contentType="text/html" session="true"  %>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Web Console" />
</jsp:include>

    <!-- Column 1 of Body -->  
    <div id="index-contentleft">

      <!-- Services down box -->
<%--
  <div class="rbroundbox">
	<div class="rbtop"><div></div></div>
		<div class="rbcontent"><p>
--%>
		<jsp:include page="/includes/servicesdown-box.jsp" flush="false" />
<%--
                </p>
		</div><!-- /rbcontent -->
	<div class="rbbot"><div></div></div>
</div><!-- /rbroundbox -->    
--%>

    </div>

    <!-- Middle Column -->
    <div id="index-contentmiddle">
      <!-- category box(es) -->    
<%--
<div class="rbroundbox">
	<div class="rbtop"><div></div></div>
		<div class="rbcontent"><p>
--%>
		<jsp:include page="/includes/categories-box.jsp" flush="false" />
<%--
                </p>
		</div><!-- /rbcontent -->
	<div class="rbbot"><div></div></div>
</div><!-- /rbroundbox -->    
--%>

               
    </div>

      <!--desktop query box -->  

    <!-- Column 3 of Body -->  
    <div id="index-contentright">
      <!-- notification box -->    
<%--
      <div class="rbroundbox">
	<div class="rbtop"><div></div></div>
		<div class="rbcontent"><p>
--%>
		<jsp:include page="/includes/notification-box.jsp" flush="false" />
<%--
                </p>
		</div><!-- /rbcontent -->
	<div class="rbbot"><div></div></div>
      </div><!-- /rbroundbox -->    
--%>

      <!-- Performance box -->    
<%--
      <div class="rbroundbox">
	<div class="rbtop"><div></div></div>
		<div class="rbcontent"><p>
--%>
		 <jsp:include page="/includes/performance-box.jsp" flush="false" />
<%--
                </p>
		</div><!-- /rbcontent -->
	<div class="rbbot"><div></div></div>
      </div><!-- /rbroundbox -->        
--%>

      <!-- Response Time box -->    
<%--
      <div class="rbroundbox">
	<div class="rbtop"><div></div></div>
		<div class="rbcontent"><p>
--%>
		<jsp:include page="/includes/response-box.jsp" flush="false" />
<%--
                </p>
		</div><!-- /rbcontent -->
	<div class="rbbot"><div></div></div>
      </div><!-- /rbroundbox -->        
--%>
          
      <!-- KSC Reports box -->    
<%--
      <div class="rbroundbox">
	<div class="rbtop"><div></div></div>
		<div class="rbcontent"><p>
--%>
		<jsp:include page="/includes/ksc-box.jsp" flush="false" />
<%--
                </p>
		</div><!-- /rbcontent -->
	<div class="rbbot"><div></div></div>
      </div><!-- /rbroundbox -->          
--%>
 
      <%--
      <!-- security box -->    
        Commenting out the security box include until it is functional
        <jsp:include page="/includes/security-box.jsp" flush="false" />
      --%>
    </div>

  <div class="spacer"><!-- --></div>
  <br/>


<jsp:include page="/includes/footer.jsp" flush="false" />
