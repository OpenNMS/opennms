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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.time.CentralizedDateTimeFormat;
import org.opennms.web.rest.support.menu.HttpMenuRequestContext;
import org.opennms.web.rest.support.menu.model.MainMenu;
import org.opennms.web.rest.support.menu.MenuProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Web Service using REST for retrieving information to dynamically build the Vue webapp's Menubar component.
 */
@Component
@Path("menu")
@Tag(name = "Menu", description = "Menu API")
public class MenuRestService {
    private static final Logger LOG = LoggerFactory.getLogger(MenuRestService.class);
    private static final String WEB_INF_PREFIX = "/WEB-INF";
    private CentralizedDateTimeFormat dateTimeFormat = new CentralizedDateTimeFormat();

    @Autowired
    private MenuProvider menuProvider;

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Get main menu",
            description = """
        The whole menubar for the calling user, assembled from the menu template plus the request's
        roles, so two users can get different `menus` contents from the same call. Alongside the menu
        trees the response carries the chrome the Vue shell needs: `baseHref`, `username`, `version`,
        `copyrightDates`, the formatted server date and time, the notification status, and the Zenith
        Connect and add-node feature flags.

        `menus` is the top-level bar. `helpMenu`, `selfServiceMenu`, `userNotificationMenu`,
        `provisionMenu` and `configurationMenu` are the fixed right-hand entries, each a single menu
        object rather than a list. Every entry shares one shape; unused members come back as null, and
        `items` is null for a leaf and a list for a submenu.""",
            operationId = "MenuRestServiceGetMainMenu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The menu definition for the calling user.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MainMenu.class),
                            examples = @ExampleObject(value = """
                    {
                      "templateName": "default",
                      "baseHref": "http://localhost:8980/opennms/",
                      "homeUrl": "http://localhost:8980/opennms/index.jsp",
                      "formattedDateTime": "2026-08-26T02:57:01-04:00",
                      "formattedDate": "August 26, 2026",
                      "formattedTime": "02:57:01 UTC-04",
                      "noticeStatus": "Off",
                      "username": "admin",
                      "baseNodeUrl": "element/node.jsp?node=",
                      "zenithConnectEnabled": false,
                      "zenithConnectBaseUrl": "",
                      "zenithConnectRelativeUrl": "",
                      "displayAddNodeButton": false,
                      "sideMenuInitialExpand": false,
                      "copyrightDates": "2002-2026",
                      "version": "36.0.4-SNAPSHOT",
                      "menus": [
                        {
                          "type": "header",
                          "id": "emptyHeader",
                          "name": "",
                          "url": null,
                          "isExternalLink": null,
                          "locationMatch": null,
                          "action": null,
                          "linkTarget": null,
                          "icon": null,
                          "roles": null,
                          "requiredSystemProperties": [],
                          "items": null
                        }
                      ],
                      "helpMenu": {
                        "type": null,
                        "id": "helpMenu",
                        "name": "Help",
                        "url": "#",
                        "roles": null,
                        "requiredSystemProperties": [],
                        "items": []
                      },
                      "selfServiceMenu": {
                        "id": "selfServiceMenu",
                        "name": "admin",
                        "url": "account/selfService/index.jsp",
                        "requiredSystemProperties": [],
                        "items": [
                          {"id": "changePassword", "name": "Change Password", "url": "account/selfService/newPasswordEntry", "items": null}
                        ]
                      },
                      "userNotificationMenu": {
                        "id": "userNotificationMenu",
                        "requiredSystemProperties": [],
                        "items": [
                          {"id": "userNotificationConfiguration", "url": "admin/notification/index.jsp", "items": null}
                        ]
                      },
                      "provisionMenu": {
                        "id": "provisionMenu",
                        "name": "Quick-Add Node",
                        "url": "admin/ng-requisitions/quick-add-node.jsp#/",
                        "roles": ["ROLE_ADMIN", "ROLE_PROVISION"],
                        "requiredSystemProperties": [],
                        "items": null
                      },
                      "configurationMenu": {
                        "id": "configurationMenu",
                        "name": "Configure OpenNMS",
                        "url": "admin/index.jsp",
                        "roles": ["ROLE_ADMIN"],
                        "requiredSystemProperties": [],
                        "items": null
                      }
                    }"""))),
            @ApiResponse(responseCode = "500", description = "The menu template could not be read or parsed. The cause is logged, not returned.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error building menu.")))
    })
    public Response getMainMenu(final @Context HttpServletRequest request) {
        try {
            MainMenu mainMenu = buildMenu(request);
            return Response.ok(mainMenu).build();
        } catch (Exception e) {
            throw new WebApplicationException(
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("Error building menu.").build());
        }
    }

    /**
     * Build the menu definition.
     * Should correspond to logic in opennms-webapp org.opennms.web.controller.NavBarController
     * as well as opennms-webapp webapp/WEB-INF/templates/navbar.ftl.
     */
    private MainMenu buildMenu(final HttpServletRequest request) throws Exception {
        MainMenu mainMenu = null;
        this.menuProvider.setMenuRequestContext(new HttpMenuRequestContext(request));

        if (containsHomeVariable(menuProvider.getMenuTemplateFilePath())) {
            this.menuProvider.setMenuTemplateFilePath(getExpandedWebInfPrefixPath(request, menuProvider.getMenuTemplateFilePath()));
        }

        try {
            mainMenu = this.menuProvider.getMainMenu();
        } catch (Exception e) {
            LOG.error("Error creating menu entries: " + e.getMessage(), e);
            throw e;
        }

        return mainMenu;
    }

    private boolean containsHomeVariable(String path) {
        return path != null && path.contains("${opennms.home}");
    }

    private String getExpandedWebInfPrefixPath(final HttpServletRequest request, final String path) {
        int index = path.indexOf(WEB_INF_PREFIX);

        if (index >= 0) {
            String webInfRealPath = request.getServletContext().getRealPath(WEB_INF_PREFIX);
            return webInfRealPath + path.substring(index + WEB_INF_PREFIX.length());
        }

        return path;
    }
}
