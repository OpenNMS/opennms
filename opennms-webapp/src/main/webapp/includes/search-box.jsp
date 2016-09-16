<%@page language="java"
        contentType="text/html"
        session="true"
%>

<script type="text/javascript" src="lib/angular/angular.js"></script>
<script type="text/javascript" src="lib/angular-bootstrap/ui-bootstrap-tpls.js"></script>
<script type="text/javascript" src="js/onms-search/app.js"></script>

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
