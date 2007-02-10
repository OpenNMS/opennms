<%@ attribute name="nodes" type="java.util.List" rtexprvalue="true" required="true" %>
<%@ attribute name="isIfAliasSearch"  type="java.lang.Boolean" rtexprvalue="true" required="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<ul class="plain">
  <c:forEach var="nodeModel" items="${nodes}">
    <c:url var="nodeLink" value="element/node.jsp">
      <c:param name="node" value="${nodeModel.node.nodeId}"/>
    </c:url>
    <li>
      <a href="${nodeLink}">${nodeModel.node.label}</a>
      <c:if test="${!empty nodeModel.interfaces}">
        <ul>
          <c:forEach var="interface" items="${nodeModel.interfaces}">
            <li>
              <c:choose>
                <c:when test="${isIfAliasSearch}">
                  <c:choose>
                    <c:when test="${interface.ipAddress == '0.0.0.0'}">
                      ${interface.snmpIfName}
                    </c:when>
                    
                    <c:otherwise>
                      ${interface.ipAddress}
                    </c:otherwise>
                  </c:choose>
                </c:when>
                
                <c:otherwise>
                  <c:url var="interfaceLink" value="element/interface.jsp">
                    <c:param name="node" value="${interface.nodeId}"/>
                    <c:param name="intf" value="${interface.ipAddress}"/>
                  </c:url>
                  <a href="${interfaceLink}">${interface.ipAddress}</a>
                </c:otherwise>
              </c:choose>
            </li>
          </c:forEach>
        </ul>
      </c:if>
    </li>
  </c:forEach>
</ul>