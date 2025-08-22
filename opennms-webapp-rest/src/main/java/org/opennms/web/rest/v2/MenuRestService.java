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
    @Operation(summary = "Get main menu", description = "Get main menu", operationId = "MenuRestServiceGetMainMenu")
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

        // TODO: These may not be needed, need more testing to be sure that variable expansion is working
        if (containsHomeVariable(menuProvider.getDispatcherServletPath())) {
            this.menuProvider.setDispatcherServletPath(getExpandedWebInfPrefixPath(request, menuProvider.getDispatcherServletPath()));
        }

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
