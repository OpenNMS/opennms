<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java"
	contentType="text/html"
	session="true"
%>

<%@page import="org.opennms.util.ilr.Collector"%>
<%@page import="java.io.*"%>
<%@page import="org.slf4j.*"%>
<%@page import="org.opennms.web.element.NetworkElementFactory"%>
<%@ page import="org.opennms.util.ilr.ServiceCollector" %>
<%@ page import="org.opennms.web.api.Util" %>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Admin")
          .headTitle("Instrumentation Log Reader")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Instrumentation Log Reader")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<%

final Logger LOG = LoggerFactory.getLogger("instrumentationLogReader.jsp");

String opennmsHome = System.getProperty("opennms.home");

Collector c = new Collector();
String searchString = request.getParameter("searchString");
String sortColumn = request.getParameter("sortColumn");
String sortOrder = request.getParameter("sortOrder");
if(sortColumn!=null){
	c.setSortColumn(Collector.SortColumn.valueOf(sortColumn));
}

if(sortOrder!=null) {
	c.setSortOrder(Collector.SortOrder.valueOf(sortOrder));
}

if(searchString != null) {
	c.setSearchString(searchString);
}

LOG.debug("creating FilenameFilter");

final FilenameFilter filter = new FilenameFilter() {
	public boolean accept(final File dir, final String name) {
		return name.startsWith("instrumentation.log");
	}
};

LOG.debug("FilenameFilter = {}", filter);

int filesMatched = 0;

LOG.debug("scanning LogDir");

final File logDir = new File(opennmsHome, "logs");
if (logDir.exists()) {
	for (final File file : logDir.listFiles(filter)) {
		if (file.length() == 0) continue;
		c.readLogMessagesFromFile(file.getPath());
		filesMatched++;
	}
}

pageContext.setAttribute("filesMatched",filesMatched);
pageContext.setAttribute("collector",c);
pageContext.setAttribute("OpennmsHome",opennmsHome);
pageContext.setAttribute("sortColumn", sortColumn);
pageContext.setAttribute("sortOrder", sortOrder);
pageContext.setAttribute("searchString", searchString==null?"":Util.encode(searchString));
%>

<c:set var="nan" value="<%=java.lang.Double.NaN%>"/>

<c:choose>
	<c:when test="${filesMatched == 0}">
		<script type="text/javascript">
			alert ("instrumentation.log either does not exist or is empty. Check to see if you have it set to INFO in log4j2.xml")
		</script>
	</c:when>
</c:choose>

<div class="row">
  <div class="col-md-4">
    <div class="card">
      <div class="card-header">
        <span>Instrumentation Log Statistics</span>
      </div>
      <div class="card-body">
        <p>
        Start time: ${collector.startTime == null ? "N/A" : collector.startTime}
        </p>
        <p>
        End time: ${collector.endTime == null ? "N/A" : collector.endTime}
        </p>
        <p>
        Duration: ${collector.formattedDuration}
        </p>
        <p>
        Total Services: ${collector.serviceCount}
        </p>
        <p>
        Threads Used: ${collector.threadCount}
        </p>
        
        <c:if test="${collector.startTime == null && collector.endTime == null}">
        <p>
        No service collector data is available. Be sure that the <strong>Collectd</strong> and
        <strong>Instrumentation</strong> appenders are set to log at <strong>INFO</strong> in
        the <em>log4j2.xml</em> configuration file.
        </p>
        </c:if>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-md-4">
    <div class="card">
      <div class="card-header">
        <span>Filtering</span>
      </div>
      <div class="card-body">
        <form id="ILRfilter" action="admin/nodemanagement/instrumentationLogReader.jsp" method="get" style="display:inline">
          <input type="text" class="form-control" name="searchString" size="15" value="<%=searchString==null?"":WebSecurityUtils.sanitizeString(searchString)%>"/>
          <button type="submit" class="btn btn-secondary mt-1">Submit</button>
        </form>
        <form id="ILRreset" action="admin/nodemanagement/instrumentationLogReader.jsp" method="get" style="display:inline">
          <input type="hidden" name="searchString" value=""/>
          <button type="submit" class="btn btn-secondary mt-1">Reset</button>
        </form>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-md-12">
    <table class="table table-sm table-striped">
      <tr>
      <th>Service</th>
      <c:choose>
      	<c:when test="${sortColumn == 'TOTALCOLLECTS' && sortOrder == 'DESCENDING'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=TOTALCOLLECTS&sortOrder=ASCENDING&&searchString=${searchString}">Collections ^</a></th>
      	</c:when>
      	<c:when test="${sortColumn != 'TOTALCOLLECTS'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=TOTALCOLLECTS&sortOrder=DESCENDING&&searchString=${searchString}">Collections</a></th>
      	</c:when>
      	<c:otherwise>
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=TOTALCOLLECTS&sortOrder=DESCENDING&&searchString=${searchString}">Collections v</a></th>
      	</c:otherwise>
      </c:choose>
  
      <c:choose>
      	<c:when test="${sortColumn == 'AVGCOLLECTTIME' && sortOrder == 'DESCENDING'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=AVGCOLLECTTIME&sortOrder=ASCENDING&&searchString=${searchString}">Average Collection Time ^</a></th>
      	</c:when>
      	<c:when test="${sortColumn != 'AVGCOLLECTTIME'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=AVGCOLLECTTIME&sortOrder=DESCENDING&&searchString=${searchString}">Average Collection Time</a></th>
      	</c:when>
      	<c:otherwise>
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=AVGCOLLECTTIME&sortOrder=DESCENDING&&searchString=${searchString}">Average Collection Time v</a></th>
      	</c:otherwise>	
      </c:choose>
  
      <c:choose>
      	<c:when test="${sortColumn == 'AVGTIMEBETWEENCOLLECTS' && sortOrder == 'DESCENDING'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=AVGTIMEBETWEENCOLLECTS&sortOrder=ASCENDING&&searchString=${searchString}">Average Time Between Collections ^</a></th>
      	</c:when>
      	<c:when test="${sortColumn != 'AVGTIMEBETWEENCOLLECTS'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=AVGTIMEBETWEENCOLLECTS&sortOrder=DESCENDING&&searchString=${searchString}">Average Time Between Collections</a></th>
      	</c:when>
      	<c:otherwise>
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=AVGTIMEBETWEENCOLLECTS&sortOrder=DESCENDING&&searchString=${searchString}">Average Time Between Collections v</a></th>
      	</c:otherwise>
      </c:choose>
  
      <c:choose>
      	<c:when test="${sortColumn == 'TOTALSUCCESSCOLLECTS' && sortOrder == 'DESCENDING'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=TOTALSUCCESSCOLLECTS&sortOrder=ASCENDING&&searchString=${searchString}">Successful Collections ^</a></th>
      	</c:when>
      	<c:when test="${sortColumn != 'TOTALSUCCESSCOLLECTS'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=TOTALSUCCESSCOLLECTS&sortOrder=DESCENDING&&searchString=${searchString}">Successful Collections</a></th>
      	</c:when>
      	<c:otherwise>
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=TOTALSUCCESSCOLLECTS&sortOrder=DESCENDING&&searchString=${searchString}">Successful Collections v</a></th>
      	</c:otherwise>
      </c:choose>
  
      <c:choose>
      	<c:when test="${sortColumn == 'SUCCESSPERCENTAGE' && sortOrder == 'DESCENDING'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=SUCCESSPERCENTAGE&sortOrder=ASCENDING&&searchString=${searchString}">Successful Percentage ^</a></th>
      	</c:when>
      	<c:when test="${sortColumn != 'SUCCESSPERCENTAGE'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=SUCCESSPERCENTAGE&sortOrder=DESCENDING&&searchString=${searchString}">Successful Percentage</a></th>
      	</c:when>
      	<c:otherwise>
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=SUCCESSPERCENTAGE&sortOrder=DESCENDING&&searchString=${searchString}">Successful Percentage v</a></th>
      	</c:otherwise>
      </c:choose>
  
      <c:choose>
      	<c:when test="${sortColumn == 'AVGSUCCESSCOLLECTTIME' && sortOrder == 'DESCENDING'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=AVGSUCCESSCOLLECTTIME&sortOrder=ASCENDING&&searchString=${searchString}">Average Successful Collection Time ^</a></th>
      	</c:when>
      	<c:when test="${sortColumn !='AVGSUCCESSCOLLECTTIME'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=AVGSUCCESSCOLLECTTIME&sortOrder=DESCENDING&&searchString=${searchString}">Average Successful Collection Time</a></th>
      	</c:when>
      	<c:otherwise>
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=AVGSUCCESSCOLLECTTIME&sortOrder=DESCENDING&&searchString=${searchString}">Average Successful Collection Time v</a></th>
      	</c:otherwise>
      </c:choose>
  
      <c:choose>
      	<c:when test="${sortColumn == 'TOTALUNSUCCESSCOLLECTS' && sortOrder == 'DESCENDING'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=TOTALUNSUCCESSCOLLECTS&sortOrder=ASCENDING&&searchString=${searchString}">Unsuccessful Collections ^</a></th>
      	</c:when>
      	<c:when test="${sortColumn != 'TOTALUNSUCCESSCOLLECTS'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=TOTALUNSUCCESSCOLLECTS&sortOrder=DESCENDING&&searchString=${searchString}">Unsuccessful Collections</a></th>
      	</c:when>
      	<c:otherwise>
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=TOTALUNSUCCESSCOLLECTS&sortOrder=DESCENDING&&searchString=${searchString}">Unsuccessful Collections v</a></th>
      	</c:otherwise>
      </c:choose>	
  
      <c:choose>	
      	<c:when test="${sortColumn == 'UNSUCCESSPERCENTAGE' && sortOrder == 'DESCENDING'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=UNSUCCESSPERCENTAGE&sortOrder=ASCENDING&&searchString=${searchString}">Unsuccessful Percentage ^</a></th>
      	</c:when>
      	<c:when test="${sortColumn != 'UNSUCCESSPERCENTAGE'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=UNSUCCESSPERCENTAGE&sortOrder=DESCENDING&&searchString=${searchString}">Unsuccessful Percentage</a></th>
      	</c:when>
      	<c:otherwise>
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=UNSUCCESSPERCENTAGE&sortOrder=DESCENDING&&searchString=${searchString}">Unsuccessful Percentage v</a></th>
      	</c:otherwise>
      </c:choose>
  
      <c:choose>
      	<c:when test="${sortColumn == 'AVGUNSUCCESSCOLLECTTIME' && sortOrder == 'DESCENDING'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=AVGUNSUCCESSCOLLECTTIME&sortOrder=ASCENDING&&searchString=${searchString}">Average Unsuccessful Collection Time ^</a></th>
      	</c:when>
      	<c:when test="${sortColumn != 'AVGUNSUCCESSCOLLECTTIME'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=AVGUNSUCCESSCOLLECTTIME&sortOrder=DESCENDING&&searchString=${searchString}">Average Unsuccessful Collection Time</a></th>
      	</c:when>
      	<c:otherwise>
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=AVGUNSUCCESSCOLLECTTIME&sortOrder=DESCENDING&&searchString=${searchString}">Average Unsuccessful Collection Time v</a></th>
      	</c:otherwise>
      </c:choose>
  
      <c:choose>
      	<c:when test="${sortColumn == 'AVERAGEPERSISTTIME' && sortOrder == 'DESCENDING'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=AVERAGEPERSISTTIME&sortOrder=ASCENDING&&searchString=${searchString}">Average Persistence Time ^</a></th>
      	</c:when>
      	<c:when test="${sortColumn != 'AVERAGEPERSISTTIME'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=AVERAGEPERSISTTIME&sortOrder=DESCENDING&&searchString=${searchString}">Average Persistence Time</a></th>
      	</c:when>
      	<c:otherwise>
      	<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=AVERAGEPERSISTTIME&sortOrder=DESCENDING&&searchString=${searchString}">Average Persistence Time v</a></th>
      	</c:otherwise>
      </c:choose>
  
      <c:choose>
      	<c:when test="${sortColumn == 'TOTALPERSISTTIME' && sortOrder == 'DESCENDING'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=TOTALPERSISTTIME&sortOrder=ASCENDING&&searchString=${searchString}">Total Persistence Time ^</a></th>
      	</c:when>
      	<c:when test="${sortColumn != 'TOTALPERSISTTIME'}">
      		<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=TOTALPERSISTTIME&sortOrder=DESCENDING&&searchString=${searchString}">Total Persistence Time</a></th>
      	</c:when>
      	<c:otherwise>
      	<th><a href="admin/nodemanagement/instrumentationLogReader.jsp?sortColumn=TOTALPERSISTTIME&sortOrder=DESCENDING&&searchString=${searchString}">Total Persistence Time v</a></th>
      	</c:otherwise>
      </c:choose>
      </tr>
  
      <c:forEach var="svcCollector" items="${collector.serviceCollectors}">
      <c:choose>
          <c:when test="${svcCollector.successPercentage > 50}" >
              <tr class="Normal">
          </c:when>
          <c:when test="${svcCollector.successPercentage == -1}" >
              <tr class="Warning">
          </c:when>
          <c:otherwise>
              <tr class="Critical">
          </c:otherwise>
      </c:choose>
		  <%
			  String nodeLabel = null;
			  final ServiceCollector svcCollector = (ServiceCollector) pageContext.getAttribute("svcCollector");
			  try {
				  final int nodeId = Integer.parseInt(svcCollector.getParsedServiceID());
				  nodeLabel = NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(nodeId);
				  if (nodeLabel == null) {
					  nodeLabel = "No label found for nodeId " + nodeId;
				  }
			  } catch (NumberFormatException e) {
				  nodeLabel = "Error parsing nodeId";
			  }
		  %>
      <td><a href="element/node.jsp?node=${svcCollector.parsedServiceID}"><%=nodeLabel%><br/>${svcCollector.serviceID}</a></td>
      <td align="right">${svcCollector.collectionCount}</td>
      <td align="right">${svcCollector.averageCollectionDuration}</td>
      <td align="right">${svcCollector.averageDurationBetweenCollections}</td>
      <td align="right">${svcCollector.successfulCollectionCount}</td>
      <c:choose>
          <c:when test="${svcCollector.successPercentage == -1}" >
              <td align="right">No Collections</td>
          </c:when>
          <c:otherwise>
              <td align="right"><fmt:formatNumber type="number" maxFractionDigits="1" minFractionDigits="1" value="${svcCollector.successPercentage}" /></td>
          </c:otherwise>
      </c:choose>
      <td align="right">${svcCollector.averageSuccessfulCollectionDuration}</td>
      <td align="right">${svcCollector.errorCollectionCount}</td>
      <c:choose>	
      	<c:when test="${svcCollector.errorPercentage == -1}">
      		<td align="right">No Collections</td>
      	</c:when>
      	<c:otherwise>
      		<td align="right"><fmt:formatNumber type="number" maxFractionDigits="1" minFractionDigits="1" value="${svcCollector.errorPercentage}" /></td>
      	</c:otherwise>
      </c:choose>
      <td align="right">${svcCollector.averageErrorCollectionDuration}</td>
      <td align="right">${svcCollector.averagePersistDuration}</td>
      <td align="right">${svcCollector.totalPersistDuration}</td>
      </tr>
  
      </c:forEach>
    </table>
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
