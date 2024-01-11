<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2024 The OpenNMS Group, Inc.
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

<%@page language="java"
        contentType="text/html"
        session="true"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- rss/rssNewsPanel.htm -->
<div class="card opennms-newsfeed-wrapper">
    <div class="card-header">
        <a href="https://www.opennms.com/blog/" target="_blank">OpenNMS News Feed</a>
    </div>
    <div id="opennms-newsfeed-content">
    </div>
</div>

<style>
    .opennms-newsfeed-wrapper {
        max-height: 500px;
        overflow: auto;
    }

    #opennms-newsfeed-content {
        padding: 6px 4px 4px 4px;
    }

    .opennms-newsfeed-item {

    }

    .opennms-newsfeed-error {
        color: #f00;
        font-weight: bold;
    }
</style>

<script type="text/javascript">
(function() {
    function createTagLink(tag) {
        const url = 'https://www.opennms.com/en/blog/tag/' + tag + '/';
        return '<a href="' + url + '" target="_blank">#' + tag + '</a>';
    }

    function createCategoryLink(category) {
        let cat = category.toLowerCase().replace(' ', '-');

        if (cat === 'on-the-horizon') {
            cat = 'opennms-on-the-horizon';
        }

        const url = 'https://www.opennms.com/en/blog/category/' + cat + '/';
        return '<a href="' + url + '" target="_blank">' + category + '</a>';
    }

    function createTemplate(item, index) {
        const arr = []

        if (index > 0) {
            arr.push('<hr />');
        }

        arr.push('<div class="opennms-newsfeed-item">');
        arr.push('<h6><a href="' + item.link + '" target="_blank">' + item.title + '</a></h6>');
        arr.push('<span>' + item.shortDescription + '</span>');
        arr.push('<br ');

        const categoryLinks = item.categories.map(c => createCategoryLink(c));
        let cats = '<span><strong>&bull;</strong> ' + categoryLinks.join(' ');

        const tagLinks = item.tags.map(t => createTagLink(t));
        const tags = tagLinks.join(' ');

        if (tagLinks.length > 0) {
          cats = cats + ' ' + tags;
        }

        arr.push(cats);
        arr.push('</span>');
        arr.push('</div>');

        return arr.join('\n');
    }

    function displayError(msg) {
        const template =
          '<div class="opennms-newsfeed-error">\n' +
          '<span>' + msg + '</span>\n' +
          '</div>\n';

        $('#opennms-newsfeed-content').append(template);
    }

    function parseNewsFeed(data) {
        $('#opennms-newsfeed-content').empty();

        if (!data || (data && !data.items) || data.error) {
            let message = 'Unknown error retrieving News Feed data.';

            if (data && data.error) {
                message = data.error;
            } else if (data && !data.items) {
                message = 'No News Feed items found.';
            }

            displayError(message);
            return;
        }

        data.items.forEach(function(item, index) {
            const template = createTemplate(item, index);
            $('#opennms-newsfeed-content').append(template);
        });
    }

    function getNewsFeed() {
        <%
          final String propertyUrl = System.getProperty("opennms.newsFeedPanel.url");

          if (propertyUrl != null && propertyUrl.length() > 0) { %>
          const newsFeedUrl = '<%=propertyUrl%>';
        <% } else { %>
          const newsFeedUrl = 'api/v2/newsfeed';
        <% } %>

        $.ajax({
            url: newsFeedUrl,
            method: 'GET',
            dataType: 'json',
            success: function(data) {
                parseNewsFeed(data);
            },
            error: function(jqXhr, errorStatus) {
                displayError('Error retrieving News Feed data');
            }
        });
    }

    $(document).ready(function() {
        getNewsFeed();
    });
})();
</script>