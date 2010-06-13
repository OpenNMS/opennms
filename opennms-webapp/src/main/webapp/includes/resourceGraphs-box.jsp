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
%>


<script type="text/javascript" src="js/opennms/ux/ComboFilterBox.js" ></script>
<script type="text/javascript" src="js/opennms/ux/NodePageableComboBox.js" ></script>
<script type="text/javascript">

  function chooseResourceBoxChange(record){
	// Note that if this file is ever included in a JSP not at the top level of the
	// base HREF, we'll have to change this function because IE resolves URLs in
	// window.location against the local directory, not the base HREF
	window.location = "graph/chooseresource.htm?reports=all&parentResourceType=node&parentResource=" + record.data.id;
  }
</script>

<h3 class="o-box"><a href="graph/index.jsp">Resource Graphs</a></h3>
<div class="boxWrapper">
  
    	<script type="text/javascript">
    		Ext.onReady(function(){
    		  var combo = new OpenNMS.ux.NodePageableComboBox({
    			text:'hello',
    			renderTo:'node-combo',
    			onSelect:chooseResourceBoxChange
    		  })
    	  	});
    	</script>
    	<div id="node-combo"></div>
     
</div>
