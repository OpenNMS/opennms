/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Get news feed",
            description = """
        Fetches an RSS feed server-side and returns it as JSON. The source defaults to
        `https://www.opennms.com/feed/` and can be overridden with the `opennms.newsFeedPanel.url`
        system property. Responses are cached in the web tier for five minutes, so repeated calls do
        not re-fetch and a change at the source can take that long to appear.

        `description` is the feed's raw HTML and `shortDescription` the same text with markup stripped.
        No publication date is carried through: the items arrive in feed order.

        The call reaches the internet from the OpenNMS host. An install with no outbound access answers
        500 rather than an empty feed.""",
            operationId = "NewsFeedRestServiceGetNewsFeed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The parsed feed.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = NewsFeed.class),
                            examples = @ExampleObject(value = """
                    {
                      "items": [
                        {
                          "categories": ["News"],
                          "tags": ["meridian"],
                          "title": "August 2026 Releases - Meridian 2025.0.9 and 2024.3.12",
                          "link": "https://www.opennms.com/en/blog/2026-08-12-august-2026-releases/",
                          "description": "<p>In August, we released updates to all OpenNMS Meridian versions under active support.</p>",
                          "shortDescription": "In August, we released updates to all OpenNMS Meridian versions under active support."
                        }
                      ],
                      "channelTitle": "The OpenNMS Group, Inc."
                    }"""))),
            @ApiResponse(responseCode = "500", description = """
                    The feed could not be fetched or parsed: an unreachable source, a non-200 from it, or
                    a body that is not the expected RSS. The cause is logged, not returned.""",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error building news feed.")))
    })
    public Response getNewsFeed(final @Context HttpServletRequest request) {
        // Honor the legacy opennms.newsFeedPanel.show gate: when disabled (e.g.
        // air-gapped / privacy-sensitive installs) return No Content WITHOUT
        // touching the cache, so no outbound request to the feed URL is made.
        if (!Boolean.parseBoolean(System.getProperty("opennms.newsFeedPanel.show", "true"))) {
            return Response.noContent().build();
        }
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
