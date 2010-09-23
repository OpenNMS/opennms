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
// 2007 Dec 12: Fix HTML to work with current CSS. - dj@opennms.org
// 2003 Oct 27: created
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

<%@page language="java"
	contentType="text/html"
	session="true"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Add Interface" />
  <jsp:param name="headTitle" value="Add Interface" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Add Interface" />
</jsp:include>

<%--
 XXX Can't do this because body is in the header:
	onLoad="document.newIpForm.ipAddress.focus()"
--%>


<script type="text/javascript">
        function verifyIpAddress () {
                var prompt = new String("IP Address");
                var errorMsg = new String("");
                var ipValue = new String(document.newIpForm.ipAddress.value);

                var ipPattern = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/;
                var ipArray = ipValue.match(ipPattern); 

                if (ipValue == "0.0.0.0")
                        errorMsg = prompt + ': ' + ipValue + ' is a special IP address and cannot be used here.';
                else if (ipValue == "255.255.255.255")
                        errorMsg = prompt + ': ' + ipValue + ' is a special IP address and cannot be used here.';
                if (ipArray == null)
                        errorMsg = prompt + ': ' + ipValue + ' is not a valid IP address.';
                else {
                        for (i = 0; i < 4; i++) {
                                thisSegment = ipArray[i];
                                if (thisSegment > 255) {
                                        errorMsg = prompt + ': ' + ipValue + ' is not a valid IP address.';
                                        break;
                                }
                        }
                }
                
                if (errorMsg != ""){
                        alert (errorMsg);
						return false;
                }
                else{
                        document.newIpForm.action="admin/addNewInterface";
                        return true;
                }
        }
    
        function cancel()
        {
                document.newIpForm.action="admin/index.jsp";
                document.newIpForm.submit();
        }
</script>

<div class="TwoColLAdmin">
<form method="post" name="newIpForm" onsubmit="return verifyIpAddress();">
  <h3>Enter IP address</h3>
  <div class="boxWrapper">
    <c:if test="${param.action == 'redo'}">
      <ul class="error">
        <li>
          The IP address ${param.ipAddress} already exists.
          Please enter a different IP address.
        </li>
      </ul>
    </c:if>

    <p>
      IP address:
      <input size="15" name="ipAddress">
    </p>

    <p>
      <input type="submit" value="Add">
      <input type="button" value="Cancel" onclick="cancel()">
    </p>

  </div>
</form>
</div>

        <div class="TwoColRAdmin">
      <h3>Add Interface</h3>
        <p>
        Enter in a valid IP address to generate a newSuspectEvent. This will add a node to the OpenNMS
        database for this device. Note: if the IP address already exists in OpenNMS, use "Rescan" from
        the node page to update it. Also, if no services exist for this IP, it will still be added.
        </p>
  </div>
  <hr />

<jsp:include page="/includes/footer.jsp" flush="false" />
