<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="Remote Monitor" />
	<jsp:param name="headTitle" value="Remote Monitor" />
	<jsp:param name="breadcrumb" value="<a href='maps.htm'>Maps</a>" />
	<jsp:param name="breadcrumb" value="Remote Monitor" />
</jsp:include>

<style type="text/css">
	iframe {
		border: 1px solid black;
	}
</style>

<iframe id='app' src="RemotePollerMap/app.jsp" scrolling="no" frameborder="0" width="100%" seamless></iframe>

<script type="text/javascript">
var timer;

var waitFor = function(callback) {
	if (timer) {
		return;
	} else {
		timer = window.setTimeout(function() {
			callback();
			timer = undefined;
		}, 100);
	}
};

function resizeIframe() {
	var element = $('#app');

	height = $(window).height();
	offset = element.offset().top;
	footerHeight = $('#footer').outerHeight();
	console.log(new Date().getTime() + ' height=',height);
	console.log(new Date().getTime() + ' offset=',offset);
	console.log(new Date().getTime() + ' footerHeight=',footerHeight);
	element.height((height - offset - footerHeight - 5) + "px");
}
$(window).resize(function() {
	waitFor(resizeIframe);
});
$(document).ready(function() {
	waitFor(resizeIframe);
});
</script>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>