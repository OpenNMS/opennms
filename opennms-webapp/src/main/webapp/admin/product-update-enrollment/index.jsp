<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Product Update Enrollment")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Product Update Enrollment")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<style>
    .admin-product-update-enrollment-form-wrapper {
        margin-bottom: 50px;
        margin-top: 8px;
    }

    .product-update-enrollment-button {
        border-radius: 0;
		box-shadow: none;
        font-size: 0.875rem;
        font-weight: 700;
        letter-spacing: 0.2em;
        line-height: 1.25rem;
        text-transform: uppercase;
    }

	.product-update-enrollment-enable-button {
        background-color: #273180;
        border: 2px solid transparent;
        color: #fff;
        margin-right: 1rem;
    }

    .product-update-enrollment-enable-button:hover {
        box-shadow: 3px 1px 2px #6c757d;
        color: #fff;
    }
</style>

<div>
If you would like to receive more information about OpenNMS Horizon, Meridian and other products, please click
Enable below and you will be redirected to the main page where you can enter your information.
</div>
<div class="admin-product-update-enrollment-form-wrapper">
    <form id="admin-product-update-enrollment-form">
        <button id="product-update-enrollment-notice-enable" type="button" class="btn product-update-enrollment-button product-update-enrollment-enable-button">Enable</button>
    </form>
</div>

<script type="text/javascript">
(function() {
    function productUpdateEnrollmentEnableInitialNotice() {
        var data = {
            noticeAcknowledged: false
        };

        $.ajax({
            url: 'rest/datachoices/productupdate/status',
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
        $('#product-update-enrollment-notice-enable').click(function(e) {
            e.preventDefault();
            productUpdateEnrollmentEnableInitialNotice();
        });
    });
})();
</script>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
