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
<springf:form action="group.edit.page" commandName="group" method="post">
    <table>
        <tr>
            <td><spring:message code="group" />:</td>
            <td><springf:input path="name" maxlength="45"/></td>
            <td><springf:errors path="name" /></td>
        </tr>
        <tr>
          <td colspan="3">
              <input type="submit" value="<spring:message code="ui.action.save"/>"/> 
              <input type="hidden" id="id" name="id" value="${group.id}"/> 
              <input type="button" onclick="location.href = 'group.list.page'" value="<spring:message code="group.list"/>"/>
          </td>
        </tr>
    </table>
</springf:form>