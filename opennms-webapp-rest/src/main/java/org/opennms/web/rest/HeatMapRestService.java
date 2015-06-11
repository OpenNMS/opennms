/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import com.google.common.collect.Lists;
import com.sun.jersey.spi.resource.PerRequest;
import org.json.JSONObject;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.HeatMapElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@PerRequest
@Scope("prototype")
@Path("heatmap")
public class HeatMapRestService extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(HeatMapRestService.class);

    @Autowired
    private OutageDao m_outageDao;

    /**
     * Transforms a list of heatmap elements to a json map.
     *
     * @param heatMapElements the list of heatmap elements
     * @return the map for the json response
     */
    private Map<String, List<Map<String, Object>>> transformResults(List<HeatMapElement> heatMapElements) {
        /**
         * the item list
         */
        final List<Map<String, Object>> itemList = new ArrayList<>();

        /**
         * Helper field for sizes
         */
        HashMap<String, Integer> elementSizes = new HashMap<>();

        /**
         * counter for total of services
         */
        int totalServices = 0;

        /**
         * iterate over the heatmap elements and increase the total
         * number of services. This is later used to compute the sizes
         * for the heatmap boxes...
         */
        for (HeatMapElement heatMapElement : heatMapElements) {
            if (heatMapElement.getServicesTotal() > 0) {

                Double color;

                if (heatMapElement.getServicesTotal()==0) {
                    color = 0.0;
                } else {
                    color = (double) heatMapElement.getServicesDown() / (double) heatMapElement.getServicesTotal();
                }

                elementSizes.put(heatMapElement.getName(), heatMapElement.getServicesTotal());

                Map<String, Object> item = new HashMap<>();
                item.put("id", heatMapElement.getName());
                item.put("elementId", heatMapElement.getId());
                item.put("color", Lists.newArrayList(color));

                itemList.add(item);

                totalServices += heatMapElement.getServicesTotal();
            }
        }

        /**
         * now iterate over the results and set the size attribute for
         * each entry...
         */
        for (Map<String, Object> map : itemList) {
            int nodesInCategory = elementSizes.get(map.get("id"));
            double size = (double) nodesInCategory / (double) totalServices;
            map.put("size", Lists.newArrayList(Double.valueOf(size)));
        }

        /**
         * create the "outer" map and add the list to it...
         */
        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        map.put("children", itemList);

        return map;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("categories")
    public Response categories() throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("categories.categoryname", "categories.categoryid", null, null);
        final JSONObject jo = new JSONObject(transformResults(heatMapElements));
        return Response.ok(jo.toString(), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("foreignSources")
    public Response foreignsources() throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("foreignsource", "0", null, null, "foreignsource");
        final JSONObject jo = new JSONObject(transformResults(heatMapElements));
        return Response.ok(jo.toString(), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("nodesByCategory/{category}")
    public Response nodesByCategory(@PathParam("category") final String category) throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("node.nodelabel", "node.nodeid", "categories.categoryname", category);
        final JSONObject jo = new JSONObject(transformResults(heatMapElements));
        return Response.ok(jo.toString(), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("nodesByForeignSource/{foreignSource}")
    public Response nodesByForeignSource(@PathParam("foreignSource") final String foreignSource) throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("node.nodelabel", "node.nodeid", "foreignsource", foreignSource);
        final JSONObject jo = new JSONObject(transformResults(heatMapElements));
        return Response.ok(jo.toString(), MediaType.APPLICATION_JSON).build();
    }
}

