<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java" contentType="text/html" session="true" %>

<%@page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@page import="java.util.Collection" %>
<%@page import="java.util.List" %>
<%@page import="java.util.Collections" %>
<%@page import="org.springframework.web.context.WebApplicationContext" %>
<%@page import="org.opennms.web.navigate.PageNavEntry" %>
<%@page import="org.opennms.core.soa.ServiceRegistry" %>
<%@page import="org.opennms.web.tags.filters.AlarmFilterCallback" %>
<%@page import="org.opennms.web.tags.filters.FilterCallback" %>
<%@page import="org.opennms.netmgt.model.OnmsFilterFavorite" %>
<%@page import="org.opennms.web.filter.NormalizedQueryParameters" %>
<%@page import="org.opennms.web.filter.Filter" %>
<%@page import="org.opennms.core.utils.WebSecurityUtils" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="../../taglib.tld" prefix="onms" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Alarms" />
  <jsp:param name="headTitle" value="Alarms" />
  <jsp:param name="location" value="alarm" />  
  <jsp:param name="breadcrumb" value="Alarms" />
</jsp:include>

<div class="row">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
      	<h3 class="panel-title">Alarm Queries</h3>
      </div>
      <div class="panel-body">
        <form action="alarm/detail.htm" method="get" role="form" class="form-inline pull-right">
          <div class="form-group">
            <label for="byalarmid_id">Alarm ID:</label>
            <input type="text" class="form-control" name="id" id="byalarmid_id"/>
            <button type="submit" class="btn btn-default">Get details</button>
          </div>                
        </form>
        <ul class="list-unstyled">
          <li><a href="alarm/list.htm" title="Summary view of all outstanding alarms">All alarms (summary)</a></li>
          <li><a href="alarm/list.htm?display=long" title="Detailed view of all outstanding alarms">All alarms (detail)</a></li>
          <li><a href="alarm/advsearch.jsp" title="More advanced searching and sorting options">Advanced Search</a></li>
          <%=getAlarmPageNavItems() %>
        </ul>  
      </div>
    </div>
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Alarm Filter Favorites</h3>
      </div>
      <div class="panel-body">
	      <onms:alert/>
          <c:choose>
              <c:when test="${!empty favorites}">
              
              
                  <!-- Filters -->
                  <ul class="list-unstyled">
                      <c:forEach var="eachFavorite" items="${favorites}">
                      	<%
                      		OnmsFilterFavorite current = (OnmsFilterFavorite) pageContext.getAttribute("eachFavorite");
    						FilterCallback callback = (AlarmFilterCallback) request.getAttribute("callback");

					    	List<Filter> queryElements = callback.parse(current.getFilter());
					    	
					    	final StringBuilder buf = new StringBuilder("<ul class=\"list-unstyled\">"); 
					    	for(Filter queryElement : queryElements) {
					    	    buf.append("<li>");
					    		buf.append(queryElement.getTextDescription());
							    buf.append("</li>");
					    	}
					    	buf.append("</ul>");

					    	pageContext.setAttribute("favTitle", buf.toString());
    					%>

                          <li>
                              <a href="alarm/list.htm?favoriteId=${eachFavorite.id}&${eachFavorite.filter}" title='<c:out value='${favTitle}'/>' data-html="true" data-toggle="tooltip" data-placement="right">${eachFavorite.name}</a> <a href="alarm/deleteFavorite?favoriteId=${eachFavorite.id}&redirect=/alarm/index" title='Delete favorite' data-toggle="tooltip" data-placement="right"><span class="glyphicon glyphicon-remove text-danger"></span></a>
                          </li>
                      </c:forEach>
                  </ul>
              </c:when>
              <c:otherwise>
                  <p>No favorites available.</p>
              </c:otherwise>
          </c:choose>
      </div>
    </div>
  </div>

	<div class="col-md-6">
		<div class="panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title">Outstanding and acknowledged alarms</h3>
			</div>
			<div class="panel-body">
				<p>
					Alarms can be <em>acknowledged</em>, or removed from the default
					view of all users, by selecting the alarms' <em>Ack</em> check box
					and clicking the <em>Acknowledge Selected Alarms</em> at the bottom
					of the page. Acknowledging an alarm gives users the ability to take
					personal responsibility for addressing a network or systems-related
					issue. Any alarm that has not been acknowledged is active in the
					default alarms view of all users' browsers and is considered
					outstanding.
				</p>

				<p>
					To view acknowledged alarms, go to the <em>All Alarms</em> (<em>summary</em>
					or <em>details</em>) list and click the minus sign next to the <em>alarm
						is outstanding</em> search constraint.
				</p>

				<p>
					If an alarm has been acknowledged in error, find the alarm and <em>unacknowledge</em>
					it, making it available again to all users' default alarm views.
				</p>

				<p>
					If you have a specific alarm identifier for which you want a
					detailed alarm description, type the identifier into the <em>Get
						details for Alarm ID</em> box and hit <b>[Enter]</b>. You will then go
					to the appropriate details page.
				</p>
			</div>
		</div>
	</div>
</div> <!-- row -->

<!--  enable tooltips -->
<script type="text/javascript">
  $(function () {
	  $('[data-toggle="tooltip"]').tooltip()
	});
	
</script>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

<%!
    protected String getAlarmPageNavItems(){
        String retVal = "";
        WebApplicationContext webappContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        ServiceRegistry registry = webappContext.getBean(ServiceRegistry.class);
        Collection<PageNavEntry> navEntries = registry.findProviders(PageNavEntry.class, "(Page=alarms)");
        
        for(PageNavEntry navEntry : navEntries){
            retVal += "<li><a href=\"" + navEntry.getUrl() + "\" >" + navEntry.getName() + "</a></li>";
        }
        
        return retVal;
    }

%>
