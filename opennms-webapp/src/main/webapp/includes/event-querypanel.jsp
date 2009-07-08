<%--

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		org.opennms.web.element.NetworkElementFactory,
		org.opennms.web.event.*
		"
%>

<script type="text/javascript">
<!--
function Blank_TextField_Validator()
{
  if(document.event_search.msgmatchany.value == "")
     {
     alert("Please Enter in Event Search Text");
     document.event_search.msgmatchany.focus();
     return false;
     }
  return true;
}
-->
</script>

<form name="event_search" action="event/query" method="GET" onsubmit="return Blank_TextField_Validator()">
      <p>Event Text:<input type="text" name="msgmatchany" /> &nbsp; Time:
        <select name="relativetime" size="1">
          <option value="0" selected>Any</option>
          <option value="1">Last hour</option>
          <option value="2">Last 4 hours</option>
          <option value="3">Last 8 hours</option>
          <option value="4">Last 12 hours</option>
          <option value="5">Last day</option>
          <option value="6">Last week</option>
          <option value="7">Last month</option>                
        </select>
        <input type="submit" value="Search" />
</form>



