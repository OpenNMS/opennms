<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Distributed Status History" />
	<jsp:param name="headTitle" value="Distributed Status History" />
	<jsp:param name="breadcrumb" value="<a href=\"distributedStatusSummary.htm\">Distributed Status</a>" />
	<jsp:param name="breadcrumb" value="History" />
</jsp:include>

<h3>Distributed Status History for <c:out value="${historyModel.chosenApplication.name}"/> from <c:out value="${historyModel.chosenMonitor.definitionName}-${historyModel.chosenMonitor.id}"/> over <c:out value="${historyModel.chosenPeriod.name}"/></h3>

<c:if test="${!empty historyModel.errors}">
  <p style="color: red;">
    <c:forEach items="${historyModel.errors}" var="error">
      <c:out value="${error}"/><br/>
    </c:forEach>
  </p>
</c:if>

<table class="normal">
  <form action="distributedStatusHistory.htm">
  <input type="hidden" name="previousLocation" value="<c:out value="${historyModel.chosenLocation.name}"/>"/>
  
  <tr>
    <td class="normal" align="right">Location</td>
    <td class="normal">
      <select name="location" id="location">
        <c:forEach items="${historyModel.locations}" var="location">
          <c:choose>
            <c:when test="${location.name == historyModel.chosenLocation.name}">
              <option selected="selected"><c:out value="${location.name}"/></option>
            </c:when>
            <c:otherwise>
              <option><c:out value="${location.name}"/></option>
            </c:otherwise>
          </c:choose>
        </c:forEach>
      </select>
	</td>
  </tr>

  <tr>
    <td class="normal" align="right">Location monitor</td>
    <td class="normal">
      <select name="monitorId" id="monitor">
        <c:forEach items="${historyModel.monitors}" var="monitor">
          <c:choose>
            <c:when test="${monitor.id == historyModel.chosenMonitor.id}">
              <option value="<c:out value="${monitor.id}"/>" selected="selected"><c:out value="${monitor.definitionName}-${monitor.id}"/></option>
            </c:when>
            <c:otherwise>
              <option value="<c:out value="${monitor.id}"/>"><c:out value="${monitor.definitionName}-${monitor.id}"/></option>
            </c:otherwise>
          </c:choose>
        </c:forEach>
      </select>
    </td> 
  </tr>

  <tr>
    <td class="normal" align="right">Application</td>
    <td class="normal">
      <select name="application" id="application">
        <c:forEach items="${historyModel.applications}" var="application">
          <c:choose>
            <c:when test="${application == historyModel.chosenApplication}">
              <option selected="selected"><c:out value="${application.name}"/></option>
            </c:when>
            <c:otherwise>
              <option><c:out value="${application.name}"/></option>
            </c:otherwise>
          </c:choose>
        </c:forEach>
      </select>
	</td>
  </tr>

  <tr>
    <td class="normal" align="right">Time Span</td>
	<td class="normal">
	  <select name="timeSpan" id="timeSpan">
	    <c:forEach items="${historyModel.periods}" var="period">
          <c:choose>
            <c:when test="${period == historyModel.chosenPeriod}">
              <option value="<c:out value="${period.id}"/>" selected="selected"><c:out value="${period.name}"/></option>
            </c:when>
            <c:otherwise>
		      <option value="<c:out value="${period.id}"/>"><c:out value="${period.name}"/></option>
            </c:otherwise>
          </c:choose>
		</c:forEach>
      </select>
    </td>
  </tr>

  <tr>
    <td class="normal"></td>
    <td class="normal">
      <input type="submit" value="Update"/>
    </td>
  </tr>

  </form>
  
  <form action="distributedStatusDetails.htm">
  <input type="hidden" name="location" value="<c:out value="${historyModel.chosenLocation.name}"/>"/>
  <input type="hidden" name="application" value="<c:out value="${historyModel.chosenApplication.name}"/>"/>

  <tr>
    <td class="normal"></td>
    <td class="normal">
      <input type="submit" value="View Status Details"/>
    </td>
  </tr>


  </form>
  
</table>

<c:forEach items="${historyModel.httpGraphUrls}" var="url">
  <p style="text-align: center">
    Node: <a href="<c:url value="element/node.jsp?node=${url.key.ipInterface.node.id}"/>"><c:out value="${url.key.ipInterface.node.label}"/></a><br/>
    Interface: <a href="<c:url value="element/interface.jsp?ipinterfaceid=${url.key.ipInterface.id}"/>"><c:out value="${url.key.ipAddress}"/></a><br/>
    Service: <a href="<c:url value="element/service.jsp?ifserviceid=${url.key.id}"/>"><c:out value="${url.key.serviceName}"/></a><br/>
    <img src="<c:out value="${url.value}"/>"/>
  </p>
</c:forEach>

<jsp:include page="/includes/footer.jsp" flush="false"/>
