<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

<%@page language="java"
        contentType="text/html"
        session="true"
        import="org.opennms.web.api.Util"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:if test="${!empty pageContext.request.remoteUser}">
    <script type="text/javascript">
        if ("Notification" in window) {
            var notificationSocket = null;

            function connect() {
                notificationSocket = new WebSocket("<%= Util.calculateUrlBase(request, "notification/stream").replaceAll("^http", "ws") %>");
                notificationSocket.onclose = function () {
                    setTimeout(connect, 1000);
                };
                notificationSocket.onerror = function () {
                    setTimeout(connect, 2000);
                };
                notificationSocket.onmessage = function (event) {
                    let message = JSON.parse(event.data);
                    console.log("Notification", message);

                    new Notification(message.head, {
                        body: message.body,
                        icon: "<%= Util.calculateUrlBase(request, "/images/o-512.png") %>",
                        badge: "<%= Util.calculateUrlBase(request, "/favicon.ico") %>",
                        tag: "opennms:notification:" + message.id,
                    });
                };
            }

            if (Notification.permission === "granted") {
                connect();
            } else if (Notification.permission !== 'denied') {
                Notification.requestPermission(function () {
                    if (permission === "granted") {
                        connect();
                    }
                });
            }
        }
    </script>
</c:if>
