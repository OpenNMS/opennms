<%@ attribute name="nodes" type="java.util.List" rtexprvalue="true" required="true" %>
<%@ attribute name="snmpParm" type="java.lang.String" rtexprvalue="true" required="true" %>
<%@ attribute name="isMaclikeSearch"  type="java.lang.Boolean" rtexprvalue="true" required="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<ul class="list-unstyled">
  <c:forEach var="nodeModel" items="${nodes}">
    <c:url var="nodeLink" value="element/node.jsp">
      <c:param name="node" value="${nodeModel.node.id}"/>
    </c:url>
    <li>
      <c:choose>
        <c:when test="${!empty nodeModel.node.foreignSource}">
          <div class="NLnode"><a href="${nodeLink}">${nodeModel.node.label}</a>&nbsp;&nbsp;<span class="NLdbid label label-default" title="Database ID: ${nodeModel.node.id}"><i class="fa fa-database"></i>&nbsp;${nodeModel.node.id}</span>&nbsp;<span class="NLfs label label-default" title="Requisition: ${nodeModel.node.foreignSource}"><i class="fa fa-list-alt"></i>&nbsp;${nodeModel.node.foreignSource}</span>&nbsp;<span class="NLfid label label-default" title="Foreign ID: ${nodeModel.node.foreignId}"><i class="fa fa-qrcode"></i>&nbsp;${nodeModel.node.foreignId}</span></div>
        </c:when>
        <c:otherwise>
          <div class="NLnode"><a href="${nodeLink}">${nodeModel.node.label}</a>&nbsp;&nbsp;<span class="NLdbid label label-default" title="Database ID: ${nodeModel.node.id}"><i class="fa fa-database"></i>&nbsp;${nodeModel.node.id}</span></div>
        </c:otherwise>
      </c:choose>
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
                      <c:set var="label" value="${interface.ipAddressAsString}" scope="page" />
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
                <c:when test="${snmpParm == ('ifAlias')}">
                  <c:choose>
                    <c:when test="${interface.ipAddressAsString == '0.0.0.0'}">
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
                      <a href="${interfaceLink}">${interface.ipAddressAsString}</a> : ${interface.snmpInterface.ifAlias}
                    </c:otherwise>
                  </c:choose>
                </c:when>        
                <c:when test="${snmpParm == ('ifName')}">
                  <c:choose>
                    <c:when test="${interface.ipAddressAsString == '0.0.0.0'}">
                      <c:choose>
                        <c:when test="${interface.snmpInterface.ifName != null}">
                          <a href="${interfaceLink}">${interface.snmpInterface.ifName}</a>
                        </c:when>
                      </c:choose>
                    </c:when>
                    <c:otherwise>
                      <a href="${interfaceLink}">${interface.ipAddressAsString}</a> : ${interface.snmpInterface.ifName}
                    </c:otherwise>
                  </c:choose>
                </c:when>   
                 <c:when test="${snmpParm == ('ifDescr')}">
                  <c:choose>
                    <c:when test="${interface.ipAddressAsString == '0.0.0.0'}">
                      <c:choose>
                        <c:when test="${interface.snmpInterface.ifName != null}">
                          <a href="${interfaceLink}">${interface.snmpInterface.ifName}</a> : ${interface.snmpInterface.ifDescr}
                        </c:when>                  
                        <c:when test="${interface.snmpInterface.ifDescr != null}">
                          <a href="${interfaceLink}">${interface.snmpInterface.ifDescr}</a>
                        </c:when>
                      </c:choose>
                    </c:when>
                    <c:otherwise>
                      <a href="${interfaceLink}">${interface.ipAddressAsString}</a> : ${interface.snmpInterface.ifDescr}
                    </c:otherwise>
                  </c:choose>
                </c:when>
                <c:otherwise>
                  <a href="${interfaceLink}">${interface.ipAddressAsString}</a>
                </c:otherwise>
              </c:choose>
            </li>
          </c:forEach>
        </c:if>
        <c:if test="${!empty nodeModel.snmpInterfaces}">
          <c:forEach var="snmpInterface" items="${nodeModel.snmpInterfaces}">
            <c:url var="interfaceLink" value="element/interface.jsp">
              <c:param name="node" value="${snmpInterface.node.id}"/>
              <c:forEach var="ipInterface" items="${nodeModel.node.ipInterfaces}">
                <c:if test="${ipInterface.snmpInterface.id == snmpInterface.id}">
                  <c:param name="intf" value="${ipInterface.ipAddressAsString}"/>
                </c:if>
              </c:forEach>
            </c:url>
            <c:url var="snmpinterfaceLink" value="element/snmpinterface.jsp">
              <c:param name="node" value="${snmpInterface.node.id}"/>
              <c:param name="ifindex" value="${snmpInterface.ifIndex}"/>
            </c:url>
            <li>
              <c:if test="${isMaclikeSearch && snmpInterface.physAddr!=null && snmpInterface.physAddr!=''}">
                <c:set var="notFound" value="true"/>
                <c:forEach var="ipInterface" items="${nodeModel.node.ipInterfaces}">
                  <c:if test="${ipInterface.snmpInterface.id == snmpInterface.id}">
                    <a href="element/interface.jsp?ipinterfaceid=${ipInterface.id}">${ipInterface.ipAddressAsString}</a> : ${snmpInterface.physAddr} (from snmp)
                    <c:remove var="notFound"/>
                  </c:if>
                </c:forEach>
                <c:if test="${notFound}">
                  <a href="${snmpinterfaceLink}">${snmpInterface.ifName}</a> : ${snmpInterface.physAddr} (from snmp)
                </c:if>
              </c:if>
              <c:choose>
                <c:when test="${snmpParm == ('ifAlias')}">
                  <c:set var="notFound" value="true"/>
                  <c:forEach var="ipInterface" items="${nodeModel.node.ipInterfaces}">
                    <c:if test="${ipInterface.snmpInterface.id == snmpInterface.id}">
                      <a href="${interfaceLink}">${ipInterface.ipAddressAsString}</a> : ${snmpInterface.ifAlias}
                      <c:remove var="notFound"/>
                    </c:if>
                  </c:forEach>
                  <c:if test="${notFound}">
                      <c:choose>
                        <c:when test="${snmpInterface.ifName != null}">
                          <a href="${snmpinterfaceLink}">${snmpInterface.ifName}</a> : ${snmpInterface.ifAlias}
                        </c:when>   
                        <c:when test="${snmpInterface.ifDescr != null}">
                          <a href="${snmpinterfaceLink}">${snmpInterface.ifDescr}</a> : ${snmpInterface.ifAlias}
                        </c:when>
                        <c:otherwise>
                          <a href="${snmpinterfaceLink}">ifIndex ${snmpInterface.ifIndex}</a> : ${snmpInterface.ifAlias}
                        </c:otherwise>
                      </c:choose>
                  </c:if>
                </c:when>        
                <c:when test="${snmpParm == ('ifName')}">
                  <c:set var="notFound" value="true"/>
                  <c:forEach var="ipInterface" items="${nodeModel.node.ipInterfaces}">
                    <c:if test="${ipInterface.snmpInterface.id == snmpInterface.id}">
                      <a href="${interfaceLink}">${ipInterface.ipAddressAsString}</a> : ${snmpInterface.ifName}
                      <c:remove var="notFound"/>
                    </c:if>
                  </c:forEach>
                  <c:if test="${notFound}">
                      <c:choose>
                        <c:when test="${snmpInterface.ifName != null}">
                          <a href="${snmpinterfaceLink}">${snmpInterface.ifName}</a>
                        </c:when>
                      </c:choose>
                  </c:if>
                </c:when>   
                <c:when test="${snmpParm == ('ifDescr')}">
                  <c:set var="notFound" value="true"/>
                  <c:forEach var="ipInterface" items="${nodeModel.node.ipInterfaces}">
                    <c:if test="${ipInterface.snmpInterface.id == snmpInterface.id}">
                      <a href="${interfaceLink}">${ipInterface.ipAddressAsString}</a> : ${snmpInterface.ifDescr}
                      <c:remove var="notFound"/>
                    </c:if>
                  </c:forEach>
                  <c:if test="${notFound}">
                      <c:choose>
                        <c:when test="${snmpInterface.ifName != null}">
                          <a href="${snmpinterfaceLink}">${snmpInterface.ifName}</a> : ${snmpInterface.ifDescr}
                        </c:when>                  
                        <c:when test="${snmpInterface.ifDescr != null}">
                          <a href="${snmpinterfaceLink}">${snmpInterface.ifDescr}</a>
                        </c:when>
                      </c:choose>
                  </c:if>
                </c:when>
              </c:choose>
            </li>
          </c:forEach>
        </c:if>
      </ul>
    </li>
  </c:forEach>
</ul>
