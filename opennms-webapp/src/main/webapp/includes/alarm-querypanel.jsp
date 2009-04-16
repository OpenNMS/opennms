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
// 2008 Sep 27: Comment-out unused code. - dj@opennms.org
// 2003 Feb 01: Disallowed null alarm search text. Bug #536.
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
	import="org.opennms.web.alarm.AlarmUtil"
%>

<script type="text/javascript">
<!--
function Blank_TextField_Validator()
{
  if(document.alarm_search.msgmatchany.value == "")
     {
     alert("Please Enter in Alarm Search Text");
     document.alarm_search.msgmatchany.focus();
     return false;
     }
  return true;
}
-->
</script>

<form name="alarm_search" action="alarm/query" method="get" onsubmit="return Blank_TextField_Validator()">
	<p><label for="msgmatchany">Alarm Text</label>: <input type="text" id="msgmatchany" name="msgmatchany" /> &nbsp; <label for="relativetime">Time</label>:
		<select id="relativetime" name="relativetime">
			<option value="0" selected><%=AlarmUtil.ANY_RELATIVE_TIMES_OPTION%></option>
			<option value="1">Last hour</option>
			<option value="2">Last 4 hours</option>
			<option value="3">Last 8 hours</option>
			<option value="4">Last 12 hours</option>
			<option value="5">Last day</option>
			<option value="6">Last week</option>
			<option value="7">Last month</option>                
		</select>
		<input type="submit" value="Search" /></p>            
	</form>



