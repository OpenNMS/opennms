<#assign currentDate = .now>
<nav class="navbar navbar-expand-md navbar-dark bg-dark" id="header" role="navigation">
    <!-- Brand and toggle get grouped for better mobile display -->
    <a class="navbar-brand" href="${baseHref}index.jsp">
        <img id="logo" src="${baseHref}images/o-green-trans.svg" alt="OpenNMS" width="40px" height="40px" onerror="this.src='${baseHref}images/o-green-trans.png'" />&nbsp;
    </a>
    <button type="button" title="Toggle navigation" class="navbar-toggler collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
        <span class="navbar-toggler-icon"></span>
    </button>



    <div style="margin-top: auto" id="navbar" class="navbar-collapse collapse">
        <#if request.remoteUser?has_content >
            <div id="headerinfo" style="position: absolute; top: 0; right: 0" class="text-light mr-4">
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
        <ul class="navbar-nav ml-auto">
		<#if request.remoteUser?has_content >
		  <#list model.entryList as entry>
              <#assign item=entry.getKey()>
              <#assign display=entry.getValue()>

              <#if shouldDisplay(item, display) >
                  <#if item.entries?has_content >
                  <#-- has sub-entries, draw menu drop-downs -->
		        <li class="nav-item dropdown">
		          <#if item.url?has_content && item.url != "#">
		            <a href="${baseHref}${item.url}" name="nav-${item.name}-top" class="nav-link dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">${item.name}</a>
                  <#else>
		            <a href="#" name="nav-${item.name}-top" class="nav-link dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">${item.name}</a>
                  </#if>
                    <div class="dropdown-menu dropdown-menu-right" role="menu">
		            <#list item.entries as subItem>
		              <#if shouldDisplay(subItem) >
                          <#if subItem.url?has_content >
		                  <a class="dropdown-item" name="nav-${item.name}-${subItem.name}" href="${baseHref}${subItem.url}">${subItem.name}</a>
                          <#else>
		                  <a class="dropdown-item" name="nav-${item.name}-${subItem.name}" href="#">${subItem.name}</a>
                          </#if>
                      </#if>
                    </#list>
                    </div>
                </li>
                  <#else>
                      <#if item.url?has_content >
		          <li class="nav-item"><a class="nav-link" name="nav-${item.name}-top" href="${baseHref}${item.url}">${item.name}</a></li>
                      <#else>
		          <a class="nav-link" name="nav-${item.name}-top" href="#">${item.name}</a>
                      </#if>
                  </#if>
              </#if>
          </#list>
            <li class="nav-item dropdown">
                <a name="nav-admin-top" href="${baseHref}account/selfService/index.jsp" class="nav-link dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
              <#if request.remoteUser?has_content >
                  <span class="fa fa-user"></span>
                  ${request.remoteUser}
              <#else>
                &hellip;
              </#if>
                </a>
                <div class="dropdown-menu dropdown-menu-right" role="menu">
                    <a class="dropdown-item" name="nav-admin-notice-status" href="#" style="white-space: nowrap">Notices: <b id="notification${noticeStatus}">${noticeStatus}</b></a>
              <#if isAdmin >
                <a class="dropdown-item" name="nav-admin-admin" href="${baseHref}admin/index.jsp" style="white-space: nowrap">Configure OpenNMS</a>
              </#if>
              <#if isAdmin || isProvision >
                <a class="dropdown-item" name="nav-admin-quick-add" href="${baseHref}admin/ng-requisitions/quick-add-node.jsp#/" style="white-space: nowrap">Quick-Add Node</a>
              </#if>
              <#if isAdmin >
                <a class="dropdown-item" name="nav-admin-support" href="${baseHref}support/index.htm">Support</a>
              </#if>
              <#if request.remoteUser?has_content >
                <a class="dropdown-item" name="nav-admin-self-service" href="${baseHref}account/selfService/index.jsp">Change Password</a>
                <a class="dropdown-item" name="nav-admin-logout" href="${baseHref}j_spring_security_logout" style="white-space: nowrap">Log Out</a>
              </#if>
                    <a class="dropdown-item" name="nav-admin-help" href="${baseHref}help/index.jsp" style="white-space: nowrap">Help</a>
                    <a class="dropdown-item" name="nav-admin-about" href="${baseHref}about/index.jsp" style="white-space: nowrap">About</a>
                </div>
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
