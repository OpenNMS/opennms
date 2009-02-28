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
// 2009 Feb 27: Add substring match capability to resource graphs and ksc reports. ayres@opennms.org
// 2009 Feb 03: Rename showReportList to kscReadOnly. - jeffg@opennms.org
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
  import="
  org.opennms.web.XssRequestWrapper
  "
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
    HttpServletRequest req = new XssRequestWrapper(request);
    String match = req.getParameter("match");
    pageContext.setAttribute("match", match);
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="headTitle" value="Performance" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="headTitle" value="KSC" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="KSC Reports" />
</jsp:include>

<!-- A script for validating Node ID Selection Form before submittal -->
<script language="Javascript" type="text/javascript" >
  function validateNode()
  {
      var isChecked = false
      for( i = 0; i < document.choose_node.report.length; i++ )
      {
         //make sure something is checked before proceeding
         if (document.choose_node.report[i].selected)
         {
            isChecked=true;
         }
      }

      if (!isChecked)
      {
          alert("Please check the node that you would like to report on.");
      }
      return isChecked;
  }

  function validateDomain()
  {
      var isChecked = false
      for( i = 0; i < document.choose_domain.domain.length; i++ )
      {
         //make sure something is checked before proceeding
         if (document.choose_domain.domain[i].selected)
         {
            isChecked=true;
         }
      }

      if (!isChecked)
      {
          alert("Please check the domain that you would like to report on.");
      }
      return isChecked;
  }

  function submitNodeForm()
  {
      if (validateNode())
      {
          document.choose_node.submit();
      }
  }

  function submitDomainForm()
  {
      if (validateDomain())
      {
          document.choose_domain.submit();
      }
  }
</script>


<%-- A script for validating Custom Report Form before submittal --%>
<script language="Javascript" type="text/javascript" >
  function validateReport()
  {
      var isChecked = false
      for( i = 0; i < document.choose_report.report.length; i++ )
      {
         //make sure something is checked before proceeding
         if (document.choose_report.report[i].selected)
         {
            isChecked=true;
         }
      }

      if (!isChecked)
      {
          alert("No reports selected.  Please click on a report title to make a report selection.");
      }
      return isChecked;
  }

  function submitReportForm()
  {
      // Create New Action (Don't need to validate select list if adding new report) 
      if (document.choose_report.report_action[2].checked == true) {
          document.choose_report.submit();
      }
      else {
          if (validateReport()) {
              // Delete Action
              if (document.choose_report.report_action[4].checked == true) {
                  var fer_sure=confirm("Are you sure you wish to delete this report?")
                  if (fer_sure==true) {
                      document.choose_report.submit();
                  }
              }
              else {
                  // View, Customize, or CreateFrom Action 
                  document.choose_report.submit();
              }
          }
      }
  }

  function submitReadOnlyView()
  {
      if (validateReport()) {
          document.choose_report.submit();
      }
  }
  
</script>

<div class="TwoColLeft">
 
  <h3>Customized Reports</h3>

  <div class="boxWrapper">
  <p>Choose the custom report title to view or modify from the list below. There are ${fn:length(reports)} custom reports to select from.</p>

      <form method="get" name="choose_report" action="KSC/formProcMain.htm">
                  <select style="width: 100%;" name="report" size="10">
                    <c:forEach var="report" items="${reports}">
                      <c:choose>
                        <c:when test="${match == null || match == ''}">
                          <option value="${report.key}">${report.value}</option>
                        </c:when>
                        <c:otherwise>
                          <c:if test="${fn:containsIgnoreCase(report.value,match)}">
                            <option value="${report.key}">${report.value}</option>
                          </c:if>
                        </c:otherwise>  
                      </c:choose>
                    </c:forEach>
                  </select>

 <p>
                  <c:choose>
                    <c:when test="${kscReadOnly == false }">
                      <input type="radio" name="report_action" value="View" checked /> View <br>
                      <input type="radio" name="report_action" value="Customize" /> Customize <br/>
                      <input type="radio" name="report_action" value="Create" /> Create New <br/>
                      <input type="radio" name="report_action" value="CreateFrom" /> Create New From Existing <br/>
                      <input type="radio" name="report_action" value="Delete" /> Delete <br/>
                      <input type="button" value="Submit" onclick="submitReportForm()" alt="Initiates Action for Custom Report"/>
                    </c:when>
                    <c:otherwise>
                      <input type="hidden" name="report_action" value="View">
                      <input type="button" value="Submit" onclick="submitReadOnlyView()" alt="Initiates Action for Custom Report"/>
                    </c:otherwise>
                  </c:choose>

 </p>
      </form>
  </div>

<h3>Node SNMP Interface Reports</h3>
<div class="boxWrapper">
      <p>Select node for desired performance report</p>
      <form method="get" name="choose_node" action="KSC/customView.htm">
        <input type="hidden" name="type" value="node">
              <select style="width: 100%;" name="report" size="10">
                <c:forEach var="resource" items="${nodeResources}">
                  <c:choose>
                    <c:when test="${match == null || match == ''}">
                      <option value="${resource.name}">${resource.label}</option>
                    </c:when>
                    <c:otherwise>
                      <c:if test="${fn:containsIgnoreCase(resource.label,match)}">
                        <option value="${resource.name}">${resource.label}</option>
                      </c:if>
                    </c:otherwise>  
                  </c:choose>
                </c:forEach>
              </select>

              <input type="button" value="Submit" onclick="submitNodeForm()" alt="Initiates Generation of Node Report"/>
      </form>
</div>

<h3>Domain SNMP Interface Reports</h3>
<div class="boxWrapper">
      <c:choose>
        <c:when test="${empty domainResources}">
          <p>No data has been collected by domain</p>
        </c:when>

        <c:otherwise>
          <p>Select domain for desired performance report</p>
          <form method="get" name="choose_domain" action="KSC/customView.htm" >
            <input type="hidden" name="type" value="domain">

                  <select style="width: 100%;" name="domain" size="10">
                    <c:forEach var="resource" items="${domainResources}">
                      <c:choose>
                        <c:when test="${match == null || match == ''}">
                          <option value="${resource.name}">${resource.label}</option>
                        </c:when>
                        <c:otherwise>
                          <c:if test="${fn:containsIgnoreCase(resource.label,match)}">
                            <option value="${resource.name}">${resource.label}</option>
                          </c:if>
                        </c:otherwise>  
                      </c:choose>
                    </c:forEach>
                  </select>

                  <input type="button" value="Submit" onclick="submitDomainForm()" alt="Initiates Generation of Domain Report"/>
          </form>
        </c:otherwise>
      </c:choose>
  </div>

</div>

<div class="TwoColRight">
  <h3>Descriptions</h3>

  <div class="boxWrapper">
    <p>
      <b>Customized Reports</b>
      <c:choose>
        <c:when test="${kscReadOnly == false }">
          allows users to create, view, and edit customized reports containing
          any number of prefabricated reports from any available graphable
          resource.
        </c:when>
        <c:otherwise>
          allows users to view customized reports containing any number of
          prefabricated reports from any available graphable resource.
        </c:otherwise>
      </c:choose>
    </p>

    <p>
      <b>Node and Domain Reports</b>
      <c:choose>
        <c:when test="${kscReadOnly == false }">
          allows users to view automatically generated reports for any node or
          domain.  These reports can be further edited and saved just like other
          customized reports.  These reports list only the SNMP interfaces on
          the selected node or domain, but they can be customized to include
          any graphable resource.
        </c:when>
        <c:otherwise>
          allows users to view automatically generated reports for any node or
          domain.  
        </c:otherwise>
      </c:choose>

    </p>
  </div>
</div>

<jsp:include page="/includes/footer.jsp" flush="false"/>
