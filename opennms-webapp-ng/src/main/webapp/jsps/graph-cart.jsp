<%@ page language="java" contentType="text/html" session="true" import="org.opennms.secret.model.GraphDefinition"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<% response.setHeader("Cache-control", "no-cache"); %>

<%--
<jsp:useBean id="graph" scope="session" type="Rrd_graph_def"/>


       <div id="graph-box">
       	<c:if test="${!empty graph.datasources.def}">
     		<img src="generatedImages/chart.png?timestamp=<%= System.currentTimeMillis() %>"/>
     	</c:if>
       </div>

       <div id="items">
           	<ul class="cart-items">
           	  <c:forEach items="${graph.datasources.def}" var="item">
           		<li><font class="cart-items" id="item_<c:out value="${item.id}"/>"><c:out value="${item.name}"/></font></li>
           	  </c:forEach>
           	</ul>

          	<c:forEach items="${graph.datasources.def}" var="item">
	           	<script type="text/javascript">new Draggable('item_<c:out value="${item.id}"/>', {revert:true})</script>
           	</c:forEach>
           	
           <div class="title">
           	<c:if test="${empty graph.datasources.def}">
           	    	Drag items here.
           	</c:if>
           </div>
       </div>

--%>

<jsp:useBean id="graphDef" scope="session" type="GraphDefinition"/>


       <div id="graph-box">
       	<c:if test="${!empty graphDef.graphDataElements}">
     		<img src="generatedImages/chart.png?timestamp=<%= System.currentTimeMillis() %>"/>
     	</c:if>
       </div>

       <div id="items">
           	<ul class="cart-items">
           	  <c:forEach items="${graphDef.graphDataElements}" var="item">
           		<li><font class="cart-items" id="item_<c:out value="${item.uniqueID}"/>"><select name="type"><option selected>Line</option><option>Area</option><option>Stack</option></select>: <input type="text" name="name" size="20" value="<c:out value="${item.dataSource.name}"/>"/></font><a href="graph.htm?remove=item_<c:out value="${item.uniqueID}"/>" class="products"></a></li>
           	  </c:forEach>
           	</ul>

          	<c:forEach items="${graphDef.graphDataElements}" var="item">
	           	<script type="text/javascript">new Draggable('item_<c:out value="${item.uniqueID}"/>', {revert:true})</script>
           	</c:forEach>
           	
           <div class="title">
           	<c:if test="${empty graphDef.graphDataElements}">
           	    	Drag items here.
           	</c:if>
           </div>
       </div>
