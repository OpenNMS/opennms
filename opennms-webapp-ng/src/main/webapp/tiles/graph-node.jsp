<%@page language="java" contentType="text/html" session="true"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

   	  <h2><c:out value="${node.node.nodeLabel}"/></h2>

	  <h3>Node Performance Data</h3>
	  <ul>
	  <c:forEach var="dataSource" items="${node.dataSources}">
		<li><font class="products" id="<c:out value='${dataSource.id}'/>"><c:out value="${dataSource.name}"/></font><a href="graph.htm?add=<c:out value="${dataSource.id}"/>" class="products"></a></li>
	  </c:forEach>
	  </ul>

      <c:forEach var="iface" items="${node.interfaces}">
	  <h3>Interface: <c:out value="${iface.nodeInterface.ipAddr}"/></h3>
	  <ul>
	    <c:forEach var="dataSource" items="${iface.dataSources}">
		  <li><font class="products" id="<c:out value='${dataSource.id}'/>"><c:out value="${dataSource.name}"/></font><a href="graph.htm?add=<c:out value="${dataSource.id}"/>" class="products"></a></li>
		</c:forEach>
		
		<li class="spacer">&nbsp;</li>

		<c:forEach var="service" items="${iface.services}">
		  <c:if test="${!empty service.dataSource}">
		    <li><font class="products" id="<c:out value='${service.dataSource.id}'/>"><c:out value="${service.dataSource.name}"/></font><a href="graph.htm?add=<c:out value="${service.dataSource.id}"/>" class="products"></a></li>
		  </c:if>
		</c:forEach>
	  </ul>
	  </c:forEach>
	  
	  <script type="text/javascript">
	  <c:forEach var="dataSource" items="${node.dataSources}">
	  	new Draggable('<c:out value="${dataSource.id}"/>', {revert:true});
	  </c:forEach>
	  <c:forEach var="iface" items="${node.interfaces}">
	    <c:forEach var="dataSource" items="${iface.dataSources}">
  	  	  new Draggable('<c:out value="${dataSource.id}"/>', {revert:true});
  		</c:forEach>
	    <c:forEach var="service" items="${iface.services}">
		  <c:if test="${!empty service.dataSource}">
  	  	    new Draggable('<c:out value="${service.dataSource.id}"/>', {revert:true});
  	  	  </c:if>
  		</c:forEach>
	  </c:forEach>
	  </script>
	  
