<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("User Data Collection")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("User Data Collection")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<style>
    .admin-user-data-collection-form-wrapper {
        margin-bottom: 50px;
        margin-top: 8px;
    }

    .user-data-collection-button {
        border-radius: 0;
		box-shadow: none;
        font-size: 0.875rem;
        font-weight: 700;
        letter-spacing: 0.2em;
        line-height: 1.25rem;
        text-transform: uppercase;
    }

	.user-data-collection-enable-button {
        background-color: #273180;
        border: 2px solid transparent;
        color: #fff;
        margin-right: 1rem;
    }

    .user-data-collection-enable-button:hover {
        box-shadow: 3px 1px 2px #6c757d;
        color: #fff;
    }
</style>

<div>
If you would like to receive more information about OpenNMS Horizon, Meridian and other products, please click
Enable below and you will be redirected to the main page where you can enter your information.
</div>
<div class="admin-user-data-collection-form-wrapper">
    <form id="admin-user-data-collection-form">
        <button id="user-data-collection-notice-enable" type="button" class="btn user-data-collection-button user-data-collection-enable-button">Enable</button>
    </form>
</div>

<script type="text/javascript">
(function() {
    function userDataCollectionEnableInitialNotice() {
        var data = {
            noticeAcknowledged: false
        };

        $.ajax({
            url: 'rest/datachoices/userdatacollection/status',
            method: 'POST',
            dataType: null,
            contentType: 'application/json',
            processData: false,
            data: JSON.stringify(data)
        })
        .done(function() {
            window.location.href = '/index.jsp';
        })
        .fail(function() {
            alert('There was an error processing your request.');
        });
    }

    $(document).ready(function() {
        $('#user-data-collection-notice-enable').click(function(e) {
            e.preventDefault();
            userDataCollectionEnableInitialNotice();
        });
    });
})();
</script>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
