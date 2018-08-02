<%@page language="java"
        contentType="text/html"
        session="true"
%>

<div id="onms-search">

  <div class="card">
    <div class="card-header">
      <span><a href="graph/index.jsp">Resource Graphs</a></span>
    </div>
    <div class="card-body">
      <onms-search-nodes />
    </div>
  </div>

  <div class="card">
    <div class="card-header">
      <span><a href="KSC/index.jsp">KSC Reports</a></span>
    </div>
    <div class="card-body">
      <onms-search-ksc />
    </div>
  </div>

</div>

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="search" />
</jsp:include>
