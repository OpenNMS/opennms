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
