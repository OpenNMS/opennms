<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//

--%>

<%--
  This page is included by other JSPs to create a box containing an
  entry to the resource graph reporting system.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
        contentType="text/html"
        session="true"
        import="
        org.opennms.web.svclayer.ResourceService,org.springframework.web.context.WebApplicationContext,org.springframework.web.context.support.WebApplicationContextUtils"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%!

    public ResourceService m_resourceService;


	public void init() throws ServletException {
	    WebApplicationContext m_webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        m_resourceService = (ResourceService) m_webAppContext.getBean("resourceService", ResourceService.class);
    }
%>

<%
    pageContext.setAttribute("resources", m_resourceService.findNodeResources());
%>
<script type="text/javascript" src="js/opennms/ux/ComboFilterBox.js" ></script>
<script type="text/javascript" src="js/opennms/ux/NodePageableComboBox.js" ></script>
<script type="text/javascript">
  Ext.onReady(function(){
	  var combo = new OpenNMS.ux.NodePageableComboBox({
		text:'hello',
		renderTo:'node-combo',
		onSelect:chooseResourceBoxChange
	  })
  });

  function resetChooseResourceBoxSelected() {
    document.chooseResourceBoxNodeList.parentResource[0].selected = true;
  }
  
  function validateChooseResourceBoxNodeChosen() {
    var selectedParentResource = false
    
    for (i = 0; i < document.chooseResourceBoxNodeList.parentResource.length; i++) {
      // make sure something is checked before proceeding
      if (document.chooseResourceBoxNodeList.parentResource[i].selected
          && document.chooseResourceBoxNodeList.parentResource[i].value != "") {
        selectedParentResource = document.chooseResourceBoxNodeList.parentResource[i].value;
        break;
      }
    }
    
    return selectedParentResource;
  }
  
  function goChooseResourceBoxChange() {
    var nodeChosen = validateChooseResourceBoxNodeChosen();
    if (nodeChosen != false) {
      document.chooseResourceBoxForm.parentResource.value = nodeChosen;
      document.chooseResourceBoxForm.submit();
      /*
       * We reset the selection after submitting the form so if the user
       * uses the back button to get back to this page, it will be set at
       * the "choose a node" option.  Without this, they wouldn't be able
       * to proceed forward to the same node because won't trigger the
       * onChange action on the <select/> element.  We also do the submit
       * in a separate form after we copy the chosen value over, just to
       * ensure that no problems happen by resetting the selection
       * immediately after calling submit().
       */
      resetChooseResourceBoxSelected();
    }
  }

  function chooseResourceBoxChange(record){
	window.location = "graph/chooseresource.htm?parentResourceType=node&reports=all&relativetime=lastday&parentResource=" + record.data.id;
  }
</script>

<h3 class="o-box"><a href="graph/index.jsp">Resource Graphs</a></h3>
<div class="boxWrapper">

  <c:choose>
    <c:when test="${empty resources}">
      <p>No nodes are in the database or no nodes have RRD data</p>
    </c:when>
  
    <c:otherwise>
    	<div id="node-combo"></div>
      <%-- <form method="get" name="chooseResourceBoxForm" action="graph/chooseresource.htm" >
        <input type="hidden" name="parentResourceType" value="node" />
        <input type="hidden" name="reports" value="all"/>
        <input type="hidden" name="relativetime" value="lastday" />
        <input type="hidden" name="parentResource" value="" />
      </form>
      
      <form name="chooseResourceBoxNodeList">
        <select style="width: 100%;" name="parentResource" onchange="goChooseResourceBoxChange();">
          <option value="">-- Choose a node --</option>
          <c:forEach var="resource" items="${resources}">
            <option value="${resource.name}">${resource.label}</option>
          </c:forEach>
        </select>
      </form>
      
      <script type="text/javascript">
        resetChooseResourceBoxSelected();
      </script>--%>
  
    </c:otherwise>
  </c:choose>
</div>
