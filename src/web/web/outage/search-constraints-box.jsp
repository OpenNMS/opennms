<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.Util,org.opennms.web.outage.*,org.opennms.web.outage.filter.Filter" %>

<%
    //required attribute parms
    OutageQueryParms parms = (OutageQueryParms)request.getAttribute( "parms" );

    if( parms == null ) {
        throw new ServletException( "Missing the outage parms request attribute." );
    }

    int length = parms.filters.size();    
%>

<!-- acknowledged/outstanding row -->
<p>
  <form action="outage/list" method="GET" name="outage_search_constraints_box_outtype_form">
    <%=Util.makeHiddenTags(request, new String[] {"outtype"})%>
    
    Outage type:
    <select name="outtype" size="1" onChange="javascript: document.outage_search_constraints_box_outtype_form.submit()">
      <option value="<%=OutageUtil.getOutageTypeString(OutageFactory.OutageType.CURRENT)%>" <%=(parms.outageType == OutageFactory.OutageType.CURRENT) ? "selected=\"1\"" : ""%>>
        Current
      </option>
      
      <option value="<%=OutageUtil.getOutageTypeString(OutageFactory.OutageType.RESOLVED)%>" <%=(parms.outageType == OutageFactory.OutageType.RESOLVED) ? "selected=\"1\"" : ""%>>
        Resolved
      </option>
      
      <option value="<%=OutageUtil.getOutageTypeString(OutageFactory.OutageType.BOTH)%>" <%=(parms.outageType == OutageFactory.OutageType.BOTH) ? "selected=\"1\"" : ""%>>
        Both Current &amp; Resolved
      </option>
    </select>        
  </form>    
</p>

<% if( length > 0 ) { %>
  <p>
    <ol>    
      <% for(int i=0; i < length; i++) { %>
        <% Filter filter = (Filter)parms.filters.get(i); %> 
        
        <li>
          <%=filter.getTextDescription()%>
          <a href="<%=OutageUtil.makeLink(request, parms, filter, false)%>">Remove</a>
        </li>
      <% } %>
    </ol>    
  </p>    
<% } %>  

