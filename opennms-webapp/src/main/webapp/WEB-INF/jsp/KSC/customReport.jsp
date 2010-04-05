<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
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
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
--%>

<%@page language="java"
  contentType="text/html"
  session="true"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="headTitle" value="Performance" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="headTitle" value="KSC" />
  <jsp:param name="location" value="KSC Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='KSC/index.htm'>KSC Reports</a>" />
  <jsp:param name="breadcrumb" value="Custom Report" />
</jsp:include>


<%-- A script to Save the file --%>
<script type="text/javascript">
    function saveReport()
    {
        document.customize_form.action.value = "Save"; 
        document.customize_form.submit();
    }
 
    function addNewGraph()
    {
        document.customize_form.action.value = "AddGraph"; 
        document.customize_form.submit();
    }
 
    function modifyGraph(graph_index)
    {
        document.customize_form.action.value = "ModGraph"; 
        document.customize_form.graph_index.value = graph_index; 
        document.customize_form.submit();
    }
 
    function deleteGraph(graph_index)
    {
        document.customize_form.action.value = "DelGraph";
        document.customize_form.graph_index.value = graph_index; 
        document.customize_form.submit();
    }
 
    function cancelReport()
    {
        var fer_sure = confirm("Do you really want to cancel configuration changes?");
        if (fer_sure==true) {
            setLocation("index.jsp");
        }
    }
    
</script>


<h3>Customized Report Configuration</h3>
<div class="boxWrapper">
    <form name="customize_form" method="get" action="KSC/formProcReport.htm">
        <input type=hidden name="action" value="none">
        <input type=hidden name="graph_index" value="-1">

        <p>
          Title: 
          <input type="text" name="report_title" value="${title}" size="80" maxlength="80"/>
        </p>

            <table class="normal" width="100%" border="2">
              <c:if test="${fn:length(resultSets) > 0}">
                <c:forEach var="graphNum" begin="0" end="${fn:length(resultSets) - 1}">
                  <c:set var="resultSet" value="${resultSets[graphNum]}"/>
                    <tr>
                        <td>
                            <input type="button" value="Modify" onclick="modifyGraph(${graphNum})">
                            <br/>
                            <input type="button" value="Delete" onclick="deleteGraph(${graphNum})">
                        </td>
                        <td align="right">
                            ${resultSet.title}
                              <br/>
                              <c:if test="${!empty resultSet.resource.parent}">
                                ${resultSet.resource.parent.resourceType.label}:
                                <c:choose>
                                  <c:when test="${!empty resultSet.resource.parent.link}">
                                    <a href="<c:url value='${resultSet.resource.parent.link}'/>">${resultSet.resource.parent.label}</a>
                                  </c:when>
                                  <c:otherwise>
                                    ${resultSet.resource.parent.label}
                                  </c:otherwise>
                                </c:choose>
                                <br />
                              </c:if>
                          
                              ${resultSet.resource.resourceType.label}:
                              <c:choose>
                                <c:when test="${!empty resultSet.resource.link}">
                                  <a href="<c:url value='${resultSet.resource.link}'/>">${resultSet.resource.label}</a>
                                </c:when>
                                <c:otherwise>
                                  ${resultSet.resource.label}
                                </c:otherwise>
                              </c:choose>

                            <br/>
                            <br/>
                            From: ${resultSet.start}
                            <br/>
                            To: ${resultSet.end}
                        </td>
              
                        <td align="left">
                          <c:url var="graphUrl" value="graph/graph.png">
                            <c:param name="resourceId" value="${resultSet.resource.id}"/>
                            <c:param name="report" value="${resultSet.prefabGraph.name}"/>
                            <c:param name="start" value="${resultSet.start.time}"/>
                            <c:param name="end" value="${resultSet.end.time}"/>
                            <c:param name="zoom" value="true"/>
                          </c:url>
                        
                          <img src="${graphUrl}" alt="Resource graph: ${resultSet.prefabGraph.title}" />
                        </td>
                    </tr>
                  </c:forEach>
                </c:if>
            </table>  

        <p>
            <input type="button" value="Add New Graph" onclick="addNewGraph()" alt="Add a new graph to the Report"/>
        </p>

        <table class="normal">
             <tr>
                 <td class="normal">
                     <c:choose>
                       <c:when test="${showTimeSpan}">
                         <c:set var="checked" value="checked"/>
                       </c:when>
                       
                       <c:otherwise>
                         <c:set var="checked" value=""/>
                       </c:otherwise>
                     </c:choose>
                     <input type="checkbox" name="show_timespan" ${checked} />
                 </td>
                 <td class="normal">
                     Show Timespan Button (allows global manipulation of report timespan)
                 </td>
             </tr>
             <tr>
                 <td class="normal">
                     <c:choose>
                       <c:when test="${showGraphType}">
                         <c:set var="checked" value="checked"/>
                       </c:when>
                       
                       <c:otherwise>
                         <c:set var="checked" value=""/>
                       </c:otherwise>
                     </c:choose>
                     <input type="checkbox" name="show_graphtype" ${checked} />
                 </td>
                 <td class="normal">
                     Show Graphtype Button (allows global manipulation of report prefabricated graph type)
                 </td>
             </tr>
             <tr>
                 <td class="normal">
                        <select name="graphs_per_line">
                           <c:choose>
                             <c:when test="${graphsPerLine == 0}">
                                <option selected value="0">default</option>
                             </c:when>
                             <c:otherwise>
                                <option value="0">default</option>
                             </c:otherwise>
                           </c:choose>
                           <c:choose>
                             <c:when test="${graphsPerLine == 1}">
                                <option selected value="1">1</option>
                             </c:when>
                             <c:otherwise>
                                <option value="1">1</option>
                             </c:otherwise>
                           </c:choose>
                           <c:choose>
                             <c:when test="${graphsPerLine == 2}">
                                <option selected value="2">2</option>
                             </c:when>
                             <c:otherwise>
                                <option value="2">2</option>
                             </c:otherwise>
                           </c:choose>
                           <c:choose>
                             <c:when test="${graphsPerLine == 3}">
                                <option selected value="3">3</option>
                             </c:when>
                             <c:otherwise>
                                <option value="3">3</option>
                             </c:otherwise>
                           </c:choose>
                           <c:choose>
                             <c:when test="${graphsPerLine == 4}">
                                <option selected value="4">4</option>
                             </c:when>
                             <c:otherwise>
                                <option value="4">4</option>
                             </c:otherwise>
                           </c:choose>
                           <c:choose>
                             <c:when test="${graphsPerLine == 5}">
                                <option selected value="5">5</option>
                             </c:when>
                             <c:otherwise>
                                <option value="5">5</option>
                             </c:otherwise>
                           </c:choose>
                           <c:choose>
                             <c:when test="${graphsPerLine == 6}">
                                <option selected value="6">6</option>
                             </c:when>
                             <c:otherwise>
                                <option value="6">6</option>
                             </c:otherwise>
                           </c:choose>
                        </select>
                 </td>
                 <td class="normal">
                     Number of graphs to show per line in the report.
                 </td>
             </tr>

        </table> 

        <p>
                <input type="submit" value="Save" onclick="saveReport()" alt="Save the Report to File"/>
                <input type="button" value="Cancel" onclick="cancelReport()" alt="Cancel the report configuration"/>
        </p>

      <p>
              If you make any changes, please make sure to save the report
              when you are done.  Changes will only be saved by using the
              "Save" button on this page.
      </p>

    </form>
</div>

<jsp:include page="/includes/footer.jsp" flush="false"/>
