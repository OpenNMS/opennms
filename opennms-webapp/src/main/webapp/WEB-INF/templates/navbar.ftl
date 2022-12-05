<#assign currentDate = .now>

<nav class="navbar navbar-expand-md navbar-dark opennms-bg-chromatic-black" id="header" role="navigation">
    <!-- Brand and toggle get grouped for better mobile display -->
    <a class="navbar-brand" href="${baseHref}index.jsp">
        <img id="logo" src="${baseHref}images/opennms-horizon-logo.svg" alt="OpenNMS Horizon Logo" width="auto" height="22px" onerror="this.src='${baseHref}images/opennms-horizon-logo.png'" />
    </a>
    <button type="button" title="Toggle navigation" class="navbar-toggler collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
        <span class="navbar-toggler-icon"></span>
    </button>

    <div id="navbar" class="navbar-collapse collapse">
        <#if request.remoteUser?has_content >
            <ul class="navbar-nav ml-1">
                <li class="nav-item">
                    <span class="navbar-text navbar-formatted-time">${formattedTime}</span>
                </li>
                <li class="nav-item">
                    <span class="navbar-text navbar-bell ml-1">
                        <#if noticeStatus = 'Unknown'>
                            <!-- Gray circle with bell inside -->
                            <i class="fa fa-bell text-secondary"></i>
                        </#if>
                        <#if noticeStatus = 'Off'>
                            <i class="fa fa-bell-slash text-danger" title="Notices: Off"></i>
                        </#if>
                        <#if noticeStatus = 'On'>
                            <i class="fa fa-bell text-horizon" title="Notices: On"></i>
                        </#if>
                    </span>
                </li>
            </ul>
        </#if>

        <onms-central-search></onms-central-search>

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
      		            <a href="${baseHref}${item.url}" name="nav-${item.name}-top" class="nav-link dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
    		              <#if isAdminLink(item) >
                            <i class="fa fa-solid fa-cogs"></i>
                          </#if>
      		              ${item.name}
      		            </a>
                      <#else>
        		        <a href="#" name="nav-${item.name}-top" class="nav-link dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">${item.name}</a>
                      </#if>
                      <div class="dropdown-menu dropdown-menu-right" role="menu">
  		                <#list item.entries as subItem>
    		              <#if shouldDisplay(subItem) >
                            <#if subItem.url?has_content >
  		                      <a class="dropdown-item" name="nav-${item.name}-${subItem.name}" href="${baseHref}${subItem.url}">
                                <#if isAdminLink(subItem) >
                                  <i class="fa fa-solid fa-cogs"></i>
                                </#if>
  		                        ${subItem.name}
                              </a>
                            <#else>
      		                  <a class="dropdown-item" name="nav-${item.name}-${subItem.name}" href="#">${subItem.name}</a>
                            </#if>
                          </#if>
                        </#list>
                      </div>
                    </li>
                  <#else>
                    <#if item.url?has_content >
  		              <li class="nav-item">
  		                <a class="nav-link" name="nav-${item.name}-top" href="${baseHref}${item.url}">
    		              <#if isAdminLink(item) >
                            <i class="fa fa-solid fa-cogs"></i>
                          </#if>
  		                  ${item.name}
		                </a>
		              </li>
                    <#else>
  		              <a class="nav-link" name="nav-${item.name}-top" href="#">${item.name}</a>
                    </#if>
                  </#if>
                </#if>
              </#list>

              <li id="menubar-plugin-container" class="nav-item dropdown" style="display:none">
                  <a name="nav-plugin-top" class="nav-link dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Plugins</a>
                  <div id="menubar-plugin-item-container" class="dropdown-menu dropdown-menu-right" role="menu"></div>
              </li>

              <li class="nav-item dropdown">
                  <a name="nav-help-top" class="nav-link dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Help</a>
                  <div class="dropdown-menu dropdown-menu-right">
                      <a class="dropdown-item" name="nav-admin-help" href="${baseHref}help/index.jsp">
                          <i class="fa fa-fw fa-question-circle"></i>&nbsp; Help
                      </a>
                      <a class="dropdown-item" name="nav-admin-about" href="${baseHref}about/index.jsp">
                          <i class="fa fa-fw fa-info-circle"></i>&nbsp; About
                      </a>
                      <a class="dropdown-item" name="nav-admin-api-documentation" href="${baseHref}ui/index.html#/open-api">
                          <i class="fa fa-fw fa-info-circle"></i>&nbsp; API Documentation
                      </a>
                      <#if isAdmin >
                          <a class="dropdown-item" name="nav-admin-support" href="${baseHref}support/index.jsp" title="Support">
                              <i class="fa fa-solid fa-cogs"></i>&nbsp;Support
                          </a>
                      </#if>
                  </div>
              </li>
              <li class="nav-item dropdown">
                  <a name="nav-user-top" href="${baseHref}account/selfService/index.jsp" class="nav-link dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
                      <span class="fa fa-user"></span>
                      ${request.remoteUser}
                  </a>
                  <div class="dropdown-menu dropdown-menu-right" role="menu">
                      <a class="dropdown-item" name="nav-admin-self-service" href="${baseHref}account/selfService/newPasswordEntry">
                          <i class="fa fa-key"></i>&nbsp; Change Password</a>
                      <form name="headerLogoutForm" action="${baseHref}j_spring_security_logout" method="post"></form>
                      <a class="dropdown-item" name="nav-admin-logout" href="javascript:document.headerLogoutForm.submit()" style="white-space: nowrap">
                          <i class="fa fa-sign-out"></i>&nbsp; Log Out
                      </a>
                  </div>
              </li>
              <li class="nav-item dropdown">
                  <a href="#" class="nav-link dropdown-toggle badge-wrapper" data-toggle="dropdown" role="button" aria-expanded="false">
                      <span class="badge badge-pill userNotificationCount" id="userNotificationsBadge"></span>
                      <span class="badge badge-pill teamNotificationCount" id="teamNotificationsBadge"></span>
                  </a>
                  <div class="dropdown-menu dropdown-menu-right" role="menu">
                      <a class="dropdown-item" href="${baseHref}notification/browse?acktype=unack&amp;filter=user==${request.remoteUser}">
                          <i class="fa fa-fw fa-user"></i>&nbsp; <span class="userNotificationCount">0</span> notices assigned to you
                      </a>
                      <div id="headerNotifications" style="min-width: 500px"></div>


                      <div class="dropdown-divider"></div>
                      <a class="dropdown-item" href="${baseHref}notification/browse?acktype=unack">
                          <i class="fa fa-fw fa-users"></i>&nbsp; <span class="teamNotificationCount">0</span> of <span class="totalNotificationCount"></span> assigned to anyone but you
                      </a>
                      <a class="dropdown-item" href="${baseHref}roles">
                          <i class="fa fa-fw fa-calendar"></i>&nbsp; On-Call Schedule
                      </a>
                  </div>
              </li>
              </ul>
              <ul class="navbar-nav">
              <#if isAdmin || isProvision >
                  <li class="nav-item">
                      <a class="nav-link" style="font-size: 1.25rem" name="nav-admin-quick-add" href="${baseHref}admin/ng-requisitions/quick-add-node.jsp#/" title="Quick-Add Node">
                          <i class="fa fa-plus-circle"></i>
                      </a>
                  </li>
              </#if>
              <#if isFlow >
                  <li class="nav-item">
                      <a class="nav-link" style="font-size: 1.25rem" name="nav-admin-flow" href="${baseHref}admin/classification/index.jsp" title="Flow Management">
                          <i class="fa fa-minus-circle"></i>
                      </a>
                  </li>
              </#if>
              <#if isAdmin >
                  <li class="nav-item">
                      <a class="nav-link" style="font-size: 1.25rem" title="Configure OpenNMS" href="${baseHref}admin/index.jsp"><i class="fa fa-cogs"></i></a>
                  </li>
              </#if>
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

<#-- Notification gathering -->
<#if request.remoteUser?has_content >
<script type="text/javascript">
    var currentUser = "${request.remoteUser}";
    var baseHref = "${baseHref}";

    var updateNotificationIndicators = function(summary) {
        if (summary === undefined) {
            summary = {
                totalCount: 0,
                totalUnacknowledgedCount:0,
                userUnacknowledgedCount: 0,
                teamUnacknowledgedCount: 0,
                userUnacknowledgedNotifications: {
                    offset: 0,
                    count: 0,
                    totalCount: 0,
                    notification: []
                }
            };
        };
        updateNotificationBadges(summary);
        updateNotificationList(summary);
    };

    var updateNotificationBadges = function(summary) {
        // Update text
        $(".userNotificationCount").text(summary.userUnacknowledgedCount);
        $(".teamNotificationCount").text(summary.teamUnacknowledgedCount);
        $(".totalNotificationCount").text(summary.totalUnacknowledgedCount);

        // Update badges
        $("#teamNotificationsBadge").attr("class", "badge badge-pill teamNotificationCount");
        $("#userNotificationsBadge").attr("class", "badge badge-pill userNotificationCount");

        if (summary.teamUnacknowledgedCount === 0) {
            $("#teamNotificationsBadge").addClass("badge-severity-cleared");
        } else {
            $("#teamNotificationsBadge").addClass("badge-info");
        }

        // Set severity for user notifications
        var userNotifications = summary.userUnacknowledgedNotifications.notification;
        if (userNotifications.length === 0) {
            $("#userNotificationsBadge").addClass("badge-severity-cleared");
        } else {
            var severities = ["CLEARED", "INDETERMINATE", "WARNING", "MINOR", "MAJOR", "CRITICAL"];
            var severityIndexList = userNotifications.map(function(notification) {
                return severities.indexOf(notification.severity);
            });
            var maxSeverityIndex = Math.max.apply(Math, severityIndexList);
            var maxSeverity = severities[maxSeverityIndex].toLowerCase();
            $("#userNotificationsBadge").addClass("badge-severity-" + maxSeverity);
        }
    };

    var updateNotificationList = function(summary) {
        $("#headerNotifications").empty();
        var notifications = summary.userUnacknowledgedNotifications.notification;
        notifications.forEach(function(notification, index) {
            // Determine time
            var date = new Date(notification.pageTime);
            var dateTime = date.toLocaleDateString() + " " + date.toLocaleTimeString();
            var nodeLabel = notification.nodeLabel ? notification.nodeLabel : "";
            var ipAddress = notification.ipAddress ? notification.ipAddress : "";
            var serviceName = notification.serviceType && notification.serviceType.name ? notification.serviceType.name : "";
            var severity = notification.severity ? notification.severity.toLowerCase() : "indeterminate";

            $('<a class="dropdown-item" id="notification-"' + notification.id + '" href="' + baseHref + 'notification/detail.jsp?notice=' + notification.id + '">' +
                '            <div class="row align-items-center">' +
                '                <div class="col-1"><span class="fa fa-circle text-severity-'+ severity +'"></span></div>' +
                '                <div class="col-11">' +
                '                    <div class="row">' +
                '                        <div class="col"><span class="font-weight-bold">' + dateTime + '</span></div>' +
                '                    </div>' +
                '                    <div class="row">' +
                '                        <div class="col">' + notification.notificationName + '</div>' +
                '                        <div class="col">' + nodeLabel + '</div>' +
                '                        <div class="col">' + ipAddress + '</div>' +
                '                        <div class="col">' + serviceName + '</div>' +
                '                    </div>' +
                '                </div>' +
                '            </div>' +
                '        </a>').appendTo("#headerNotifications");

            // Add Divider
            if (index < notifications.length - 1) {
                $('<div class="dropdown-divider"></div>').appendTo("#headerNotifications");
            }
        });

        if (summary.userUnacknowledgedCount > notifications.length) {
            var moreLink = baseHref + "notification/browse?acktype=unack&amp;filter=user==" + currentUser;
            $('<div class="dropdown-divider"></div><a class="dropdown-item" href="' + moreLink + '">Show more ...</a>').appendTo("#headerNotifications");
        }
    };

    var updatePluginLinks = function(response) {
        if (response && response.length) {
            response.forEach(function(item, index) {
                var name = "nav-plugin-" + item.extensionId + "-" + item.resourceRootPath;
                var url = "${baseHref}ui/#/plugins/" + item.extensionId + "/" + item.resourceRootPath + "/" + item.moduleFileName;
                var text = item.menuEntry;
                var html = '<a class="dropdown-item" name="nav-' + name + '" href="' + url + '">' + text + '</a>';

                $(html).appendTo("#menubar-plugin-item-container");
            });

            $("#menubar-plugin-container").show();
        }
    };

    $(document).ready(function() {
        updateNotificationIndicators();

        // Load notification summary for current user
        $.get(baseHref + "rest/notifications/summary", function(response) {
            if (response) {
                updateNotificationIndicators(response);
            }
        });

        // Get plugin info
        $.get(baseHref + "rest/plugins", function(response) {
            if (response) {
                updatePluginLinks(response);
            }
        });
    });

</script>
</#if>
