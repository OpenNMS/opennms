<%@ attribute name="nodes" type="java.util.List" rtexprvalue="true" required="true" %>
<%@ attribute name="isIfAliasSearch"  type="java.lang.Boolean" rtexprvalue="true" required="true" %>
<%@ attribute name="isMaclikeSearch"  type="java.lang.Boolean" rtexprvalue="true" required="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<ul class="plain">
  <c:forEach var="nodeModel" items="${nodes}">
    <c:url var="nodeLink" value="element/node.jsp">
      <c:param name="node" value="${nodeModel.node.id}"/>
    </c:url>
    <li>
      <a href="${nodeLink}">${nodeModel.node.label}</a>
      <ul>
        <c:if test="${!empty nodeModel.interfaces}">
          <c:forEach var="interface" items="${nodeModel.interfaces}">
            <c:url var="interfaceLink" value="element/interface.jsp">
              <c:param name="ipinterfaceid" value="${interface.id}"/>
            </c:url>
            
            <li>
              <c:choose>
                <c:when test="${isMaclikeSearch && interface.snmpInterface.physAddr != null}">
                  <c:choose>
                    <c:when test="${interface.ipAddress != '0.0.0.0'}">
                      <c:set var="label" value="${interface.ipAddress}" scope="page" />
                    </c:when>
                    <c:when test="${interface.snmpInterface.ifName != null}">
                      <c:set var="label" value="${interface.snmpInterface.ifName}" scope="page" />
                    </c:when>
                    <c:when test="${interface.snmpInterface.ifDescr != null}">
                      <c:set var="label" value="${interface.snmpInterface.ifDescr}" scope="page" />
                    </c:when>
                    <c:otherwise>
                      <c:set var="label" value="ifIndex:${interface.snmpInterface.ifIndex}" scope="page" />
                    </c:otherwise>
                  </c:choose>
                  <a href="${interfaceLink}">${label}</a> : ${interface.snmpInterface.physAddr}
                </c:when>
                <c:when test="${isIfAliasSearch}">
                  <c:choose>
                    <c:when test="${interface.ipAddress == '0.0.0.0'}">
                      <c:choose>
                        <c:when test="${interface.snmpInterface.ifName != null}">
                          <a href="${interfaceLink}">${interface.snmpInterface.ifName}</a> : ${interface.snmpInterface.ifAlias}
                        </c:when>
                        
                        <c:when test="${interface.snmpInterface.ifDescr != null}">
                          <a href="${interfaceLink}">${interface.snmpInterface.ifDescr}</a> : ${interface.snmpInterface.ifAlias}
                        </c:when>
                        
                        <c:otherwise>
                          <a href="${interfaceLink}">ifIndex ${interface.snmpInterface.ifIndex}</a> : ${interface.snmpInterface.ifAlias}
                        </c:otherwise>
                      </c:choose>
                    </c:when>
                    
                    <c:otherwise>
                      <a href="${interfaceLink}">${interface.ipAddress}</a> : ${interface.snmpInterface.ifAlias}
                    </c:otherwise>
                  </c:choose>
                </c:when>
                
                <c:otherwise>
                  <a href="${interfaceLink}">${interface.ipAddress}</a>
                </c:otherwise>
              </c:choose>
            </li>
          </c:forEach>
        </c:if>
        <c:if test="${!empty nodeModel.arpInterfaces}">
          <c:forEach var="arpInterface" items="${nodeModel.arpInterfaces}">
            <li>
              <c:if test="${isMaclikeSearch && arpInterface.physAddr!=null && arpInterface.physAddr!=null}">
                <c:set var="notFound" value="true"/>
                <c:forEach var="ipInterface" items="${nodeModel.node.ipInterfaces}">
                  <c:if test="${ipInterface.ipAddress == arpInterface.ipAddress}">
                    <a href="element/interface.jsp?ipinterfaceid=${ipInterface.id}">${arpInterface.ipAddress}</a> : ${arpInterface.physAddr} (from arp)
                    <c:remove var="notFound"/>
                  </c:if>
                </c:forEach>
                <c:if test="${notFound}">
                  ${arpInterface.ipAddress} : ${arpInterface.physAddr} (from arp)
                </c:if>
              </c:if>
            </li>
          </c:forEach>
        </c:if>
      </ul>
    </li>
  </c:forEach>
</ul>
