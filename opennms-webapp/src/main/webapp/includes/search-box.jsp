<%@page language="java"
        contentType="text/html"
        session="true"
%>

<div id="onms-search">

  <div class="panel panel-default">
    <div class="panel-heading">
      <h3 class="panel-title"><a href="graph/index.jsp">Resource Graphs</a></h3>
    </div>
    <div class="panel-body">
      <onms-search-nodes />
    </div>
  </div>

  <div class="panel panel-default">
    <div class="panel-heading">
      <h3 class="panel-title"><a href="KSC/index.jsp">KSC Reports</a></h3>
    </div>
    <div class="panel-body">
      <onms-search-ksc />
    </div>
  </div>

</div>

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="search" />
</jsp:include>
