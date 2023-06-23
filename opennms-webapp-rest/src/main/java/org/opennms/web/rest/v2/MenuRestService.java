/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
import org.opennms.web.rest.support.menu.MainMenu;
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

        // TODO: This may not be needed, need more testing to be sure that variable expansion is working
        String webInfRealPath = request.getServletContext().getRealPath(WEB_INF_PREFIX);

        if (this.menuProvider.getDispatcherServletPath().contains("${opennms.home}")) {
            int index = this.menuProvider.getDispatcherServletPath().indexOf(WEB_INF_PREFIX);

            if (index >= 0) {
                String path = webInfRealPath + this.menuProvider.getDispatcherServletPath().substring(index + WEB_INF_PREFIX.length());
                this.menuProvider.setDispatcherServletPath(path);
            }
        }

        if (this.menuProvider != null) {
            try {
                HttpMenuRequestContext context = new HttpMenuRequestContext(request);
                mainMenu = this.menuProvider.getMainMenu(context);
            } catch (Exception e) {
                LOG.error("Error creating menu entries: " + e.getMessage(), e);
                throw e;
            }
        }

        return mainMenu;
    }
}
