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
	session="true"%>
<%@page language="java" contentType="text/html" session="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Inventory Reports" />
  <jsp:param name="headTitle" value="Inventory Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="Inventory(Rancid) Reports" />
</jsp:include>

<div class="TwoColLeft">
    <!-- general info box -->
    <h3>Generate reports</h3>
    <table class="o-box">
	</table>
	
    <h3>Inventory</h3>
	<form id="inventoryReport" method="post" name="inventoryReport">
	<table class="o-box">
		<tr>
		    <th width="50%"><label id="date" for="date">Date:</label></th>
		    <td><input id="date" type="text" name="date" value="YYYY/MM/DD"></td>
		</tr>
		<tr>
		    <th width="50%"><label id="fieldhas" for="fieldhas">Matching:</label></th>
		    <td><input id="fieldhas" type="text" name="fieldhas" value=""></td>
		</tr>
		<tr>
		    <th width="50%"><label id="reporttype" for="reporttype">Report type:</label></th>
		    <td>
		    	<select name="reporttype">
				<option value="rancidlist">Rancid</option>
				<option value="inventory">Inventory</option>
				</select>
			</td>
		 </tr>
		 <tr>
		 <th> Report format: </th>
		 <td>
		 	<input type="radio" name="reportfiletype" value="pdftype" > XML
			<input type="radio" name="reportfiletype" value="htmltype" checked > HTML
		</td>
		</tr>
		<tr>
		    <th width="50%"><label id="reportemail" for="reportemail">Email to:</label></th>
		    <td><input id="reportemail" type="text" name="reportemail" value=""></td>
		</tr>
		 <tr>
		 	<th></th>
		 	<th><input id="run" type="button" value="run" onClick="runInventory()">
		 	</th>
		 </tr>	
		 </table>
		 </form>
</div>

  <div class="TwoColRight">
    <h3>Descriptions</h3>
    <div class="boxWrapper">
          <p><b>Date</b> provide a date at which you want to get report.
          If you leave it blank the report data is today. 
      </p>

          <p><b>Matching</b> provide a string to match on. The report will only contain
          the items that match the specified string.
          If you leave it blank the report data contains all items. This matching applies
          only to inventory report, it will be ignored in case of rancid list report.
      </p>

      <p><b>Report Type</b> Selecting <em>Rancid</em> you get a list of all the rancid
            devices in router.db (for every rancid group) at a specified date. 
            Selecting <em>Inventory</em> you get a list of all inventory items for each device
            that had inventory data at the specified data for matching string
      </p>
      
      <p><b>Report Format</b> Selecting <em>HTML</em> you get an HTML report.
        Selecting <em>XML</em> you get an XML report.
      </p>
      
      <p><b>Email</b> Sets the email address of the user that will receive the report.
      </p>
    
	</div>
  </div>

<jsp:include page="/includes/footer.jsp" flush="false" />

<script language="JavaScript">

function runInventory() {
	  document.inventoryReport.action="inventory/rancidReportExec.htm";
	  document.inventoryReport.submit();	
}

</script>

