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
// 2008 Sep 28: Fixed XSS security issues. - ranger@opennms.org
// 2004 Nov 18: Fixed problem with category display when nodeLabel can't be found. Bill Ayres.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.category.*,
	        org.opennms.web.Util,
		org.opennms.web.element.NetworkElementFactory,
		org.opennms.web.MissingParameterException,
		java.util.*,
		org.opennms.netmgt.xml.rtc.Node,
		org.opennms.web.XssRequestWrapper
		"
%>

<%!
    public CategoryModel model = null;
    
    public void init() throws ServletException {
        try {
            this.model = CategoryModel.getInstance();            
        }
        catch( java.io.IOException e ) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }
        catch( org.exolab.castor.xml.MarshalException e ) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }
        catch( org.exolab.castor.xml.ValidationException e ) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }        

    }
%>

<%

   HttpServletRequest req = new XssRequestWrapper(request);
   String categoryName = req.getParameter("category");

    if (categoryName == null) {
        throw new MissingParameterException("category");
    }

    Category category = this.model.getCategory(categoryName);

    if (category == null) {
        throw new CategoryNotFoundException(categoryName);
    }
    
    AclUtils.NodeAccessChecker accessChecker = AclUtils.getNodeAccessChecker(getServletContext());

    //put the nodes in a tree map to sort by name
    TreeMap nodeMap = new TreeMap();    
    Enumeration nodeEnum = category.enumerateNode();
    
    while (nodeEnum.hasMoreElements()) {
        Node node = (Node) nodeEnum.nextElement();
        int nodeId = (int)node.getNodeid();
        String nodeLabel =
		NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(nodeId);
        // nodeMap.put( nodeLabel, node );

        if (accessChecker.isNodeAccessible(nodeId)) {
            if (nodeLabel != null && !nodeMap.containsKey(nodeLabel)) {
                nodeMap.put(nodeLabel, node);
            } else if (nodeLabel != null) {
                nodeMap.put(nodeLabel+" (nodeid="+node.getNodeid()+")", node);
            } else {
                nodeMap.put("nodeId=" + node.getNodeid(), node);
            }
        }
    }
    
    Set keySet = nodeMap.keySet();
    Iterator nameIterator = keySet.iterator();
%>


<%@page import="org.opennms.web.AclUtils"%>
<%@page import="org.opennms.web.AclUtils.NodeAccessChecker"%><jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Category Service Level Monitoring" />
  <jsp:param name="headTitle" value="<%=category.getName()%>" />
  <jsp:param name="headTitle" value="Category" />
  <jsp:param name="headTitle" value="SLM" />
  <jsp:param name="breadcrumb" value="<a href='rtc/index.jsp'>SLM</a>" />
  <jsp:param name="breadcrumb" value="Category"/>
</jsp:include>

<h3>
  <span title="Last updated <%=Util.htmlify(category.getLastUpdated().toString())%>">
    <%=Util.htmlify(category.getName())%>
  </span>
</h3>

<form name="showoutages">
  <p>
    Show interfaces:
	<% String showoutages = req.getParameter("showoutages"); %>

        <%  
        if(showoutages == null ) {
           showoutages = "avail";
        } %>

              <input type="radio" name="showout" <%=(showoutages.equals("all") ? "checked" : "")%>
               onclick="top.location = '<%=org.opennms.web.Util.calculateUrlBase( req )%>rtc/category.jsp?category=<%=Util.encode(category.getName())%>&amp;showoutages=all'" ></input>All


              <input type="radio" name="showout" <%=(showoutages.equals("outages") ? "checked" : "")%>
               onclick="top.location = '<%=org.opennms.web.Util.calculateUrlBase( req )%>rtc/category.jsp?category=<%=Util.encode(category.getName())%>&amp;showoutages=outages'" ></input>With outages


              <input type="radio" name="showout" <%=(showoutages.equals("avail") ? "checked" : "")%>
               onclick="top.location = '<%=org.opennms.web.Util.calculateUrlBase( req )%>rtc/category.jsp?category=<%=Util.encode(category.getName())%>&amp;showoutages=avail'" ></input>With availability &lt; 100% 

  </p>
</form>

      <% if( category.getComment() != null ) { %>      
        <p><%=Util.htmlify(category.getComment())%></p>
      <% } %>
      <% if( AclUtils.shouldFilter() ) { %>
        <p style="color: red"> This list has been filtered to accessible nodes only based on your user group. </p>
      <% } %>
      <!-- Last updated <%=Util.htmlify(category.getLastUpdated().toString())%> -->

      <table>
        <tr>
          <th>Nodes</th>
          <th>Outages</th>
          <th>24hr Availability</th>
        </tr>
      
        <%  
	    int valuecnt = 0;
	    int outagecnt = 0;

            while( nameIterator.hasNext() ) {
                String nodeLabel = (String)nameIterator.next();
                Node node = (Node)nodeMap.get(nodeLabel);
                
                double value = node.getNodevalue();
        
                if( value >= 0 ) {
                    long serviceCount = node.getNodesvccount();        
                    long serviceDownCount = node.getNodesvcdowncount();
                    double servicePercentage = 100.0;
                
                    if( serviceCount > 0 ) {
                       servicePercentage = ((double)(serviceCount-serviceDownCount))/(double)serviceCount*100.0;
                    }
                
                    String availClass = CategoryUtil.getCategoryClass( category, value );
                    String outageClass = CategoryUtil.getCategoryClass( category, servicePercentage );

		    if ( showoutages.equals("all") | (showoutages.equals("outages") & serviceDownCount > 0 ) | (showoutages.equals("avail") & value < 100 ) ) {
        %>
                    <tr class="CellStatus">
                      <td><a href="element/node.jsp?node=<%=node.getNodeid()%>"><%=Util.htmlify(nodeLabel)%></a></td>
                      <td class="<%=outageClass%>" align="right"><%=serviceDownCount%> of <%=serviceCount%></td>
                      <td class="<%=availClass%>" align="right" width="30%"><b><%=CategoryUtil.formatValue(value)%>%</b></td>
                    </tr>
            	    <%  } 
		    if (value < 100 )
		        ++valuecnt;
		    if (serviceDownCount > 0 )
		        ++outagecnt;
		    %>
            <%  } %>
        <%  } %>

	<% if ( showoutages.equals("outages") & outagecnt == 0 ) { %>
		<tr>
                  <td colspan="3">
		    There are currently no outages in this Category
		  </td>
                </tr>
        <%  } %>

	<% if ( showoutages.equals("avail") & valuecnt == 0 ) { %>
		<tr>
                  <td colspan="3">
		    All services in this Category are at 100%
		  </td>
                </tr>
        <%  } %>
        
    </table>


<jsp:include page="/includes/footer.jsp" flush="false" />
