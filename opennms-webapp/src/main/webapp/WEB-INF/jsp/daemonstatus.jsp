<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html>
  <body>
     <form:form method="post" commandName="toModify">
        <table border="1">
          <tr>
            <td></td>
            <td>Service Name</td>
            <td>Status</td>
          </tr>
          <c:forEach items="${daemons}" var="daemon">
            <tr>
            
              <td> <form:checkbox path="values" value="${daemon.serviceName}"/> </td>
              <td><c:out value="${daemon.serviceName}"> </c:out></td>
              <td><c:out value="${daemon.serviceStatus}"> </c:out> </td>
           
            </tr>
          </c:forEach>
          <tr>
            <td colspan="3">
               <input type="submit" value="stop" name="operation"/>
               <input type="submit" value="start" name="operation"/>
               <input type="submit" value="restart" name="operation"/>
               <input type="submit" value="refresh" name="operation"/>
            </td>
          </tr>
        </table>
        
    </form:form>  
  </body>
</html>
