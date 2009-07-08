/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.controller;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.StreamUtils;
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.svclayer.RrdGraphService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class RrdGraphController extends AbstractController {
    private RrdGraphService m_rrdGraphService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String[] requiredParameters = new String[] {
                "resourceId",
                "start",
                "end"
        };
        
        for (String requiredParameter : requiredParameters) {
            if (request.getParameter(requiredParameter) == null) {
                throw new MissingParameterException(requiredParameter,
                                                    requiredParameters);
            }
        }

        String resourceId = request.getParameter("resourceId");
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        
        long startTime;
        long endTime;
        try {
            startTime = WebSecurityUtils.safeParseLong(start);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not parse start '"
                                               + start + "' as an integer time: " + e.getMessage(), e);
        }
        try {
            endTime = WebSecurityUtils.safeParseLong(end);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not parse end '"
                                               + end + "' as an integer time: " + e.getMessage(), e);
        }
        
        InputStream tempIn;
        if ("true".equals(request.getParameter("adhoc"))) {
            String[] adhocRequiredParameters = new String[] {
                    "title",
                    "ds",
                    "agfunction",
                    "color",
                    "dstitle",
                    "style"
            };
            
            for (String requiredParameter : adhocRequiredParameters) {
                if (request.getParameter(requiredParameter) == null) {
                    throw new MissingParameterException(requiredParameter,
                                                        adhocRequiredParameters);
                }
            }

            String title = request.getParameter("title");
            String[] dataSources = request.getParameterValues("ds");
            String[] aggregateFunctions = request.getParameterValues("agfunction");
            String[] colors = request.getParameterValues("color");
            String[] dataSourceTitles = request.getParameterValues("dstitle");
            String[] styles = request.getParameterValues("style");
            
            tempIn = m_rrdGraphService.getAdhocGraph(resourceId,
                                                     title,
                                                     dataSources,
                                                     aggregateFunctions,
                                                     colors,
                                                     dataSourceTitles,
                                                     styles,
                                                     startTime, endTime);
        } else {
            String report = request.getParameter("report");
            if (report == null) {
                throw new MissingParameterException("report");
            }
            
            tempIn = m_rrdGraphService.getPrefabGraph(resourceId,
                                                      report, startTime, endTime);
        }

        response.setContentType("image/png");
        
        StreamUtils.streamToStream(tempIn, response.getOutputStream());

        tempIn.close();
                
        return null;
    }

    public RrdGraphService getRrdGraphService() {
        return m_rrdGraphService;
    }

    public void setRrdGraphService(RrdGraphService rrdGraphService) {
        m_rrdGraphService = rrdGraphService;
    }
}
