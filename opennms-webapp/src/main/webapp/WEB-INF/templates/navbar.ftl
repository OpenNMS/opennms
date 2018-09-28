<#assign currentDate = .now>
<nav class="navbar navbar-inverse navbar-fixed-top" id="header" role="navigation">
    <!-- Brand and toggle get grouped for better mobile display -->
    <div class="navbar-header">
      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="${baseHref}index.jsp">
        <img id="logo" src="${baseHref}images/horizon_logo.svg" alt="OpenNMS" onerror="this.src='${baseHref}images/horizon_logo_small.png'" />
      </a>
    </div>

    <#if request.remoteUser?has_content >
      <div id="headerinfo" style="display: none" class="nav navbar-nav navbar-right navbar-info">
        ${formattedTime}
        <span class="fa-stack" style="text-shadow:none">
        <#if noticeStatus = 'Unknown'>
            <!-- Gray circle with bell inside -->
            <i class="fa fa-circle fa-stack-2x text-muted"></i>
            <i class="fa fa-circle-thin fa-stack-2x"></i>
            <i class="fa fa-bell fa-stack-1x"></i>
        </#if>
        <#if noticeStatus = 'Off'>
            <!-- Bell with red slash over it -->
            <i class="fa fa-bell fa-stack-1x"></i>
            <i class="fa fa-ban fa-stack-2x text-danger"></i>
        </#if>
        <#if noticeStatus = 'On'>
            <!-- Green circle with bell inside -->
            <i class="fa fa-circle fa-stack-2x text-success"></i>
            <i class="fa fa-circle-thin fa-stack-2x"></i>
            <i class="fa fa-bell fa-stack-1x"></i>
        </#if>
        </span>
      </div>
    </#if>

    <div style="margin-right: 15px" id="navbar" class="navbar-collapse collapse">
		<ul class="nav navbar-nav navbar-right">
		<#if request.remoteUser?has_content >
		  <#list model.entryList as entry>
		    <#assign item=entry.getKey()>
		    <#assign display=entry.getValue()>

            <#if shouldDisplay(item, display) >
		      <#if item.entries?has_content >
		        <#-- has sub-entries, draw menu drop-downs -->
		        <li class="dropdown">
		          <#if item.url?has_content && item.url != "#">
		            <a href="${baseHref}${item.url}" name="nav-${item.name}-top" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">${item.name} <span class="caret"></span></a>
		          <#else>
		            <a href="#" name="nav-${item.name}-top" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">${item.name} <span class="caret"></span></a>
		          </#if>
		          <ul class="dropdown-menu" role="menu">
		            <#list item.entries as subItem>
		              <#if shouldDisplay(subItem) >
		              <li>
		                <#if subItem.url?has_content >
		                  <a name="nav-${item.name}-${subItem.name}" href="${baseHref}${subItem.url}">${subItem.name}</a>
		                <#else>
		                  <a name="nav-${item.name}-${subItem.name}" href="#">${subItem.name}</a>
		                </#if>
		              </li>
		              </#if>
		            </#list>
		          </ul>
		        </li>
		      <#else>
		        <#if item.url?has_content >
		          <li><a name="nav-${item.name}-top" href="${baseHref}${item.url}">${item.name}</a></li>
		        <#else>
		          <a name="nav-${item.name}-top" href="#">${item.name}</a>
		        </#if>
		      </#if>
		    </#if>
		  </#list>
		  <li class="dropdown">
            <a name="nav-admin-top" href="${baseHref}account/selfService/index.jsp" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
              <#if request.remoteUser?has_content >
                <span class="glyphicon glyphicon-user"></span>
                ${request.remoteUser}
              <#else>
                &hellip;
              </#if>
              <span class="caret"></span>
            </a>
            <ul class="dropdown-menu" role="menu">
              <li><a name="nav-admin-notice-status" href="#" style="white-space: nowrap">Notices: <b id="notification${noticeStatus}">${noticeStatus}</b></a></li>
              <#if isAdmin >
                <li><a name="nav-admin-admin" href="${baseHref}admin/index.jsp" style="white-space: nowrap">Configure OpenNMS</a></li>
              </#if>
              <#if isAdmin || isProvision >
                <li><a name="nav-admin-quick-add" href="${baseHref}admin/ng-requisitions/quick-add-node.jsp#/" style="white-space: nowrap">Quick-Add Node</a></li>
              </#if>
              <#if isAdmin >
                <li><a name="nav-admin-support" href="${baseHref}support/index.htm">Support</a></li>
              </#if>
              <#if request.remoteUser?has_content >
                <li><a name="nav-admin-self-service" href="${baseHref}account/selfService/index.jsp">Change Password</a>
                <li><a name="nav-admin-logout" href="${baseHref}j_spring_security_logout" style="white-space: nowrap">Log Out</a></li>
              </#if>
              <li><a name="nav-admin-help" href="${baseHref}help/index.jsp" style="white-space: nowrap">Help</a></li>
              <li><a name="nav-admin-about" href="${baseHref}about/index.jsp" style="white-space: nowrap">About</a></li>
            </ul>
          </li>
		</#if>
		</ul>
    </div>
</nav>

<#-- hide the header if not displayed in a toplevel window (iFrame) -->
<script type='text/javascript'>
if (window.location != window.parent.location && window.name.indexOf("-with-header") == -1) {
  // Hide the header
  $("#header").hide();
  // Remove any padding from the body
  $("body.fixed-nav").attr('style', 'padding-top: 0px !important');
}
</script>
