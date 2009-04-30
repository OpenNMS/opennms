<%--
//============================================================================
//
// Copyright (c) 2009+ desmax74
// Copyright (c) 2009+ The OpenNMS Group, Inc.
// All rights reserved everywhere.
//
// This program was developed and is maintained by Rocco RIONERO
// ("the author") and is subject to dual-copyright according to
// the terms set in "The OpenNMS Project Contributor Agreement".
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
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
// USA.
//
// The author can be contacted at the following email address:
//
//       Massimiliano Dess&igrave;
//       desmax74@yahoo.it
//
//
//-----------------------------------------------------------------------------
// OpenNMS Network Management System is Copyright by The OpenNMS Group, Inc.
//============================================================================
--%>
<%@ include file="/WEB-INF/jsp/taglibs.jsp"%>
<div id="header">&nbsp;&nbsp;&nbsp;&nbsp;<img src="img/logo.png"/>
	<div id="tabsF">
  		<ul>
    		<li><a href="index.jsp"><span><spring:message code="ui.home"/></span></a></li>
    		<li><a href="group.list.page"><span><spring:message code="ui.groups"/></span></a></li>
    		<li><a href="authority.list.page"><span><spring:message code="ui.authorities"/></span></a></li>
    		<li><a href="user.list.page"><span><spring:message code="ui.users"/></span></a></li>
    		<li><a href="logout.jsp"><span>logout</span></a></li>
  		</ul>
	</div>
</div>