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

package org.opennms.web.rest.v2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.web.rest.support.newsfeed.NewsFeed;
import org.opennms.web.rest.support.newsfeed.NewsFeedProvider;
import org.opennms.web.rest.support.newsfeed.xml.NewsFeedXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import joptsimple.internal.Strings;

/**
 * Web Service for retrieving OpenNMS RSS feed.
 */
@Component
@Path("newsfeed")
@Tag(name = "Newsfeed", description = "Newsfeed API")
public class NewsFeedRestService {
    private static final String DEFAULT_NEWS_FEED_URL = "https://www.opennms.com/feed/";
    private static final Logger LOG = LoggerFactory.getLogger(NewsFeedRestService.class);

    private String feedUrl;

    @Autowired
    private NewsFeedProvider newsFeedProvider;

    private final LoadingCache<String, String> cache;

    public NewsFeedRestService() {
        String newsFeedUrlProperty = System.getProperty("opennms.newsFeedPanel.url", "");
        this.feedUrl = !Strings.isNullOrEmpty(newsFeedUrlProperty) ? newsFeedUrlProperty : DEFAULT_NEWS_FEED_URL;

        this.cache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(100)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String cacheKey) throws Exception {
                    String body = "";

                    try {
                        body = requestNewsFeed(feedUrl);
                    } catch (Exception e) {
                        LOG.error("News Feed: Got exception requesting feed from {}: {}", feedUrl, e.getMessage());
                        throw e;
                    }

                    return body;
                }
            });
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Get news feed", description = "Get news feed", operationId = "NewsFeedRestServiceGetNewsFeed")
    public Response getNewsFeed(final @Context HttpServletRequest request) {
        try {
            String responseBody = this.cache.get("newsfeed");

            InputStream inputStream = new ByteArrayInputStream(responseBody.getBytes());

            NewsFeedXml.RssElement xRss = newsFeedProvider.parseXml(inputStream);
            NewsFeed newsFeed = newsFeedProvider.parseXmlToNewsFeed(xRss);

            return Response.ok(newsFeed).build();
        } catch (Exception e) {
            LOG.error("News Feed: Got exception: {}", e.getMessage());

            throw new WebApplicationException(
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("Error building news feed.").build());
        }
    }

    private String requestNewsFeed(final String feedUrl) throws Exception, IOException, InterruptedException {
        LOG.info("Requesting news feed from url {}.", feedUrl);

        final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        // 'User-Agent' header allows for statistics on News Feed access
        final HttpRequest httpRequest = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(feedUrl))
            .setHeader("User-Agent", "OpenNMS NewsFeedRestService")
            .build();

        final HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            LOG.error("Received error response from news feed: {}.", response.statusCode());

            throw new Exception("Error response from news feed.");
        }

        final String responseBody = response.body();
        LOG.info("Received news feed response.");

        return responseBody;
    }
}
